package cmsc491.assignment1.domain.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import cmsc491.assignment1.R;
import cmsc491.assignment1.domain.activityRecog.Movement;

public class MFAdapter extends BaseAdapter {

    private List<Movement> movementFeed = new ArrayList<Movement>();

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

    public void setMovements(List<Movement> movements){
        movementFeed = movements;
    }
}
