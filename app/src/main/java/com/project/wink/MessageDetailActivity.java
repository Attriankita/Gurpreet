package com.project.wink;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AddressComponent;
import com.seatgeek.placesautocomplete.model.AddressComponentType;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageDetailActivity extends AppCompatActivity {
    private static final String TAG = "UpdateProfile";
    @BindView(R.id.input_name) TextView _fullName;
    @BindView(R.id.input_phone) TextView _phone;
    @BindView(R.id.input_address) TextView _address;
    @BindView(R.id.input_subject) TextView _subject;
    @BindView(R.id.input_message) TextView _message;
    @BindView(R.id.input_date) TextView _date;
    @BindView(R.id.input_time) TextView _time;
    @BindView(R.id.countdown) TextView _countdown;
    @BindView(R.id.feedbackTxt) TextView _feedbackTxt;
    @BindView(R.id.timer) TextView _timer;
    @BindView(R.id.input_payment) TextView _payment;
    @BindView(R.id.input_feedback) EditText _feedback;

    @BindView(R.id.img) ParseImageView _image;

    @BindView(R.id.btn_feedback) Button _btn_feedback;
    @BindView(R.id.btn_reject) Button _buttonReject;
    @BindView(R.id.btn_accept) Button _buttonAccept;
    @BindView(R.id.btn_cancel) Button _buttonCancel;
    @BindView(R.id.btn_start) Button _buttonStart;
    @BindView(R.id.btn_stop) Button _buttonStop;
    @BindView(R.id.btn_cancel_service) Button _buttonCancelService;

    String userType;
    Number rate;
    Number balance;
    Double balance2;
    String currentUserId;
    String currentUserName;
    String startDate;
    Thread myThread = null;
    Thread timerThread = null;
    SessionManager session;
    String messageId;
    String senderId;

    String currentDateTime;
    String serviceStartTime = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ButterKnife.bind(this);

        toolbar.setTitle("Message Detail");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final ParseUser user = ParseUser.getCurrentUser();
        userType = user.getString("userType");
        rate = user.getNumber("rate");
        currentUserId = user.getObjectId();
        currentUserName = user.getString("fullName");
        balance = user.getNumber("money");
        balance2 = balance.doubleValue();;

        session = new SessionManager(getApplicationContext());
        Intent intent = this.getIntent();
        ParseObject messageData = intent.getParcelableExtra("messageObject");
        messageId = messageData.getObjectId();
        senderId = messageData.getString("senderId");
        startDate = messageData.getString("date") + " " +messageData.getString("time");

        if(messageData.getString("serviceStartTime")!=null)
        {
            serviceStartTime = messageData.getString("serviceStartTime");
        }
        _fullName.setText("From: "+messageData.getString("senderName"));
        _subject.setText("Subject: "+messageData.getString("subject"));
        _message.setText("Message: "+messageData.getString("message"));
        _date.setText("Date: "+messageData.getString("date"));
        _time.setText("Time: "+messageData.getString("time"));
        _address.setText("Address: "+messageData.getString("location"));


        if(messageData.getString("requestStatus").equals("In Progress")  )

        {
            if( userType.equals("Service Provider")) {
                _buttonCancelService.setVisibility(View.VISIBLE);
                _buttonStop.setVisibility(View.VISIBLE);

            }
            _buttonStart.setVisibility(View.GONE);
            _countdown.setVisibility(View.GONE);
            _timer.setVisibility(View.VISIBLE);
            Runnable myRunnableThread = new TimerRunner();
            timerThread= new Thread(myRunnableThread);
            timerThread.start();
        }
        if(messageData.getString("requestStatus").equals("Done"))
        {
            setPayment();
            if(userType.equals("Service Provider"))
            {
                if(messageData.getString("feedbackFromProvider").equals("") || messageData.getString("feedbackFromProvider")==null)
                {
                    _feedback.setVisibility(View.VISIBLE);
                    _btn_feedback.setVisibility(View.VISIBLE);
                }
                else{
                    _feedbackTxt.setText("Feedback From User: "+messageData.getString("feedbackFromUser"));
                }

            }
            if(userType.equals("User"))
            {
                if(messageData.getString("feedbackFromUser").equals("") || messageData.getString("feedbackFromUser")==null)
                {
                    _feedback.setVisibility(View.VISIBLE);

                    _btn_feedback.setVisibility(View.VISIBLE);
                }else{
                    _feedbackTxt.setText("Feedback From Provider: "+messageData.getString("feedbackFromProvider"));
                }
            }

        }

        if(messageData.getString("requestStatus").equals("Accepted"))
        {
            _countdown.setVisibility(View.VISIBLE);

            Runnable myRunnableThread = new CountDownRunner();
            myThread= new Thread(myRunnableThread);
            myThread.start();
            if(userType.equals("Service Provider"))
            {

                _buttonStart.setVisibility(View.VISIBLE);
            }
        }

        if(userType.equals("User") || !messageData.getString("requestStatus").equals("Waiting"))
        {
            _buttonAccept.setVisibility(View.GONE);
            _buttonReject.setVisibility(View.GONE);

        }
        else {
            _buttonAccept.setVisibility(View.VISIBLE);
            _buttonReject.setVisibility(View.VISIBLE);
        }
        if(userType.equals("User") && messageData.getString("requestStatus").equals("Waiting"))
        {
            _buttonCancel.setVisibility(View.VISIBLE);
        }
        ParseFile image = messageData.getParseFile("realImage");
        // Uri dataUri = Uri.parse(image.getUrl());
        if (image != null) {

            _image.setParseFile(image);
            _image.loadInBackground();

        }
        _buttonReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStatus("Rejected");
            }
        });
        _buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTimer("Done");
            }
        });
        _buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               _buttonCancelService.setVisibility(View.VISIBLE);
               _buttonStop.setVisibility(View.VISIBLE);
                _buttonStart.setVisibility(View.GONE);
                _countdown.setVisibility(View.GONE);
                _timer.setVisibility(View.VISIBLE);

                startTimer("In Progress");
             //   myThread.stop();
            }
        });

        _btn_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              sendFeedback();
             //   myThread.stop();
            }
        });
        _buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStatus("Accepted");
            }
        });
        _buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStatus("Cancelled");
            }
        });

        _buttonCancelService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStatus("Cancelled");
            }
        });
      //  getUserDetails();
       // if (checkConnection()) {
        //    serverQuery(senderId);
       // }
    }
    public void setPayment(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Payment");
        query.whereEqualTo("messageId",messageId);
        // Retrieve the object by id
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    String rate = list.get(0).getString("rate");
                    String hours = list.get(0).getString("time");

                    String amount = list.get(0).getString("amount");
                     _payment.setText("Rate: "+rate+"\nTime: "+hours+"\nTotal amount paid:"+amount);
                     _payment.setVisibility(View.VISIBLE);

                } else {
                   // alertDisplayer("Error", e.getMessage());
                }
            }
        });
    }
