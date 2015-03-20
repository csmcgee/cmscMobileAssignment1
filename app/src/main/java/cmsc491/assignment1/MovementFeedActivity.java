package cmsc491.assignment1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
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
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                Movement  itemValue    = (Movement) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue.getTypeString(), Toast.LENGTH_LONG)
                        .show();

            }

        });

    }

    protected void onStart(){
        super.onStart();
        // Spawn service
        Intent intent = new Intent(this, MovementRecogService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mrPoller.run();
    }

    /**
     * No longer visible and should release all resources here.
     */
    protected void onStop(){
        super.onStop();
        unbindService(mConnection);
        mrPoller.stop();
        // close file writer/reader here.
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
