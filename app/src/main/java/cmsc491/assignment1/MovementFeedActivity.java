package cmsc491.assignment1;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import cmsc491.assignment1.domain.activityRecog.Movement;
import cmsc491.assignment1.domain.views.MFAdapter;


public class MovementFeedActivity extends ActionBarActivity {
    private ListView listView;
    private Handler mHandler;

    // use this adapter to let list view know that it has been updated
    public final MFAdapter mfAdapter = new MFAdapter();

    public static final String FILE_NAME = "Movements.txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement_feed);

        boolean check = isExternalStorageWritable();

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

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // EXAMPLE:
                // every 15 seconds notify data has changed
                mfAdapter.updateFeed();
                mfAdapter.notifyDataSetChanged();
                mHandler.postDelayed(this, 10 * 1000);
            }
        }, 10 * 1000);

        if(isExternalStorageWritable()){
            File file = new File(getExternalFilesDir(null), FILE_NAME);
            try {
                PrintWriter pw = new PrintWriter(new FileWriter(file));
                pw.write("Hello World.");
                pw.close();
            } catch (IOException e) {
                // can't write
                e.printStackTrace();
            }
        }

    }

    public void onStop(){
        super.onStop();
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

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
