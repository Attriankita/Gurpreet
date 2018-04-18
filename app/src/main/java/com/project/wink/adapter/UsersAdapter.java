package com.project.wink.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.project.wink.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class UsersAdapter extends ArrayAdapter<ParseUser> implements Filterable {
    private Activity activityContext;
    //    private int layoutResourceId = R.layout.list_row;
    //private LayoutInflater inflater;
    protected ArrayList<ParseUser> userList;
    private LayoutInflater inflater;
    private Bitmap bitmapImage;
    private List<ParseUser> worldpopulationlist = null;
    public UsersAdapter(@NonNull Activity context, @NonNull ArrayList<ParseUser> objects) {
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
            convertView = inflater.inflate(R.layout.admin_list_item,parent,false);

            holder  = new ViewHolder();
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.fullName = (TextView) convertView.findViewById(R.id.fullName);
            holder.userType = (TextView) convertView.findViewById(R.id.userType);
            holder.btnBlockUnblock = (Button) convertView.findViewById(R.id.btn_block_unblock);
            holder.btnDelete = (Button) convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.email.setText(user.getUsername());
        holder.fullName.setText(user.getString("fullName"));
        holder.userType.setText("Type: " +user.getString("userType"));
        if(user.getBoolean("status"))
        {
            holder.btnBlockUnblock.setText("Unblock");
        }
        else{
            holder.btnBlockUnblock.setText("Block");
        }
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });
        holder.btnBlockUnblock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });

        return convertView;
    }

    private class ViewHolder{

        TextView email;
        TextView fullName;
        TextView userType;
        Button btnBlockUnblock;
        Button btnDelete;
    }

}
