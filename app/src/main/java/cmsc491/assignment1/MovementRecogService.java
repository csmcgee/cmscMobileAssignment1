package cmsc491.assignment1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class MovementRecogService extends Service {
    private MRBinder mBinder = new MRBinder();
    private final Handler mHandler = new Handler();

    public MovementRecogService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO: Refactor to - The main activity at regular intervals should poll for the set of activities from the service and write it to external storage.
                // every 15 seconds notify data has changed
                MovementFeedActivity.mfAdapter.updateFeed();
                MovementFeedActivity.mfAdapter.notifyDataSetChanged();
                mHandler.postDelayed(this, 10 * 1000);
            }
        }, 10 * 1000);
        return mBinder;
    }

    public class MRBinder extends Binder {
        MovementRecogService getService() {
            return MovementRecogService.this;
        }
    }
}