public void paymentDone(final String endTime) throws java.text.ParseException {
    final ProgressDialog progressDialog = new ProgressDialog(MessageDetailActivity.this,
            R.style.AppTheme_Dark_Dialog);
    progressDialog.setIndeterminate(true);
    progressDialog.setMessage("Making Payment...");
 //   progressDialog.show();

    final String hoursDone = getHours(serviceStartTime,endTime);
    final Double hours = Double.parseDouble(hoursDone);
    Double rate2  = rate.doubleValue();
    final String finalRate = String.valueOf(rate2);
    double roundOff = Math.round(hours*rate2 * 100.0) / 100.0;
    final String finalAmount = String.valueOf(roundOff);
 //   alertDisplayer("Payment Done", time);

        ParseObject click = new ParseObject("Payment");
        click.put("amount", finalAmount);
        click.put("rate", finalRate);
        click.put("status", true);
        click.put("messageId", messageId);
        click.put("time", hoursDone);
        click.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("Payment", "Done successfully");
                    progressDialog.dismiss();
                    updateRates(finalAmount);
                    _buttonAccept.setVisibility(View.GONE);
                    _buttonReject.setVisibility(View.GONE);
                    _buttonCancel.setVisibility(View.GONE);

                    _buttonCancelService.setVisibility(View.GONE);
                    _buttonStop.setVisibility(View.GONE);
                    _timer.setVisibility(View.GONE);
                    setPayment();
                    //  _btn_send.setEnabled(false);
                    alertDisplayer("Payment Done", "Your payment done successfully.Rate:"+finalRate+" Time:"+hoursDone+" Total amount paid:"+finalAmount);
                    //  Toast.makeText(getApplicationContext(), "Send Message", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    alertDisplayer("Payment Failed", e.getMessage());
                    // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void doWork()
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                try
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

                    String currentDateTimeString = simpleDateFormat.format(new Date());
                  //  String newDate = simpleDateFormat.format(startDate);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm");
                    Date convertedDate = dateFormat.parse(startDate);
                    String datee = simpleDateFormat.format(convertedDate);

                    Date date1 = simpleDateFormat.parse(currentDateTimeString);
                    Date date2 = simpleDateFormat.parse(datee);

                    long different = date2.getTime() - date1.getTime();

                    long secondsInMilli = 1000;
                    long minutesInMilli = secondsInMilli * 60;
                    long hoursInMilli = minutesInMilli * 60;
                    long daysInMilli = hoursInMilli * 24;

                    long elapsedDays = different / daysInMilli;
                    different = different % daysInMilli;

                    long elapsedHours = different / hoursInMilli;
                    different = different % hoursInMilli;

                    long elapsedMinutes = different / minutesInMilli;
                    different = different % minutesInMilli;

                    long elapsedSeconds = different / secondsInMilli;

                    String difference = "";

                    if(elapsedDays<=0 && elapsedHours<=0 && elapsedMinutes<=0 && elapsedSeconds<=0)
                    {
                        if(elapsedDays>0) {
                            difference = "Time UP\n"+Math.abs(elapsedDays) + " Days " + Math.abs(elapsedHours) + " Hours " + Math.abs(elapsedMinutes) + " Minutes " + Math.abs(elapsedSeconds)+" Seconds";
                        }else{
                            difference = "Time UP\n"+Math.abs(elapsedHours) + " Hours " + Math.abs(elapsedMinutes) + " Minutes " + Math.abs(elapsedSeconds)+" Seconds";
                        }
                    }
                    else{
                        if(elapsedDays>0) {
                            difference = "Time Remaining\n"+elapsedDays + " Days " + elapsedHours + " Hours " + elapsedMinutes + " Minutes " + elapsedSeconds+" Seconds";
                        }else{
                            difference = "Time Remaining\n"+elapsedHours + " Hours " + elapsedMinutes + " Minutes " + elapsedSeconds+" Seconds";
                        }
                    }
                    _countdown.setText(difference);

                }
                catch (Exception e)
                {

                }
            }
        });
    }
    public String getHours(String start,String end) throws java.text.ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

        //  String newDate = simpleDateFormat.format(startDate);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date convertedDate = dateFormat.parse(end);
        Date stdt = dateFormat.parse(start);

        String endDt = simpleDateFormat.format(convertedDate);
        String startDt = simpleDateFormat.format(stdt);

        Date date1 = simpleDateFormat.parse(startDt);
        Date date2 = simpleDateFormat.parse(endDt);
        double different = date2.getTime() - date1.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
