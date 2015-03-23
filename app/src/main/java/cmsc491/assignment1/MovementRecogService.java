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
import android.os.PowerManager;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cmsc491.assignment1.domain.activityRecog.Movement;

public class MovementRecogService extends Service {

    // Main data structure for polling last 10 movements
    List<Movement> recentMovements;

    private MRBinder mBinder = new MRBinder();
    private final Handler mHandler = new Handler();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Integer[] points = {0, 0, 0};
    private MovementFileManager movementFileManager;
    private int iter_counter = 0;
    private int data_iter = 0;
    private DateTime time;

    private AccelerometerInfo accel;
    private GyroscopeInfo gyro;
    private Runnable recogRunner;

    private final int ELAPSED_TIME_SECONDS = 15;
    // For now make a decision every minute
    private final int NUM_OF_ITERATIONS = 8;
    private final int NUM_OF_AXIS = 3;
    private final int NUM_DATA_POINTS = 60;
    private final int WALKING = 0;
    private final int SITTING = 1;
    private final int SLEEPING = 2;

    private float xVariance = 0.0f;
    private float yVariance = 0.0f;
    private float zVariance = 0.0f;

    private float yAngleMean = 0.0f;

    public static final String FILE_NAME = "Movements.txt";

    float accelData[][] = new float[NUM_OF_AXIS][NUM_DATA_POINTS];
    float rMatrix[] = new float[9];
    float angles[][] = new float[NUM_OF_AXIS][NUM_DATA_POINTS];

    private PowerManager pm;
    private PowerManager.WakeLock wl;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(Movement.APP_TAG, "MovementRecogService onBind()");
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MovementRecog");
        wl.acquire();

        // Prepare file system
        movementFileManager = new MovementFileManager(FILE_NAME);
        movementFileManager.initializeFile();
        recentMovements = movementFileManager.retrieveRecentMovements();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = new AccelerometerInfo();
        gyro = new GyroscopeInfo();

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorManager.registerListener(accel, accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyro, gyroscope,SensorManager.SENSOR_DELAY_NORMAL);

        // Initialize time
        time = new DateTime();

        recogRunner = new Runnable() {
            @Override
            public void run() {
                //Insert variance logic here with accelData points;
                accelData[0][data_iter] = accel.getX();
                accelData[1][data_iter] = accel.getY();
                accelData[2][data_iter] = accel.getZ();

                angles[0][data_iter] = gyro.getXAngle();
                angles[1][data_iter] = gyro.getYAngle();
                angles[2][data_iter] = gyro.getZAngle();
                data_iter++;

                // runs every 15 seconds
                if(data_iter >= 60) {
                    xVariance = calcVariance(accelData[0], calcMean(accelData[0]));
                    Log.i(Movement.APP_TAG, String.format("Y Variance: %f", yVariance));
                    yVariance = calcVariance(accelData[1], calcMean(accelData[1]));
                    zVariance = calcVariance(accelData[2], calcMean(accelData[2]));

                    yAngleMean = Math.abs(calcMean(angles[1]));
                    Log.i(Movement.APP_TAG, String.format("Y Angle mean: %f", yAngleMean));

                    /**
                     * Point assigning logic here.
                     */
                    if(yVariance >= 1f){ // Person was moving/walking.
                        points[WALKING]++;
                    }
                    else if(yAngleMean >= 45 && yAngleMean <= 135 ){ // Person was sitting
                        points[SITTING]++;
                    }else if(yAngleMean < 45 || yAngleMean > 135){ // Person was sleeping
                        points[SLEEPING]++;
                    }
                    data_iter = 0;
                    iter_counter++;
                }

                // Reached decision period.
                if(iter_counter >= NUM_OF_ITERATIONS){
                    Log.i(Movement.APP_TAG, String.format("Decision period reached for %s - %s", time.toString(), new DateTime().toString()));
                    // reset counter
                    iter_counter = 0;

                    Movement.Type type;

                    if(points[WALKING] > points[SITTING] && points[WALKING] > points[SLEEPING])
                        type = Movement.Type.WALKING;
                    else if(points[SITTING] > points[WALKING] && points[SITTING] > points[SLEEPING])
                        type = Movement.Type.SITTING;
                    else if(points[SLEEPING] > points[WALKING] && points[SLEEPING] > points[SITTING])
                        type = Movement.Type.SLEEPING;
                    else
                        type = Movement.Type.WALKING; // TIE-BREAKER

                    /**
                     * Review points and make final decision here.
                     */
                    trackMovement(new Movement(type, time, new DateTime()));
                    Log.i(Movement.APP_TAG, String.format("Movement type determined: %s", Movement.getTypeString(type)));
                    time = new DateTime(); // reinitialize date/time for next interval
                    Arrays.fill(points, 0);

                    // After decision is made immediately call itself to start next period.
                    mHandler.postDelayed(this, 0);
                }else
                    mHandler.postDelayed(this, 250); // If not decision time then wait 250 milliseconds.
            }
        };

