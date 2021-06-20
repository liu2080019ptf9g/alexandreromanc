package csumissu.fakewechat.comments;

import android.support.annotation.NonNull;

import csumissu.fakewechat.data.source.EntityRepository;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static csumissu.fakewechat.util.Preconditions.checkNotNull;

/**
 * @author sunyaxi
 * @date 2016/5/23
 */
public class CommentsPresenter implements CommentsContract.Presenter {

    private final CommentsContract.View mView;
    private final EntityRepository mRepository;

    public CommentsPresenter(@NonNull CommentsContract.View view,
                             @NonNull EntityRepository repository) {
        mView = checkNotNull(view);
        mRepository = checkNotNull(repository);
    }

    @Override
    public void start() {
        loadOwner();
        loadAllStatuses(false);
    }

    @Override
    public void loadOwner() {
        mRepository.getOwner()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mView::showOwner);
    }

    @Override
    public void loadAllStatuses(boolean forceUpdate) {
        mRepository.getStatusResult()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> mView.setRefreshIndicator(true))
                .doOnTerminate(() -> mView.setRefreshIndicator(false))
                .subscribe(statusResult -> {
                    loadStatuses(1, false);
                }, throwable -> {
                    mView.showLoadingError();
                });
    }

    @Override
    public void loadStatuses(int page) {
        loadStatuses(page, true);
    }

    private void loadStatuses(int page, final boolean showLoadingUI) {
        mRepository.getStatuses(20, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> {
                    if (showLoadingUI) {
                        mView.setLoadingIndicator(true);
                    }
                })
                .doOnTerminate(() -> {
                    if (showLoadingUI) {
                        mView.setLoadingIndicator(false);
                    }
                })
                .subscribe(mView::showStatuses, throwable -> {
                    mView.showLoadingError();
                });
    }

}
