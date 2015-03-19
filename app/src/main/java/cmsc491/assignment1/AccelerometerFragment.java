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
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class AccelerometerFragment extends Fragment implements SensorEventListener {
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private Button mAccelBtn;
    private boolean toggle = true;

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
        mAccelBtn = (Button) view.findViewById(R.id.accelBtn);
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

        mTimer1 = new Runnable(){
            @Override
            public void run() {
                xSeries.appendData(new DataPoint(timer, (double) xaccl), true, 100);
                ySeries.appendData(new DataPoint(timer++, (double) yaccl),true, 100);
                zSeries.appendData(new DataPoint(timer++, (double) zaccl), true, 100);
                mHandler.postDelayed(this, 200);
            }
        };

        mAccelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggle){
                    mAccelBtn.setText("Stop");
                    mHandler.postDelayed(mTimer1,200);
                }else{
                    mAccelBtn.setText("Start");
                    mHandler.removeCallbacks(mTimer1);
                }
                toggle = !toggle;
            }
        });

        return view;
    }

    public void onResume(){
        super.onResume();
    }

    public void onPause(){
        super.onPause();
        mHandler.removeCallbacks(mTimer1);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(Math.abs(xaccl - event.values[0]) > 1)
                xaccl = event.values[0];

            if(Math.abs(yaccl - event.values[1]) > 1)
                yaccl = event.values[1];

            if(Math.abs(zaccl - event.values[2]) > 1)
                zaccl = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
