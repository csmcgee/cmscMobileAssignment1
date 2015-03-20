package cmsc491.assignment1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import cmsc491.assignment1.domain.activityRecog.Movement;

public class MovementRecogService extends Service {
    private MRBinder mBinder = new MRBinder();
    private final Handler mHandler = new Handler();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Integer[] statuses;
    private MovementFileManager movementFileManager;
    private int iter_counter = 0;

    private final String FILE_NAME = "Movement.txt";
    private final int ELAPSED_TIME_SECONDS = 15;
    private final int NUM_OF_ITERATIONS = 120 / ELAPSED_TIME_SECONDS;


    float rMatrix[] = new float[9];
    float angles[] = new float[3];

    @Override
    public IBinder onBind(Intent intent) {
        // Prepare file system
        movementFileManager = new MovementFileManager(FILE_NAME);
        movementFileManager.initializeFile();


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        final AccelerometerInfo accel = new AccelerometerInfo();
        final GyroscopeInfo gyro = new GyroscopeInfo();
        statuses = new Integer[3];

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        sensorManager.registerListener(accel, accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyro, gyroscope,SensorManager.SENSOR_DELAY_NORMAL);


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("MovementRecog", "Iteration hit.");
                if(Math.abs(accel.getX()) > 0){
                    statuses[0]++;
                }
                else{
//                    if(gyro.getY())
                }

                // After algorithm has run, increment and check iteration counter
                if(iter_counter++ >= NUM_OF_ITERATIONS){
                    Log.i("MovementRecog", "Decision period reached.");
                    // reset counter
                    iter_counter = 0;

                    // make decision of type of movement
                    // write to file system
                    // update data structure
                }


                mHandler.postDelayed(this, ELAPSED_TIME_SECONDS * 1000);
            }
        }, 0);


        return mBinder;
    }

    public void onDestroy(){
        movementFileManager.closeFile();
    }


    public class MRBinder extends Binder {
        MovementRecogService getService() {
            return MovementRecogService.this;
        }
    }

    private class AccelerometerInfo implements SensorEventListener {


        private float xaccl= 0, yaccl = 0, zaccl = 0;

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

    private class GyroscopeInfo implements SensorEventListener{


        private float xgyro= 0, ygyro = 0, zgyro = 0;


        @Override
        public void onSensorChanged(SensorEvent event) {

            if(event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
                xgyro = event.values[0];
                ygyro = event.values[1];
                zgyro = event.values[2];

                sensorManager.getRotationMatrixFromVector(rMatrix, event.values);
                sensorManager.getOrientation(rMatrix, event.values);

                Log.i("MovementRecog", String.format("x: %.3f\t y: %.3f\t z: %.3f", event.values[0]*(180f/Math.PI),
                        event.values[1]*(180f/Math.PI),
                        event.values[2]*(180f/Math.PI)));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public float getY(){
            return ygyro;
        }
    }

    private class MovementFileManager {
        private String fileName;
        private PrintWriter printWriter;
        private File file;

        public MovementFileManager(String fileName){
            this.fileName = fileName;
        }

        public void initializeFile(){
            if(isExternalStorageWritable()){
                file = new File(getExternalFilesDir(null), fileName);
                try{
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    printWriter = new PrintWriter(new FileWriter(file, true));
                    // read from file system to retrieve last 10 movements

                }catch(Exception e){
                    Log.e("MovementRecog", "Unable to write to file system.");
                }

            }
        }

        public void writeMovement(Movement m){
            if(isExternalStorageWritable()){
                printWriter.append(m.toString());
            }
        }

        /* Checks if external storage is available for read and write */
        private boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }

        public void closeFile(){
            printWriter.close();
        }

        /* Checks if external storage is available to at least read */
        private boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return true;
            }
            return false;
        }
    }
}
