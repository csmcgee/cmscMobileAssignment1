package cmsc491.assignment1;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class AccelerometerFragment extends Fragment implements SensorEventListener {
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    private float xaccl= 0, yaccl = 0, zaccl = 0;

    private Double timer = new Double(0);

    private LineGraphSeries<DataPoint> xSeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> ySeries = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> zSeries = new LineGraphSeries<DataPoint>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_accelerometer, container, false);
        Context context = view.getContext();
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        GraphView graph = (GraphView) view.findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setMinY(-25);
        graph.getViewport().setMaxY(25);
        graph.getViewport().setScrollable(true);
        xSeries.setColor(Color.RED);
        ySeries.setColor(Color.BLUE);
        zSeries.setColor(Color.GREEN);
        graph.addSeries(xSeries);
        graph.addSeries(ySeries);
        graph.addSeries(zSeries);
        return view;
    }

    public void onResume(){
        super.onResume();

        mTimer1 = new Runnable(){
            @Override
            public void run() {
                xSeries.appendData(new DataPoint(timer, (double) xaccl), true, 30);
                ySeries.appendData(new DataPoint(timer++, (double) yaccl),true, 30);
                zSeries.appendData(new DataPoint(timer++, (double) zaccl), true, 30);
                mHandler.postDelayed(this, 100);
            }
        };

        mHandler.postDelayed(mTimer1, 100);
    }

    public void onPause(){
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
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
}
