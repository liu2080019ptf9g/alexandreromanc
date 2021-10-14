package csumissu.fakewechat.v2.base;

import android.view.MenuItem;

import com.squareup.leakcanary.RefWatcher;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import csumissu.fakewechat.v2.AppComponent;
import csumissu.fakewechat.v2.WechatApp;
import csumissu.fakewechat.v2.util.KeyboardUtils;

/**
 * @author sunyaxi
 * @date 2016/11/17.
 */
public class BaseActivity extends RxAppCompatActivity {

    protected WechatApp getApplicationExt() {
        return (WechatApp) getApplication();
    }

    protected AppComponent getApplicationComponent() {
        return getApplicationExt().component();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isFinishing()) {
                    finish();
                } else {
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        KeyboardUtils.hideSoftInput(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = getApplicationExt().getRefWatcher();
        refWatcher.watch(this);
    }
}
