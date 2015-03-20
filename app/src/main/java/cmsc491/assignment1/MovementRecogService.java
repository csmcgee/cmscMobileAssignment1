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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MovementRecogService extends Service {
    private MRBinder mBinder = new MRBinder();
    private final Handler mHandler = new Handler();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Integer[] statuses;
    private int iter_counter = 0;
    private int data_iter = 0;

    private PrintWriter printWriter;
    private File file;

    private final int ELAPSED_TIME_SECONDS = 15;
    private final int NUM_OF_ITERATIONS = 120 / ELAPSED_TIME_SECONDS;
    private final int NUM_OF_COORDS = 3;
    private final int NUM_DATA_POINTS = 60;
    public static final String FILE_NAME = "Movements.txt";
    private float xVariance = 0.0f;
    private float yVariance = 0.0f;
    private float zVariance = 0.0f;

    float accelData[][] = new float[NUM_OF_COORDS][NUM_DATA_POINTS];
    float rMatrix[] = new float[9];
    float angles[] = new float[3];

    @Override
    public IBinder onBind(Intent intent) {



        // Prepare file system
        if(isExternalStorageWritable()){
            file = new File(getExternalFilesDir(null), FILE_NAME);
            try{
                if(!file.exists()){
                    file.createNewFile();
                }
                printWriter = new PrintWriter(new FileWriter(file));
                // read from file system to retrieve last 10 movements

            }catch(Exception e){
                Log.e("MovementRecog", "Unable to write to file system.");
            }

        }

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
               // Log.i("MovementRecog", "Iteration hit.");

                //Insert variance logic here with accelData points;
                accelData[0][data_iter] = accel.getX();
                accelData[1][data_iter] = accel.getY();
                accelData[2][data_iter] = accel.getZ();
                data_iter++;

                if(data_iter >= 60) {

                    xVariance = calcVariance(accelData[0], calcMean(accelData[0]));
                    Log.i("XVAR", String.valueOf(xVariance));
                    yVariance = calcVariance(accelData[1], calcMean(accelData[1]));
                    zVariance = calcVariance(accelData[2], calcMean(accelData[2]));
                    data_iter = 0;
                }


                // After algorithm has run, increment and check iteration counter
                if(iter_counter++ >= NUM_OF_ITERATIONS){
                    Log.i("MovementRecog", "Decision period reached.");
                    // reset counter
                    iter_counter = 0;


                    // make decision of type of movement
                    //Putting in a base value of 1 for the variance check here
                    if(xVariance >= 1f){
                        //Person has been moving.
                    }
                    else{
                        //Person has been sitting/laying down. Gyroscope?
                    }

                    // write to file system
                    // update data structure
                }


                mHandler.postDelayed(this, 250);
            }
        }, 0);



        return mBinder;
    }

    //Calculate means on float array
    public float calcMean(float[] points){
        float sum = 0.0f;
        for(float a : points)
            sum += a;
        return sum/points.length;
    }

    //Calculate variances of float array
    public float calcVariance(float[] points, float mean){


        float sums = 0.0f;
        for(float f:points){
            sums += (f - mean) * (f - mean);
        }
        return sums/(points.length -1);

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
        public float getY(){
            return yaccl;
        }
        public float getZ(){
            return zaccl;
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
}
