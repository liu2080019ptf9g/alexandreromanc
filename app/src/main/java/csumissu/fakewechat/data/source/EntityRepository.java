package csumissu.fakewechat.data.source;

import android.support.annotation.NonNull;

import java.util.List;

import csumissu.fakewechat.data.Status;
import csumissu.fakewechat.data.StatusResult;
import csumissu.fakewechat.data.User;
import rx.Observable;

/**
 * @author sunyaxi
 * @date 2016/5/20
 */
public class EntityRepository implements EntityDataSource {

    private static EntityRepository INSTANCE;
    private final EntityDataSource mLocalDataSource;
    private final EntityDataSource mRemoteDataSource;
    private User mOwner;

    private EntityRepository(@NonNull EntityDataSource localDataSource,
                             @NonNull EntityDataSource remoteDataSource) {
        mLocalDataSource = localDataSource;
        mRemoteDataSource = remoteDataSource;
    }

    public synchronized static EntityRepository getInstance(EntityDataSource localDataSource,
                                                            EntityDataSource remoteDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new EntityRepository(localDataSource, remoteDataSource);
        }
        return INSTANCE;
    }

    @Override
    public Observable<User> getOwner() {
        Observable<User> memory = Observable.just(mOwner);
        Observable<User> disk = mLocalDataSource.getOwner().doOnNext(this::saveOwner);
        Observable<User> network = mRemoteDataSource.getOwner().doOnNext(user -> {
            saveOwner(user);
            mLocalDataSource.saveOwner(user);
        });
        return Observable.concat(memory, disk, network).first();
    }

    @Override
    public void saveOwner(User user) {
        mOwner = user;
    }

    @Override
    public Observable<StatusResult> getStatusResult() {
        return mRemoteDataSource.getStatusResult().doOnNext(mLocalDataSource::saveStatusResult);
    }

    @Override
    public void saveStatusResult(StatusResult statusResult) {
        // Do nothing, as this data could be large.
    }

    @Override
    public Observable<List<Status>> getStatuses(int count, int page) {
        Observable<List<Status>> disk = mLocalDataSource.getStatuses(count, page);
        Observable<List<Status>> network = mRemoteDataSource.getStatusResult()
                .doOnNext(mLocalDataSource::saveStatusResult)
                .map(statusResult -> statusResult.getStatuses().subList(count * page, count * (page + 1)));
        return Observable.concat(disk, network).first();
    }
}
