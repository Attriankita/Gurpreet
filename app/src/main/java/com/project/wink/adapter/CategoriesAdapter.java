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
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.project.wink.R;

import java.util.ArrayList;
import java.util.List;


public class CategoriesAdapter extends ArrayAdapter<ParseObject> implements Filterable {
    private Activity activityContext;

    protected ArrayList<ParseObject> categoryList;
    private LayoutInflater inflater;
    private Bitmap bitmapImage;
    public CategoriesAdapter(@NonNull Activity context, @NonNull ArrayList<ParseObject> objects) {
        super(context, 0, objects);
        activityContext = context;
        categoryList = objects;
    }

    @Override
    public int getCount() {
        return categoryList.size();
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
        final ParseObject category = categoryList.get(position);

        if (convertView == null){
            inflater = LayoutInflater.from(activityContext);
            convertView = inflater.inflate(R.layout.category_list_item,parent,false);

            holder  = new ViewHolder();
            holder.categoryName = (TextView) convertView.findViewById(R.id.categoryName);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.categoryName.setText(category.getString("name"));

        return convertView;
    }

    private class ViewHolder{

        TextView categoryName;
    }

}