/*
        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        */
        different = ((different/1000)/60)/60;
        different = Math.round(different*100.0)/100.0;
        String difference = String.valueOf(different);
    return difference;

    }
    class CountDownRunner implements Runnable
    {
        // @Override
        public void run()
        {
            while(!Thread.currentThread().isInterrupted())
            {
                try
                {
                    doWork();
                    Thread.sleep(1000); // Pause of 1 Second
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
                catch(Exception e)
                {
                }
            }
        }
    }
    public void doTimer()
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                try
                {

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

                    String currentDateTimeString = simpleDateFormat.format(new Date());
                    //  String newDate = simpleDateFormat.format(startDate);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date convertedDate;
                    if(serviceStartTime.equals(null) || serviceStartTime.equals(""))
                    {
                         convertedDate = dateFormat.parse(currentDateTime);
                    }
                    else{
                         convertedDate = dateFormat.parse(serviceStartTime);
                    }

                    String datee = simpleDateFormat.format(convertedDate);

                    Date date1 = simpleDateFormat.parse(currentDateTimeString);
                    Date date2 = simpleDateFormat.parse(datee);

                    long different = date1.getTime() - date2.getTime();

                    long secondsInMilli = 1000;
                    long minutesInMilli = secondsInMilli * 60;
                    long hoursInMilli = minutesInMilli * 60;
                    long daysInMilli = hoursInMilli * 24;

                    long elapsedDays = different / daysInMilli;
                    different = different % daysInMilli;

                    long elapsedHours = different / hoursInMilli;
                    different = different % hoursInMilli;

                    long elapsedMinutes = different / minutesInMilli;
                    different = different % minutesInMilli;

                    long elapsedSeconds = different / secondsInMilli;

                    String difference = "";

                    if(elapsedDays<=0 && elapsedHours<=0 && elapsedMinutes<=0 && elapsedSeconds<=0)
                    {
                        if(elapsedDays>0) {
                            difference = "Time UP\n"+Math.abs(elapsedDays) + " Days " + Math.abs(elapsedHours) + " Hours " + Math.abs(elapsedMinutes) + " Minutes " + Math.abs(elapsedSeconds)+" Seconds";
                        }else{
                            difference = "Time UP\n"+Math.abs(elapsedHours) + " Hours " + Math.abs(elapsedMinutes) + " Minutes " + Math.abs(elapsedSeconds)+" Seconds";
                        }
                    }
                    else{
                        if(elapsedDays>0) {
                            difference = "Service Time\n"+elapsedDays + " Days " + elapsedHours + " Hours " + elapsedMinutes + " Minutes " + elapsedSeconds+" Seconds";
                        }else{
                            difference = "Service Time\n"+elapsedHours + " Hours " + elapsedMinutes + " Minutes " + elapsedSeconds+" Seconds";
                        }
                    }

                _timer.setText(difference);
                }
                catch (Exception e)
                {

                }
            }
        });
    }
    class TimerRunner implements Runnable
    {
        // @Override
        public void run()
        {
            while(!Thread.currentThread().isInterrupted())
            {
                try
                {
                    doTimer();
                    Thread.sleep(1000); // Pause of 1 Second
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
                catch(Exception e)
                {
                }
            }
        }
    }

    public void sendFeedback(){
        final ProgressDialog progressDialog = new ProgressDialog(MessageDetailActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sending Feedback...");
        progressDialog.show();
final String txt = _feedback.getText().toString();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("objectId",messageId);
        // Retrieve the object by id
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    if(userType.equals("Service Provider"))
                    {
                        list.get(0).put("feedbackFromProvider",txt);
                    }
                    else{
                        list.get(0).put("feedbackFromUser",txt);
                    }

                    list.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null) {
                                // Log.i("Message", "Sent correctly");
                                progressDialog.dismiss();
                                _feedback.setVisibility(View.GONE);
                                _btn_feedback.setVisibility(View.GONE);
                                //  _btn_send.setEnabled(false);
                                alertDisplayer("Feedback Sent", "Your reply sent successfully");
                                //  Toast.makeText(getApplicationContext(), "Send Message", Toast.LENGTH_SHORT).show();

                            }
                            else{
                                progressDialog.dismiss();
                                alertDisplayer("Feedback Failed", e.getMessage());
                                // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    alertDisplayer("Error", e.getMessage());
                }
            }
        });

    }
    public void updateStatus(final String reqStatus){
        final ProgressDialog progressDialog = new ProgressDialog(MessageDetailActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sending Reply...");
        progressDialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("objectId",messageId);
        // Retrieve the object by id
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());
                    list.get(0).put("requestStatusTime",formattedDate);
                    list.get(0).put("requestStatus",reqStatus);
                    list.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null) {
                                // Log.i("Message", "Sent correctly");
                                progressDialog.dismiss();
                                _buttonAccept.setVisibility(View.GONE);
                                _buttonReject.setVisibility(View.GONE);
                                _buttonCancel.setVisibility(View.GONE);

                                _buttonCancelService.setVisibility(View.GONE);
                                _buttonStop.setVisibility(View.GONE);
                                _timer.setVisibility(View.GONE);
                                //  _btn_send.setEnabled(false);
                                alertDisplayer("Message Sent", "Your reply sent successfully");
                                //  Toast.makeText(getApplicationContext(), "Send Message", Toast.LENGTH_SHORT).show();

                            }
                            else{
                                progressDialog.dismiss();
                                alertDisplayer("Updation Failed", e.getMessage());
                                // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    alertDisplayer("Error", e.getMessage());
                }
            }
        });

    }
    public void endTimer(final String reqStatus){


        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String formattedDate = df.format(c.getTime());
        currentDateTime = formattedDate;

        final ProgressDialog progressDialog = new ProgressDialog(MessageDetailActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sending Reply...");
        progressDialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("objectId",messageId);
        // Retrieve the object by id
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    final String formattedDate = df.format(c.getTime());
                    list.get(0).put("serviceEndTime",formattedDate);
                    list.get(0).put("requestStatus",reqStatus);
                    list.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null) {
                                // Log.i("Message", "Sent correctly");
                                progressDialog.dismiss();
                                try {
                                    paymentDone(formattedDate);
                                } catch (java.text.ParseException e1) {
                                    e1.printStackTrace();
                                }
                                //  _btn_send.setEnabled(false);
                                // alertDisplayer("Message Sent", "Your reply sent successfully");
                                //  Toast.makeText(getApplicationContext(), "Send Message", Toast.LENGTH_SHORT).show();

                            }
                            else{
                                progressDialog.dismiss();
                                alertDisplayer("Updation Failed", e.getMessage());
                                // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    alertDisplayer("Error", e.getMessage());
                }
            }
        });
    }
    public void startTimer(final String reqStatus){


        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        currentDateTime = formattedDate;

        final ProgressDialog progressDialog = new ProgressDialog(MessageDetailActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sending Reply...");
        progressDialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.whereEqualTo("objectId",messageId);
        // Retrieve the object by id
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());
                    list.get(0).put("serviceStartTime",formattedDate);
                    list.get(0).put("requestStatus",reqStatus);
                    list.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null) {
                                // Log.i("Message", "Sent correctly");
                                progressDialog.dismiss();
                                Runnable myRunnableThread = new TimerRunner();
                                timerThread= new Thread(myRunnableThread);
                                timerThread.start();
                                //  _btn_send.setEnabled(false);
                               // alertDisplayer("Message Sent", "Your reply sent successfully");
                                //  Toast.makeText(getApplicationContext(), "Send Message", Toast.LENGTH_SHORT).show();

                            }
                            else{
                                progressDialog.dismiss();
                                alertDisplayer("Updation Failed", e.getMessage());
                                // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    alertDisplayer("Error", e.getMessage());
                }
            }
        });
    }
    public boolean checkConnection(){   //method for checking network connection
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    private void serverQuery(String senderId) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId",senderId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, @Nullable ParseException e) {
                if (e == null){
                    if(list!=null && !list.isEmpty())
                    {
                       _address.setText("Address: "+list.get(0).getString("street")+","+list.get(0).getString("city")+"\n"+list.get(0).getString("state")+" "+list.get(0).getString("postalCode")+","+list.get(0).getString("country"));


                    }
                    else{
                        Toast.makeText(getApplicationContext(), "List Empty",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    alertDisplayer("Error", e.getMessage());
                }
            }
        });
    }
    public void updateRates(final String finalAmmount){

        Double finalAmt = Double.valueOf(finalAmmount);
        Double prAmt = Double.valueOf((Double) balance2);
        final Double finalPrAmount = prAmt + finalAmt;
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        ParseUser user = ParseUser.getCurrentUser();
                        user.put("money",finalPrAmount);

                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {

                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("objectId", senderId);
                                    params.put("amount", finalAmmount);
                                    ParseCloud.callFunctionInBackground("updateAmount", params, new FunctionCallback<Object>() {


                                        @Override
                                        public void done(Object mapObject, ParseException e) {
                                            if (e == null) {

                                            }
                                            else {
                                                // Something went wrong
                                            }
                                        }
                                    });


                                } else {

                                }
                            }
                        });



                    }
                }, 3000);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
     //   getMenuInflater().inflate(R.menu.delete_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }




    void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(MessageDetailActivity.this)
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
