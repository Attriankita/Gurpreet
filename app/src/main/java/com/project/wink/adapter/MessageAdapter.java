package com.project.wink.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.project.wink.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;


public class MessageAdapter extends ArrayAdapter<ParseObject> implements Filterable  {
    private Activity activityContext;
    //    private int layoutResourceId = R.layout.list_row;
    //private LayoutInflater inflater;
    protected ArrayList<ParseObject> userList;
    private LayoutInflater inflater;
    private Bitmap bitmapImage;
    String currentUserId;
    public MessageAdapter(@NonNull Activity context, @NonNull ArrayList<ParseObject> objects) {
        super(context, 0, objects);
        activityContext = context;
        userList = objects;
        final ParseUser user = ParseUser.getCurrentUser();
        currentUserId = user.getObjectId();
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Nullable
    @Override
    public ParseObject getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(ParseObject item) {
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
        final ParseObject user = userList.get(position);

        if (convertView == null){
            inflater = LayoutInflater.from(activityContext);
            convertView = inflater.inflate(R.layout.message_list_item,parent,false);

            holder  = new ViewHolder();
            holder.user_name = (TextView) convertView.findViewById(R.id.user_name);
            holder.subject = (TextView) convertView.findViewById(R.id.subject);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.reqStatus = (TextView) convertView.findViewById(R.id.requestStatus);
            holder.container = (LinearLayout) convertView.findViewById(R.id.container_inner);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        if(user.getString("senderId").equals(currentUserId))
        {
            holder.user_name.setText("To: "+user.getString("receiverName"));

            holder.reqStatus.setText("Status:"+user.getString("requestStatus"));

        }else{

            holder.user_name.setText("From: "+user.getString("senderName"));
            holder.reqStatus.setText("Status:"+user.getString("requestStatus"));

            if(user.getString("status").equals("sent"))
            {
                TextView textView;
                for (int i=0; i<holder.container.getChildCount();i++)
                {
                    View view = holder.container.getChildAt(i);
                    if (view instanceof TextView){
                        textView = (TextView) view;
                        textView.setTextColor(Color.BLUE);
                        textView.setTypeface(textView.getTypeface(), Typeface.BOLD_ITALIC);
                    }
                }
            }
        }
        if(user.getString("requestStatus").equals("Waiting"))
        {
            holder.reqStatus.setTextColor(Color.rgb(255, 102, 0));
        }
        else if(user.getString("requestStatus").equals("Rejected"))
        {
            holder.reqStatus.setTextColor(Color.rgb(255, 51, 0));
        }
        else
        {
            holder.reqStatus.setTextColor(Color.rgb(0, 204, 0));
        }
        Date CurrentString = user.getCreatedAt();
        StringTokenizer tk = new StringTokenizer(String.valueOf(CurrentString));

        String date = tk.nextToken()+" "+tk.nextToken()+" "+tk.nextToken();  // <---  yyyy-mm-dd
        String time = tk.nextToken();
        holder.date.setText(date+"\n"+time);
        holder.subject.setText("Subject: "+user.getString("subject"));


        return convertView;
    }


    private class ViewHolder{

        TextView user_name;
        TextView subject;
        TextView date;
        TextView reqStatus;
        LinearLayout container;
    }

}
