package com.project.wink.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.project.wink.adapter.CategoriesAdapter;
import com.project.wink.R;
import com.project.wink.ServiceProvidersListActivity;

import java.util.ArrayList;
import java.util.List;


public class HomeMainUser extends Fragment {
    private CategoriesAdapter mAdapter;
    private ArrayList<ParseObject> categoryArrayList;
    @Nullable
    private ListView listView;
    private FragmentActivity myContext;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.home_main_user, container, false);
        categoryArrayList = new ArrayList<>();
        listView = view.findViewById(R.id.category_list);
        mAdapter = new CategoriesAdapter(getActivity(), categoryArrayList);
        listView.setAdapter(mAdapter);
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(),userArrayList.get(position).getObjectId(),Toast.LENGTH_SHORT).show();
                long viewId = view.getId();
                //creating fragment object
                Fragment fragment = null;
                   // String message = "Full Name: " + userArrayList.get(position).get("fullName") + "\nEmail: " + userArrayList.get(position).get("username") + "\nPhone: " + userArrayList.get(position).get("phone")+"\nAddress:"+userArrayList.get(position).get("street")+","+userArrayList.get(position).get("city")+","+userArrayList.get(position).get("state")+" "+userArrayList.get(position).get("postalCode")+","+userArrayList.get(position).get("country");
                fragment = new ServiceProvidersListActivity();
                //replacing the fragment
                if (fragment != null) {
                    FragmentTransaction ft = myContext.getSupportFragmentManager().beginTransaction();
                    Bundle args = new Bundle();
                    args.putString("Category", (String) categoryArrayList.get(position).get("name"));
                    fragment.setArguments(args);
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                }

            }
        });
        if (checkConnection()) {
            serverQuery();
        }



        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return view;
    }
    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }
    public boolean checkConnection(){   //method for checking network connection
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    private void serverQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Categories");
        query.orderByAscending("name");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    if(list!=null && !list.isEmpty())
                    {
                        categoryArrayList.clear();
                        for (int i = 0; i < list.size(); i++) {
                            mAdapter.add(list.get(i));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(getContext(), "List Empty",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    alertDisplayer("Error", e.getMessage());
                }
            }
        });
    }
    void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.Theme_AppCompat_Dialog))
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Services");
    }
}