        mHandler.postDelayed(recogRunner, 0);

        return mBinder;
    }

    public void onDestroy(){
        super.onDestroy();
        Log.i(Movement.APP_TAG, "Service onDestroy()");
        mHandler.removeCallbacks(recogRunner);
        wl.release();
    }

    public List<Movement> getRecentMovements(){
        return recentMovements;
    }

    private void trackMovement(Movement m){
        movementFileManager.writeMovement(m);
        recentMovements.add(0, m);

        if(recentMovements.size() > 10)
            recentMovements.remove(recentMovements.size()-1);
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
        return sums/(points.length - 1);

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
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

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

    private class GyroscopeInfo implements SensorEventListener {

        /**
         * x - Tangential to the ground pointing approx East.
         * y - Tangential to the ground points toward the North Pole.
         * z - Points toward the sky is perpendicular to the ground plane.
         */
        private float xgyro= 0, ygyro = 0, zgyro = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {


            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                xgyro = event.values[0];
                ygyro = event.values[1];
                zgyro = event.values[2];

                sensorManager.getRotationMatrixFromVector(rMatrix, event.values);
                sensorManager.getOrientation(rMatrix, event.values);

//                Log.i("MovementRecog", String.format("x: %.3f\t y: %.3f\t z: %.3f", event.values[0]*(180f/Math.PI),
//                        event.values[1]*(180f/Math.PI),
//                        event.values[2]*(180f/Math.PI)));
            }
            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                sensorManager.getRotationMatrixFromVector(rMatrix, event.values);
                sensorManager.getOrientation(rMatrix, event.values);

                xgyro = (float) (event.values[0]*(180f/Math.PI));
                ygyro = (float) (event.values[1]*(180f/Math.PI));
                zgyro = (float) (event.values[2]*(180f/Math.PI));

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

        public float getXAngle() { return xgyro; }
        public float getYAngle() { return ygyro; }
        public float getZAngle() { return zgyro; }
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

                }catch(Exception e){
                    Log.e(Movement.APP_TAG, "Unable to write to file system.");
                }

            }
        }

        public void writeMovement(Movement m){
            try {
                printWriter = new PrintWriter(new FileWriter(file, true));
                if(isExternalStorageWritable()){
                    printWriter.append(m.toString());
                }
            }catch(Exception e){
                Log.e(Movement.APP_TAG, "Unable to write to file.");
            }
            printWriter.close();
        }

        public List<Movement> retrieveRecentMovements(){
            List<Movement> movements = new LinkedList<Movement>();
            try {
                Scanner scanner = new Scanner(file);
                Pattern pattern = Pattern.compile("(\\D+) (.+) - (.+)");
                Matcher matcher;
                DateTimeFormatter formatter = Movement.getDateTimeFormatter();
                while(scanner.hasNext()){
                    String line = scanner.nextLine();
                    matcher = pattern.matcher(line);
                    if(matcher.find()){
                        String type = matcher.group(1);
                        String start = matcher.group(2);
                        String end = matcher.group(3);

                        Movement m = new Movement(Movement.stringToMovementType(type),
                                formatter.parseDateTime(start), formatter.parseDateTime(end));

                        // only keep most recent 10
                        movements.add(m);
                        if(movements.size() > 10){
                            movements.remove(0);
                        }
                    }
                }
                Collections.reverse(movements);
            }catch(Exception e){
                Log.e(Movement.APP_TAG, "Could not read file.");
            }
            return movements;
        }

        // Permission methods
        /* Checks if external storage is available to at least read */
        private boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return true;
            }
            return false;
        }

        /* Checks if external storage is available for read and write */
        private boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }
    }
}
