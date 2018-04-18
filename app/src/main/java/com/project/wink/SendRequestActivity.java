package com.project.wink;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AddressComponent;
import com.seatgeek.placesautocomplete.model.AddressComponentType;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import tgio.parselivequery.BaseQuery;
import tgio.parselivequery.LiveQueryClient;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.Subscription;
import tgio.parselivequery.interfaces.OnListener;

public class SendRequestActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "SendRequest";
    @BindView(R.id.input_provider_name) TextView _providerName;
    @BindView(R.id.input_date) EditText _date;
    @BindView(R.id.input_time) EditText _time;
    @BindView(R.id.input_location) EditText _location;
    @BindView(R.id.input_note) EditText _message;

    @BindView(R.id.img) ImageView _image;
    @BindView(R.id.input_subject) EditText _subject;
    @BindView(R.id.btn_send) Button _btn_send;
    @BindView(R.id.btn_picture) Button _btn_picture;

    private int year, month, day, hour, min;

    SessionManager session;
    String userType;
    String currentUserId;
    String currentUserName;
    String receiverId;
    String name;
    String path = "";
    private Bitmap imageBitmap;

    AppLocationService appLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Send Request");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        name = getIntent().getStringExtra("name");
        receiverId = getIntent().getStringExtra("objectId");

        final ParseUser user = ParseUser.getCurrentUser();
        userType = user.getString("userType");
        currentUserId = user.getObjectId();
        currentUserName = user.getString("fullName");

        appLocationService = new AppLocationService(
                SendRequestActivity.this);
        Location location = appLocationService
                .getLocation(LocationManager.GPS_PROVIDER);

        //you can hard-code the lat & long if you have issues with getting it
        //remove the below if-condition and use the following couple of lines
        //double latitude = 37.422005;
        //double longitude = -122.084095

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude,
                    getApplicationContext(), new GeocoderHandler());
        } else {
            showSettingsAlert();
        }

        _providerName.setText(name);
        _providerName.setEnabled(false);
        session = new SessionManager(getApplicationContext());
        final Calendar calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);

        _date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view){
                // Get Current Date
                showDialog(999);

            }
        });
        _time.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(final View view){
                // Get Current Time
                showDialog(777);
        }
        });
      //  getUserDetails();
        // Implementing the actions that our app will have
        // Starting with the sendButton functionality
        _btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String messageToSend = _providerName.getText().toString();

              sendRequest();
            }
        });

        _btn_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openImageChooser();
            }
        });
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                SendRequestActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        SendRequestActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            _location.setText(locationAddress);
        }
    }

    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                     path = getRealPathFromURI(getApplicationContext(),selectedImageUri);
                    Log.i(TAG, "Image Path : " + path);
                    // Set the image in ImageView
                    _image.setImageURI(selectedImageUri);
                }
            }
        }
    }
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private byte[] readInFile(String path) throws IOException {
        // TODO Auto-generated method stub
        byte[] data = null;
        File file = new File(path);
        InputStream input_stream = new BufferedInputStream(new FileInputStream(
                file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        data = new byte[16384]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();

    }
    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };
    private void showDate(int year, int month, int day) {
        _date.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }else if(id== 777)
        {
            // set time picker as current time
            return new TimePickerDialog(this,
                    timePickerListener, hour, min,false);
        }
        return null;
    }
    private TimePickerDialog.OnTimeSetListener timePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int selectedHour,
                                      int selectedMinute) {

                    hour = selectedHour;
                    min = selectedMinute;

                    // set current time into textview
                    _time.setText(new StringBuilder().append(pad(hour))
                            .append(":").append(pad(min)));


                }
            };
    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
      //  getMenuInflater().inflate(R.menu.delete_navigation, menu);
        return true;
    }
    public void sendRequest() {
        Log.d(TAG, "Sending Request");

        if (!validate()) {
            //  onSignupFailed();
            return;
        }


        final ProgressDialog progressDialog = new ProgressDialog(SendRequestActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sending Request...");
        progressDialog.show();

        final String receiver_name = _providerName.getText().toString();
        final String date = _date.getText().toString();
        final String time = _time.getText().toString();
        final String subject = _subject.getText().toString();
        final String message = _message.getText().toString();
        final String location = _location.getText().toString();
        byte[] image = new byte[1];
        try {
             image = readInFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Date currentTime = Calendar.getInstance().getTime();
        String fullName = null;
        if(path!=null)
        {
             fullName = currentTime+path;
             fullName = fullName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        }
        ParseFile file = null;
        if(path != null && path.trim().length() > 0)
        // Create the ParseFile
        {

            file = new ParseFile(fullName, (byte[]) image);
            final String finalFullName = fullName;
            final ParseFile finalFile = file;
            file.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // If successful add file to user and signUpInBackground
                    if(null == e)
                    {

                        ParseObject click = new ParseObject("Message");
                        click.put("message", message);
                        click.put("receiverId", receiverId);
                        click.put("status", "sent");
                        click.put("date", date);
                        click.put("time", time);
                        click.put("imageName", finalFullName);
                        click.put("realImage", finalFile);
                        click.put("senderId", currentUserId);
                        click.put("feedbackFromProvider", "");
                        click.put("feedbackFromUser", "");
                        click.put("senderName", currentUserName);
                        click.put("receiverName", name);
                        click.put("subject", subject);
                        click.put("location", location);
                        click.put("requestStatus", "Waiting");
                        click.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null) {
                                    Log.i("Message", "Sent correctly");
                                    progressDialog.dismiss();
                                    _btn_send.setVisibility(View.GONE);
                                  //  _btn_send.setEnabled(false);
                                    alertDisplayer("Request Sent", "Request sent successfully.Go back and wait for service provider's response.");
                                    //  Toast.makeText(getApplicationContext(), "Send Message", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    progressDialog.dismiss();
                                    alertDisplayer("Sending Failed", e.getMessage());
                                    // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }else{

          ParseObject click = new ParseObject("Message");
            click.put("message", message);
            click.put("receiverId", receiverId);
            click.put("status", "sent");
            click.put("date", date);
            click.put("time", time);
            click.put("senderId", currentUserId);
            click.put("senderName", currentUserName);
            click.put("receiverName", name);

            click.put("feedbackFromProvider", "");
            click.put("feedbackFromUser", "");
            click.put("subject", subject);
            click.put("location", location);
            click.put("requestStatus", "Waiting");
            click.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e==null) {
                        Log.i("Message", "Sent correctly");
                        progressDialog.dismiss();
                      //  _btn_send.setEnabled(false);
                        alertDisplayer("Request Sent", "Request sent successfully.Go back and wait for service provider's response.");
                        //  Toast.makeText(getApplicationContext(), "Send Message", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        progressDialog.dismiss();
                        alertDisplayer("Sending Failed", e.getMessage());
                        // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


            // Upload the image into Parse Cloud


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // message.setText("");
            }
        });
    }
    public boolean validate() {
        boolean valid = true;

        String date = _date.getText().toString();
        String time = _time.getText().toString();
        String subject = _subject.getText().toString();
        String message = _message.getText().toString();

        if (date.isEmpty()) {
            _date.setError("Select Date");
            valid = false;
        } else {
            _date.setError(null);
        }

        if (time.isEmpty()) {
            _time.setError("Select Time");
            valid = false;
        } else {
            _time.setError(null);
        }

        if (subject.isEmpty()) {
            _subject.setError("Enter Subject");
            valid = false;
        } else {
            _subject.setError(null);
        }
        if (message.isEmpty()) {
            _message.setError("Enter Message");
            valid = false;
        } else {
            _message.setError(null);
        }
        return valid;
    }
    void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(SendRequestActivity.this)
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
