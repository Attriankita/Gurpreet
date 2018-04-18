package com.project.wink;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.project.wink.adapter.MessageAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class ViewMessagesActivity extends Fragment {

    SessionManager session;
    public MessageAdapter mAdapter;
    protected ArrayList<ParseObject> messageArrayList;
    final Context context = getContext();
    @Nullable
    private ListView listView;
    private FragmentActivity myContext;
    TextView userFullName;
    String userType;
    String currentUserId;
    String currentUserName;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.view_messages, container, false);

        messageArrayList = new ArrayList<>();
        listView = view.findViewById(R.id.messages_list);
        mAdapter = new MessageAdapter(getActivity(), messageArrayList);
        listView.setAdapter(mAdapter);

        final ParseUser user = ParseUser.getCurrentUser();
        userType = user.getString("userType");
        currentUserId = user.getObjectId();
        currentUserName = user.getString("fullName");

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub

                deleteConfirmation(messageArrayList.get(pos).getObjectId(),pos);

                return true;
            }
        });

        Bundle bundle=getArguments();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(),messageArrayList.get(position).getObjectId(),Toast.LENGTH_SHORT).show();
                long viewId = view.getId();
                if(userType.equals("Service Provider")) {
                    updateMessageCounts(messageArrayList.get(position).getObjectId());
                }
                Intent intent = new Intent(getActivity(), MessageDetailActivity.class);
                intent.putExtra("messageObject",messageArrayList.get(position));
                startActivity(intent);
            }
        });

        if (checkConnection()) {
            serverQuery(currentUserId);
        }
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return view;

    }
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }
    public void deleteConfirmation(final String objectId,final Integer position){
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Request")
                .setMessage("Do you really want to delete this request?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteMessage(objectId,position);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void deleteMessage(final String objectId,final Integer position){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Deleting Request...");
        progressDialog.show();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
                        query.whereEqualTo("objectId",objectId);
                        query.findInBackground(new FindCallback<ParseObject>() {

                            @Override
                            public void done(List<ParseObject> results, ParseException e) {

                                if (e == null) {
                                    for (ParseObject reply : results) {
                                        reply.deleteInBackground();

                                    }
                                   // userTempList.remove(userArrayList.get(position));
                                    mAdapter.remove(messageArrayList.get(position));
                                    mAdapter.notifyDataSetChanged();
                                    assert listView != null;
                                    listView.invalidateViews();
                                    progressDialog.dismiss();
                                    // Update user login session
                                    alertDisplayer("Request Delete", "Your Request deleted successfully");
                                } else {
                                    progressDialog.dismiss();
                                    alertDisplayer("Deletion Failed", e.getMessage());
                                }

                            }
                        });
                    }
                }, 3000);
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
    @Override
    public void onResume() {
        super.onResume();
        serverQuery(currentUserId);

    }
    public boolean onFragmentKeyDown() {
        new ViewMessagesActivity();
        return false;
    }
    protected void serverQuery(String category) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("receiverId",currentUserId);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Message");
        query.whereEqualTo("senderId",currentUserId);

      //ParseQuery<ParseObject> finalQuery = ParseQuery.getQuery("Message");

        ParseQuery<ParseObject>[] photoQueries = new ParseQuery[]{query, query2};
        ParseQuery<ParseObject> finalQuery = ParseQuery.or(Arrays.asList(photoQueries));
        finalQuery.orderByDescending("createdAt");
        finalQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    if(list!=null && !list.isEmpty())
                    {
                        messageArrayList.clear();
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
    public void updateMessageCounts(final String messageId) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("objectId", messageId);
        // Retrieve the object by id
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null) {
                    if(list.get(0).getString("status").equals("sent")) {
                        list.get(0).put("status", "seen");
                        list.get(0).saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {

                                } else {

                                }
                            }
                        });
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
        getActivity().setTitle("Message");

    }

}