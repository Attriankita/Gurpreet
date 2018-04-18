package com.project.wink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.project.wink.adapter.ProvidersAdapter;

import java.util.ArrayList;
import java.util.List;


public class ServiceProvidersListActivity extends Fragment {

    SessionManager session;
    private ProvidersAdapter mAdapter;
    private ArrayList<ParseUser> userArrayList;
    final Context context = getContext();
    @Nullable
    private ListView listView;
    private FragmentActivity myContext;
    TextView userFullName;
    String userType;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.service_providers, container, false);

        userArrayList = new ArrayList<>();
        listView = view.findViewById(R.id.messages_list);
        mAdapter = new ProvidersAdapter(getActivity(),userArrayList);
        listView.setAdapter(mAdapter);
        Bundle bundle=getArguments();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(),userArrayList.get(position).getObjectId(),Toast.LENGTH_SHORT).show();
                long viewId = view.getId();

                Intent intent = new Intent(getActivity(), SendRequestActivity.class);
                intent.putExtra("objectId", userArrayList.get(position).getObjectId());
                intent.putExtra("name", userArrayList.get(position).getString("fullName"));
                startActivity(intent);
            }
        });
        if (checkConnection()) {
            serverQuery(String.valueOf(bundle.getString("Category")));
        }
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return view;

    }
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }


    public void onBackPressed()
    {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }
    public boolean checkConnection(){   //method for checking network connection
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    private void serverQuery(String category) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo( "userType", "Service Provider" );
        query.whereEqualTo( "categories", category );
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, @Nullable ParseException e) {
                if (e == null){
                    if(list!=null && !list.isEmpty())
                    {
                        userArrayList.clear();
                        for (int i = 0; i < list.size(); i++) {
                            mAdapter.add(list.get(i));
                        }
                        notifyDataSetChanged();
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
        getActivity().setTitle("Service Provider");

    }

}