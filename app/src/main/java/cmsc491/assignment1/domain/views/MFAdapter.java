package cmsc491.assignment1.domain.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import cmsc491.assignment1.R;
import cmsc491.assignment1.domain.activityRecog.Movement;

public class MFAdapter extends BaseAdapter {

    private List<Movement> movementFeed = GetMovementData();

    @Override
    public int getCount() {
        return movementFeed.size();
    }

    @Override
    public Object getItem(int position) {
        return movementFeed.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.movement_list_item, parent,false);
        }

        TextView movementType = (TextView) convertView.findViewById(R.id.movementType);
        TextView interval = (TextView) convertView.findViewById(R.id.intervalView);

        Movement movement = movementFeed.get(position);

        movementType.setText(movement.getTypeString());
        interval.setText(movement.getIntervalString());

        return convertView;
    }

    public void updateFeed(){
        movementFeed = GetMovementData();
    }

    /**
     * Data provider method used to generate fake data for UI testing.
     */
    private static List<Movement> GetMovementData(){
    Random rand = new Random();
    ArrayList<Movement> movements = new ArrayList<Movement>();
    for(int i = 0; i < 20; i+=2){
        DateTime startTime;
        DateTime endTime;

        DateTime jDate = new DateTime();
        startTime = jDate.plusMinutes(i);
        endTime = jDate.plusMinutes(i+2);

        Movement.Type type = null;
        switch(rand.nextInt(3)){
            case 0:
                type = Movement.Type.SITTING;
                break;
            case 1:
                type = Movement.Type.WALKING;
                break;
            case 2:
                type = Movement.Type.SLEEPING;
        }

        movements.add(new Movement(type, startTime, endTime));

    }

    Collections.reverse(movements);

    return movements;
}
}
