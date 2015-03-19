package cmsc491.assignment1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MovementRecogService extends Service {
    private MRBinder mBinder = new MRBinder();
    private final Handler mHandler = new Handler();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Integer[] statuses;
    private int ELAPSEDTIME = 15;


//    public MovementRecogService() {
//    }



    @Override
    public IBinder onBind(Intent intent) {



        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        final accelerometerInfo accel = new accelerometerInfo();
        final gyroscopeInfo gyro = new gyroscopeInfo();
        statuses = new Integer[3];



        sensorManager.registerListener(accel, accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyro, gyroscope,SensorManager.SENSOR_DELAY_NORMAL);


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO: Refactor to - The main activity at regular intervals should poll for the set of activities from the service and write it to external storage.
                // every 15 seconds notify data has changed


                if(Math.abs(accel.getX()) > 0){
                    statuses[0]++;
                }
                else{
//                    if(gyro.getY())
                }



                MovementFeedActivity.mfAdapter.updateFeed();
                MovementFeedActivity.mfAdapter.notifyDataSetChanged();
                mHandler.postDelayed(this, ELAPSEDTIME * 1000);
            }
        }, 0);
        return mBinder;
    }



    public class MRBinder extends Binder {
        MovementRecogService getService() {
            return MovementRecogService.this;
        }
    }

    public class accelerometerInfo implements SensorEventListener {


        private float xaccl= 0, yaccl = 0, zaccl = 0;

        public void onCreate(){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                xaccl = event.values[0];
                yaccl = event.values[1];
                zaccl = event.values[2];
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public float getX(){
            return xaccl;
        }
    }

    public class gyroscopeInfo implements SensorEventListener{


        private float xgyro= 0, ygyro = 0, zgyro = 0;

        public void onCreate(){
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                xgyro = event.values[0];
                ygyro = event.values[1];
                zgyro = event.values[2];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public float getY(){
            return ygyro;
        }
    }
}
