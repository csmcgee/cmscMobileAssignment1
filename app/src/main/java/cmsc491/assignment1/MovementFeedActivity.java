package cmsc491.assignment1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import cmsc491.assignment1.domain.activityRecog.Movement;
import cmsc491.assignment1.domain.impl.MFAdapter;


public class MovementFeedActivity extends ActionBarActivity {
    private ListView listView;
    private final MovementRecogPoller mrPoller = new MovementRecogPoller();

    private MovementRecogService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MovementRecogService.MRBinder binder = (MovementRecogService.MRBinder) service;
            mService = binder.getService();

            // when service is available start polling
            Log.i(Movement.APP_TAG, "Starting poller.");
            mrPoller.run();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    // use this adapter to let list view know that it has been updated
    public static final MFAdapter mfAdapter = new MFAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement_feed);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.listFeed);

        // Assign adapter to ListView
        listView.setAdapter(mfAdapter);
        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) { }

        });
    }

    protected void onStart(){
        super.onStart();
        // Spawn service
        Intent intent = new Intent(this, MovementRecogService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    protected void onResume(){
        super.onResume();

        // if service is already bound
        if(mService != null)
            mrPoller.run();
    }

    protected void onPause(){
        super.onPause();
        // stop polling to update activity on pause.
        mrPoller.stop();
    }

    /**
     * No longer visible and should release all resources here.
     */
    protected void onStop(){
        super.onStop();
        Log.i(Movement.APP_TAG, "Activity stopped (hidden).");
        mrPoller.stop();
    }

    protected void onDestroy(){
        super.onDestroy();
        Log.i(Movement.APP_TAG, "Activity destroyed");
        unbindService(mConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movement_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sensor) {
            Intent intent = new Intent(this, SensorActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Used to poll service for movement type activity and place it in
     * list view.
     */
    private class MovementRecogPoller implements Runnable {
        private Handler mHandler;

        public MovementRecogPoller(){
            mHandler = new Handler();
        }

        // Beware of first case, perhaps refactor to not poll on first attempt
        public void pollService(){

            // poll service
            mfAdapter.setMovements(mService.getRecentMovements());
            mfAdapter.notifyDataSetChanged();
        }

        @Override
        public void run() {
            pollService();
            // every two minutes
            mHandler.postDelayed(this, 120 * 1000);
        }

        public void stop(){
            mHandler.removeCallbacks(this);
        }
    }
}
