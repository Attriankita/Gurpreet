package com.project.wink;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.project.wink.adapter.MessageAdapter;
import com.project.wink.admin.HomeMainAdmin;
import com.project.wink.user.HomeMainUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tgio.parselivequery.BaseQuery;
import tgio.parselivequery.LiveQueryClient;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.Subscription;
import tgio.parselivequery.interfaces.OnListener;

import static android.support.v4.view.MenuItemCompat.*;
import static com.project.wink.Constants.DBConstants.*;

public class HomeNavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SessionManager session;
    String userType;
    String currentUserId;
    TextView messageCountView;
    int mMessageCount = 0;
    //declare a static variable
    public static MessageAdapter adapter;
    DBHelper mDbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initialize session
        session = new SessionManager(getApplicationContext());
        // Inialize DB Helper
        mDbHelper = new DBHelper(getApplicationContext());
        // Drawer Navigation
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Get current User
        final ParseUser user = ParseUser.getCurrentUser();
        userType = user.getString("userType");
        currentUserId = user.getObjectId();

        // Start Live Query to update messages
        LiveQueryClient.init("wss://wink.back4app.io", "qwVtlQmEIrIhLda5IJPtUXpFfnMzz7mZptW7jmXL", true);
        LiveQueryClient.connect();

        // Subscription for provider
        final Subscription sub = new BaseQuery.Builder("Message")
                .where("receiverId", currentUserId)
                .addField("status")
                .addField("receiverName")
                .addField("senderName")
                .addField("senderId")
                .addField("createdAt")
                .addField("subject")
                .build()
                .subscribe();

        // Subscription for User
        final Subscription subUser = new BaseQuery.Builder("Message")
                .where("senderId", currentUserId)
                .addField("status")
                .addField("receiverName")
                .addField("senderName")
                .addField("senderId")
                .addField("createdAt")
                .addField("subject")
                .build()
                .subscribe();


        sub.on(LiveQueryEvent.CREATE, new OnListener() {
            @Override
            public void on(final JSONObject object1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ViewMessagesActivity  f = (ViewMessagesActivity) getSupportFragmentManager().findFragmentByTag("message");
                        try {
                            JSONObject jObj = object1.getJSONObject("object");
                            JSONObject object = jObj;

                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        if(checkUserExists(TABLE_NAME,COLUMN_NAME_OBJECT_ID,currentUserId))
                        {
                            if(userType.equals("Service Provider")) {
                                updateMessageTable(); // Update Message Table
                            }
                        }
                        else{
                            insertMessageTable();
                        }

                    }
                });
            }
        });
        sub.on(LiveQueryEvent.UPDATE, new OnListener() {
            @Override
            public void on(final JSONObject object1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ViewMessagesActivity  f = (ViewMessagesActivity) getSupportFragmentManager().findFragmentByTag("message");
                        try {
                            JSONObject jObj = object1.getJSONObject("object");
                            JSONObject object = jObj;

                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        if(checkUserExists(TABLE_NAME,COLUMN_NAME_OBJECT_ID,currentUserId))
                        {
                            if(userType.equals("Service Provider"))
                            {
                                serverQuery(currentUserId); // Update Message Table

                            }


                        }
                        else{
                           // insertMessageTable();
                        }

                    }
                });
            }
        });

        // Update on user
        subUser.on(LiveQueryEvent.UPDATE, new OnListener() {
            @Override
            public void on(final JSONObject object1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ViewMessagesActivity  f = (ViewMessagesActivity) getSupportFragmentManager().findFragmentByTag("message");
                        try {
                            JSONObject jObj = object1.getJSONObject("object");
                            JSONObject object = jObj;

                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        if(checkUserExists(TABLE_NAME,COLUMN_NAME_OBJECT_ID,currentUserId))
                        {
                            if(userType.equals("User")) {
                                updateMessageTable(); // Update Message Table
                            }
                        }
                        else{
                            if(userType.equals("User")) {
                                insertMessageTable();
                            }

                        }

                    }
                });
            }
        });
        //add this line to display menu1 when the activity is loaded
        displaySelectedScreen(R.id.nav_home);
    }

    public boolean checkConnection(){   //method for checking network connection
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    // Update Message Table
    public void insertMessageTable(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_OBJECT_ID,currentUserId);
        values.put(COLUMN_NAME_COUNT_MESSAGES,1);

        long id = db.insert(TABLE_NAME, null , values);

        updateMessagesOnIcon(); // Update on icon
    }
    // Update Message Table
    public void updateMessageTable(){

        int newVal = countMessages()+1;
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(COLUMN_NAME_COUNT_MESSAGES, newVal);

        String where= COLUMN_NAME_OBJECT_ID+"= " + "\"" + currentUserId + "\"";
        try{

            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            int ss= db.update(TABLE_NAME, dataToInsert, where, null);

            updateMessagesOnIcon(); // Update on icon
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }


    }

    // Update Message Table
    public void updateMessageTableFull(int count){

        int newVal = count;
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(COLUMN_NAME_COUNT_MESSAGES, newVal);

        String where= COLUMN_NAME_OBJECT_ID+"= " + "\"" + currentUserId + "\"";
        try{

            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            int ss= db.update(TABLE_NAME, dataToInsert, where, null);

            updateMessagesOnIcon(); // Update on icon
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }


    }
    // Update messages on icon
    public void updateMessagesOnIcon(){

        mMessageCount = countMessages();

        setupBadge();
    }
    // Count messages in local database
    public int countMessages(){

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
        int m =0;
        String column1="";
        if (c.moveToFirst()){
            do {
                m++;
                // Passing values
                 column1 = c.getString(2);
                // Do something Here with values
            } while(c.moveToNext());
        }
        c.close();
    return Integer.parseInt(column1);
    }
    // Get User Existed or not
    public boolean checkUserExists(String TableName, String dbfield, String fieldValue) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection = dbfield + " = ?";
        String[] selectionArgs = { fieldValue };
        String[] projection = {
                _ID,
                COLUMN_NAME_OBJECT_ID,
                COLUMN_NAME_COUNT_MESSAGES
        };
        String sortOrder =
               _ID + " DESC";
        Cursor cursor = db.query(
                TableName,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        if(cursor.getCount()>0)
        {
            return true;
        }
        cursor.close();
        return false;
    }
    private void setupBadge() {
        if (messageCountView != null) {
          //  Toast.makeText(getApplicationContext(),String.valueOf(mMessageCount),Toast.LENGTH_SHORT).show();
            if (mMessageCount == 0) {
                if (messageCountView.getVisibility() != View.GONE) {
                    messageCountView.setVisibility(View.GONE);
                }
            } else {
                messageCountView.setText(String.valueOf(Math.min(mMessageCount, 99)));
                if (messageCountView.getVisibility() != View.VISIBLE) {
                    messageCountView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_navigation, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_cart);

        View actionView = getActionView(menuItem);
        messageCountView = (TextView) actionView.findViewById(R.id.cart_badge);

        if (checkConnection()) {
            serverQuery(currentUserId);

        }
        else{
            updateMessagesOnIcon();
        }

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });
        return true;
    }
    private void serverQuery(String currentUserId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("status","sent");
        query.whereEqualTo("receiverId",currentUserId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    if(list!=null && !list.isEmpty())
                    {
                        updateMessageTableFull(list.size());
                    }
                    else{
                        updateMessageTableFull(0);
                    }
                } else {
                   // alertDisplayer("Error", e.getMessage());
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.action_cart) {
            mMessageCount = 0;
            setupBadge();
            fragment = new ViewMessagesActivity();
            //replacing the fragment
            if (fragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment,"message");
                ft.commit();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.getItemId());
        return true;
    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;
        String tag = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_home:
                if(userType.equals("Admin"))
                {
                    fragment = new HomeMainAdmin();
                    tag = "adminHome";
                }else if(userType.equals("User")){
                    fragment = new HomeMainUser();
                    tag = "userHome";
                }
                else{
                    fragment = new ViewMessagesActivity();
                    tag = "providerHome";
                }

                break;
            case R.id.nav_myaccount:
                fragment = new MyAccount();
                tag = "myAccount";
                break;
            case R.id.nav_legal:
                Intent intent = new Intent(getApplicationContext(), LegalActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                session.logoutUser();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment,tag);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

}
