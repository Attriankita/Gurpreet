package com.project.wink.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.parse.ParseUser;
import com.project.wink.R;

import java.util.ArrayList;
import java.util.List;


public class ProvidersAdapter extends ArrayAdapter<ParseUser> implements Filterable  {
    private Activity activityContext;
    //    private int layoutResourceId = R.layout.list_row;
    //private LayoutInflater inflater;
    protected ArrayList<ParseUser> userList;
    private LayoutInflater inflater;
    private Bitmap bitmapImage;
    private List<ParseUser> worldpopulationlist = null;
    public ProvidersAdapter(@NonNull Activity context, @NonNull ArrayList<ParseUser> objects) {
        super(context, 0, objects);
        activityContext = context;
        userList = objects;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Nullable
    @Override
    public ParseUser getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(ParseUser item) {
        return super.getPosition(item);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        final ParseUser user = userList.get(position);

        if (convertView == null){
            inflater = LayoutInflater.from(activityContext);
            convertView = inflater.inflate(R.layout.service_provider_list_item,parent,false);

            holder  = new ViewHolder();
            holder.providerName = (TextView) convertView.findViewById(R.id.providerName);
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.rate = (TextView) convertView.findViewById(R.id.rate);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.providerName.setText(user.getString("fullName"));
        holder.rate.setText("$"+user.getNumber("rate")+"/hr");
        holder.email.setText(user.getUsername());
        holder.address.setText(user.getString("street")+","+user.getString("city")+"\n"+user.getString("state")+","+user.getString("country"));

        ParseUser userDetails = ParseUser.getCurrentUser();
        float currentLat = Float.parseFloat(userDetails.getString("latitude"));
        float currentLng = Float.parseFloat(userDetails.getString("longitude"));

        float userLng = Float.parseFloat(user.getString("longitude"));
        float userLat = Float.parseFloat(user.getString("latitude"));
        String distance = meterDistanceBetweenPoints(currentLat,currentLng,userLat,userLng);
        holder.distance.setText("Distance:\n"+distance+" km");

        return convertView;
    }
    private String meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return String.format("%.2f", (6366000 * tt)/1000);
    }
    private class ViewHolder{

        TextView providerName;
        TextView email;
        TextView address;
        TextView rate;
        TextView distance;
    }

}
