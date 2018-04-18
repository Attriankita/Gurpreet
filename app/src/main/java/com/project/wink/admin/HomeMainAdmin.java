package com.project.wink.admin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.project.wink.R;
import com.project.wink.adapter.UsersAdapter;

import java.util.Locale;
import java.util.Map;

public class HomeMainAdmin extends Fragment{

    private UsersAdapter mAdapter;
    private ArrayList<ParseUser> userArrayList;
    @Nullable
    private ListView listView;
    final Context context = getContext();
    private Button button;
    EditText etSearch;
    ArrayList<ParseUser>  userTempList;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.home_main_admin, container, false);

        userArrayList = new ArrayList<>();
        listView = view.findViewById(R.id.user_list);
        etSearch = view.findViewById(R.id.search_box);
        mAdapter = new UsersAdapter(getActivity(),userArrayList);
        listView.setAdapter(mAdapter);
       // mAdapter.addBackground(SwipeDirection.DIRECTION_FAR_LEFT,R.layout.row_bg_left_far)

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(),userArrayList.get(position).getObjectId(),Toast.LENGTH_SHORT).show();
                long viewId = view.getId();

                if (viewId == R.id.btn_delete) {
                    deleteConfirmation(userArrayList.get(position).getObjectId() ,position);
                }else if(viewId == R.id.btn_block_unblock){
                    updateUser(userArrayList.get(position).getObjectId(),userArrayList.get(position).getBoolean("status"),view);

                }
                else {
                    String message = "Full Name: " + userArrayList.get(position).get("fullName") + "\nEmail: " + userArrayList.get(position).get("username") + "\nPhone: " + userArrayList.get(position).get("phone")+"\nAddress:"+userArrayList.get(position).get("street")+","+userArrayList.get(position).get("city")+","+userArrayList.get(position).get("state")+" "+userArrayList.get(position).get("postalCode")+","+userArrayList.get(position).get("country");
                    alertDisplayer("User Details", message);
                }
            }
        });


// Capture Text in EditText
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                String text = etSearch.getText().toString().toLowerCase(Locale.getDefault());
                filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                // TODO Auto-generated method stub
            }
        });

        if (checkConnection()) {
            serverQuery();
        }
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        serverQuery();

    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        userArrayList.clear();
        if (charText.length() == 0) {
            mAdapter.addAll(userTempList);
        }
        else
        {

            for (ParseUser wp : userTempList)
            {
                if (wp.getString("fullName").toLowerCase(Locale.getDefault()).contains(charText) || wp.getUsername().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    mAdapter.add(wp);
                }
            }


        }
        mAdapter.notifyDataSetChanged();
    }
    public void updateUser(final String objectId, final boolean user_status, final View view){

        final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Changing Status...");
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        String u_status = "True";
                        String btnText = "Unblock";
                        if(user_status)
                        {
                             u_status = "False";
                            btnText = "Block";
                        }
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("objectId", objectId);
                        params.put("user_status", u_status);
                        ParseCloud.callFunctionInBackground("updateUserStatus", params, new FunctionCallback<Object>() {


                            @Override
                            public void done(Object mapObject, ParseException e) {
                                if (e == null) {

                                }
                                else {
                                    // Something went wrong
                                }
                            }
                        });

                        Button f = view.findViewById(R.id.btn_block_unblock);
                        f.setText(btnText);



                    }
                }, 3000);


    }
    public boolean checkConnection(){   //method for checking network connection
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Wink Admin");
    }

    private void serverQuery() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo( "userType", "Admin" );
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
                            mAdapter.notifyDataSetChanged();
                            userTempList = (ArrayList<ParseUser>)userArrayList.clone();
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
    public void deleteConfirmation(final String objectId, final Integer position){
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Do you realy want to delete this user ?"
                )
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteUser(objectId, position);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void deleteUser(final String objectId, final Integer position){
        final ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Deleting Account...");
        progressDialog.show();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog.dismiss();

                        Map<String, String> params = new HashMap<String, String>();
                        params.put("objectId", objectId);
                        ParseCloud.callFunctionInBackground("deleteUserWithId", params, new FunctionCallback<Object>() {

                            @Override
                            public void done(Object mapObject, ParseException e) {
                                if (e == null) {


                                }
                                else {
                                    // Something went wrong
                                }
                            }
                        });
                        alertDisplayer("User deleted","User Deleted Successfully");
                        userTempList.remove(userArrayList.get(position));
                        mAdapter.remove(userArrayList.get(position));
                        mAdapter.notifyDataSetChanged();
                        assert listView != null;
                        listView.invalidateViews();
                    }
                }, 3000);
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

}