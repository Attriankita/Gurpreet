package com.project.wink;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AddressComponent;
import com.seatgeek.placesautocomplete.model.AddressComponentType;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.BindView;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private static final int SELECT_PICTURE = 100;
    private static final int SELECT_RESUME = 101;
    private static final int SELECT_CERTIFICATE = 102;
    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_phone) EditText _phoneText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_c_password) EditText _c_passwordText;
    @BindView(R.id.radioUserType) RadioGroup _userType;
    @BindView(R.id.radioUser) RadioButton _radioUser;
    @BindView(R.id.radioServiceProvider) RadioButton _radioServiceProvider;
    @BindView(R.id.btn_signup) Button _signupButton;

    @BindView(R.id.govt_pdf) Button _govt_pdf;
    @BindView(R.id.resume_pdf) Button _resume_pdf;
    @BindView(R.id.certificate_pdf) Button _certificate_pdf;

    @BindView(R.id.mainLayout) LinearLayout _mainLayout;

    @BindView(R.id.link_login) TextView _loginLink;

    @BindView(R.id.places_autocomplete) PlacesAutocompleteTextView _street;
    @BindView(R.id.input_city) TextView _city;
    @BindView(R.id.input_state) TextView _state;
    @BindView(R.id.input_country) TextView _country;
    @BindView(R.id.input_postal_code) TextView _postal;
    @BindView(R.id.input_longitude) TextView _longitude;
    @BindView(R.id.input_latitude) TextView _latitude;

    String path = "";
    String resumePath = "";
    String certificatePath = "";

    // Session Manager Class
    SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        final GeocodingLocation locationAddress = new GeocodingLocation();


        // Session Manager
        session = new SessionManager(getApplicationContext());

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
        _govt_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openPDFChooser();
            }
        });
        _resume_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openResumePDFChooser();
            }
        });
        _certificate_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                openCertificatePDFChooser();
            }
        });
        _userType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i)
                {
                    case R.id.radioUser:
                        // TODO Something
                        removeButtons();
                        break;
                    case R.id.radioServiceProvider:
                        // TODO Something
                        addButtons();
                        break;
                }
            }
        });
        _street.setOnPlaceSelectedListener(new OnPlaceSelectedListener() {
            @Override
            public void onPlaceSelected(final Place place) {
                _street.getDetailsFor(place, new DetailsCallback() {
                    @Override
                    public void onSuccess(final PlaceDetails details) {
                        Log.d("test", "details " + details);
                        _street.setText(details.name);
                        locationAddress.getAddressFromLocation(details.formatted_address,
                                getApplicationContext(), new GeocoderHandler());
                        for (AddressComponent component : details.address_components) {
                            for (AddressComponentType type : component.types) {
                                switch (type) {
                                    case STREET_NUMBER:
                                        break;
                                    case ROUTE:
                                        break;
                                    case NEIGHBORHOOD:
                                        break;
                                    case SUBLOCALITY_LEVEL_1:
                                        break;
                                    case SUBLOCALITY:
                                        break;
                                    case LOCALITY:
                                        _city.setText(component.long_name);
                                        break;
                                    case ADMINISTRATIVE_AREA_LEVEL_1:
                                        _state.setText(component.long_name);
                                        break;
                                    case ADMINISTRATIVE_AREA_LEVEL_2:
                                        break;
                                    case COUNTRY:
                                        _country.setText(component.long_name);
                                        break;
                                    case POSTAL_CODE:
                                        _postal.setText(component.long_name);
                                        break;
                                    case POLITICAL:
                                        break;
                                }
                            }
                        }
                        _postal.requestFocus();
                    }

                    @Override
                    public void onFailure(final Throwable failure) {
                        Log.d("test", "failure " + failure);
                    }
                });
            }
        });
    }
    /* Choose an pdf from Gallery */
    void openPDFChooser() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), SELECT_PICTURE);
    }
    void openResumePDFChooser() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), SELECT_RESUME);
    }
    void openCertificatePDFChooser() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), SELECT_CERTIFICATE);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {

                    if (selectedImageUri.getLastPathSegment().endsWith("pdf")) {
                        path = getPath(getApplicationContext(),selectedImageUri);
                      //  Toast.makeText(getBaseContext(), path,
                               // Toast.LENGTH_LONG).show();
                        _govt_pdf.setText("Document Uploaded");
                    } else if (resultCode == RESULT_CANCELED){
                        Toast.makeText(this, "Invalid file type", Toast.LENGTH_SHORT).show();
                    }
                    // Get the path from the Uri

                    // Set the image in ImageView
                   // _image.setImageURI(selectedImageUri);
                }
            }
            if (requestCode == SELECT_RESUME) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {

                    if (selectedImageUri.getLastPathSegment().endsWith("pdf")) {
                        resumePath = getPath(getApplicationContext(),selectedImageUri);
                        //Toast.makeText(getBaseContext(), path,
                               // Toast.LENGTH_LONG).show();
                        _resume_pdf.setText("Document Uploaded");
                    } else if (resultCode == RESULT_CANCELED){
                        Toast.makeText(this, "Invalid file type", Toast.LENGTH_SHORT).show();
                    }
                    // Get the path from the Uri

                    // Set the image in ImageView
                    // _image.setImageURI(selectedImageUri);
                }
            }
            if (requestCode == SELECT_CERTIFICATE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {

                    if (selectedImageUri.getLastPathSegment().endsWith("pdf")) {
                        certificatePath = getPath(getApplicationContext(),selectedImageUri);
                       // Toast.makeText(getBaseContext(), path,
                                //Toast.LENGTH_LONG).show();
                        _certificate_pdf.setText("Document Uploaded");
                    } else if (resultCode == RESULT_CANCELED){
                        Toast.makeText(this, "Invalid file type", Toast.LENGTH_SHORT).show();
                    }
                    // Get the path from the Uri

                    // Set the image in ImageView
                    // _image.setImageURI(selectedImageUri);
                }
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
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
            String[] separated = locationAddress.split(",");
            String latitude = separated[0]; // this will contain "Fruit"
            String longitude = separated[1];
          _longitude.setText(longitude);
          _latitude.setText(latitude);
        }
    }
    public  void addButtons(){
    _certificate_pdf.setVisibility(View.VISIBLE);
    _govt_pdf.setVisibility(View.VISIBLE);
    _resume_pdf.setVisibility(View.VISIBLE);

    }
    public  void removeButtons(){

        _certificate_pdf.setVisibility(View.GONE);
        _govt_pdf.setVisibility(View.GONE);
        _resume_pdf.setVisibility(View.GONE);
    }
    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
          //  onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        final String name = _nameText.getText().toString();
        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();
        final String phone = _phoneText.getText().toString();

        final String street = _street.getText().toString();
        final String city = _city.getText().toString();
        final String state = _state.getText().toString();
        final String postal = _postal.getText().toString();
        final String country = _country.getText().toString();
        final String longitude = _longitude.getText().toString();
        final String latitude = _latitude.getText().toString();

        // get selected radio button from radioGroup
        int selectedId = _userType.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        RadioButton radioButton = (RadioButton) findViewById(selectedId);

        final String userType = radioButton.getText().toString();

        if(_govt_pdf.getVisibility()==View.VISIBLE)
        {
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

        byte[] image2 = new byte[1];
        try {
            image2 = readInFile(resumePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Date currentTim2e = Calendar.getInstance().getTime();
        String fullName2 = null;
        if(resumePath!=null)
        {
            fullName2 = currentTime+resumePath;
            fullName2 = fullName2.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        }

        byte[] image3 = new byte[1];
        try {
            image3 = readInFile(certificatePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Date currentTime3 = Calendar.getInstance().getTime();
        String fullName3 = null;
        if(certificatePath!=null)
        {
            fullName3 = currentTime+certificatePath;
            fullName = fullName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        }

        ParseFile file = null;
        ParseFile file1 = null;
        ParseFile file2 = null;
        if(path != null && path.trim().length() > 0) {
            file1 = new ParseFile(fullName2, (byte[]) image2);
            final String finalFullName1 = fullName2;
            final ParseFile finalFile1 = file1;

            file1.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // If successful add file to user and signUpInBackground
                    if (null == e) {
                    }
                }
            });

            file2 = new ParseFile(fullName3, (byte[]) image3);
            final String finalFullName2 = fullName3;
            final ParseFile finalFile2 = file2;


            file2.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // If successful add file to user and signUpInBackground
                    if (null == e) {
                    }
                }
            });

            file = new ParseFile(fullName, (byte[]) image);
            final String finalFullName = fullName;
            final ParseFile finalFile = file;
            file.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // If successful add file to user and signUpInBackground
                    if (null == e) {

                        ParseUser user = new ParseUser();
                        user.setUsername(email);
                        user.setPassword(password);
                        user.put("userType", userType);
                        user.put("fullName", name);
                        user.put("phone", phone);
                        user.put("govtID", finalFile);
                        user.put("resumeFile", finalFile);
                        user.put("certificateFile", finalFile);
                        user.put("street", street);
                        user.put("city", city);
                        user.put("state", state);
                        user.put("postalCode", postal);
                        user.put("country", country);
                        user.put("longitude", longitude);
                        user.put("latitude", latitude);
                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    progressDialog.dismiss();
                                    // Creating user login session
                                    // For testing i am stroing name, providerName as follow
                                    // Use user real data
                                    session.createLoginSession(name, email);

                                    //  t_username.setText(ParseUser.getCurrentUser().getUsername());
                                    ParseUser user = ParseUser.getCurrentUser();
                                    user.setUsername(email);
                                    user.setEmail(email);
                                    user.put("userType", userType);
                                    user.put("fullName", name);
                                    user.put("phone", phone);
                                    user.put("street", street);
                                    user.put("city", city);
                                    user.put("state", state);
                                    user.put("postalCode", postal);
                                    user.put("country", country);
                                    user.put("rate", 0);
                                    user.put("longitude", longitude);
                                    user.put("latitude", latitude);
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Intent show = new Intent(SignupActivity.this, HomeNavigationActivity.class);

                                            startActivity(show);
                                            finish();
                                        }
                                    });

                                } else {
                                    progressDialog.dismiss();
                                    alertDisplayer("Register Fail", e.getMessage());
                                    _signupButton.setEnabled(true);
                                }
                            }
                        });
                    }
                }
            });
        }

        }
        else{
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {

                            ParseUser user = new ParseUser();
                            user.setUsername(email);
                            user.setPassword(password);
                            user.put("userType", userType);
                            user.put("fullName", name);
                            user.put("phone", phone);
                            user.put("street", street);
                            user.put("city", city);
                            user.put("state", state);
                            user.put("postalCode", postal);
                            user.put("country", country);
                            user.put("longitude", longitude);
                            user.put("latitude", latitude);
                            user.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        progressDialog.dismiss();
                                        // Creating user login session
                                        // For testing i am stroing name, providerName as follow
                                        // Use user real data
                                        session.createLoginSession(name, email);

                                        //  t_username.setText(ParseUser.getCurrentUser().getUsername());
                                        ParseUser user = ParseUser.getCurrentUser();
                                        user.setUsername(email);
                                        user.setEmail(email);
                                        user.put("userType", userType);
                                        user.put("fullName", name);
                                        user.put("phone", phone);
                                        user.put("street", street);
                                        user.put("city", city);
                                        user.put("state", state);
                                        user.put("postalCode", postal);
                                        user.put("country", country);
                                        user.put("rate", 0);
                                        user.put("longitude", longitude);
                                        user.put("latitude", latitude);
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                Intent show = new Intent(SignupActivity.this, HomeNavigationActivity.class);

                                                startActivity(show);
                                                finish();
                                            }
                                        });

                                    } else {
                                        progressDialog.dismiss();
                                        alertDisplayer("Register Fail", e.getMessage());
                                        _signupButton.setEnabled(true);
                                    }
                                }
                            });

                        }
                    }, 3000);

        }
        // TODO: Implement your own signup logic here.

    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
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
    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String phone = _phoneText.getText().toString();
        String password = _passwordText.getText().toString();
        String c_password = _c_passwordText.getText().toString();
        String street = _street.getText().toString();
        String state = _state.getText().toString();
        String postal_Code = _postal.getText().toString();
        String city = _city.getText().toString();
        String country = _country.getText().toString();

        if (name.isEmpty()) {
            _nameText.setError("Enter your full name");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (phone.isEmpty()) {
            _phoneText.setError("Enter a valid phone number");
            valid = false;
        } else {
            _phoneText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Enter a password between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (c_password.isEmpty() || c_password.length() < 4 || c_password.length() > 10) {
            _c_passwordText.setError("Must same as above");
            valid = false;
        } else {
            _c_passwordText.setError(null);
        }

        if(!password.equals(c_password)){
            _c_passwordText.setError("Must same as above");
            valid = false;
        } else {
            _c_passwordText.setError(null);
        }

        if (street.isEmpty()) {
            _street.setError("Enter street address");
            valid = false;
        } else {
            _street.setError(null);
        }

        if (city.isEmpty()) {
            _city.setError("Enter city");
            valid = false;
        } else {
            _city.setError(null);
        }

        if (postal_Code.isEmpty()) {
            _postal.setError("Enter postal code");
            valid = false;
        } else {
            _postal.setError(null);
        }

        if (state.isEmpty()) {
            _state.setError("Enter state");
            valid = false;
        } else {
            _state.setError(null);
        }

        if (country.isEmpty()) {
            _country.setError("Enter country");
            valid = false;
        } else {
            _country.setError(null);
        }

        if (_userType.getCheckedRadioButtonId() == -1)
        {
            _radioServiceProvider.setError("Must select one type");
            valid = false;
        }
        else
        {
            _radioServiceProvider.setError(null);
        }
        if (_govt_pdf.getVisibility() == View.VISIBLE) {
            if(path==null || path=="")
            {
                _govt_pdf.setError("Must Select any govt ID");
                valid = false;
            }else{
                _govt_pdf.setError(null);
            }
            // Its visible
        } else {
            _govt_pdf.setError(null);
            // Either gone or invisible
        }
        if (_resume_pdf.getVisibility() == View.VISIBLE) {
            if(resumePath==null || resumePath=="")
            {
                _resume_pdf.setError("Must Select Resume");
                valid = false;
            }else{
                _resume_pdf.setError(null);
            }
            // Its visible
        } else {
            _resume_pdf.setError(null);
            // Either gone or invisible
        }
        if (_certificate_pdf.getVisibility() == View.VISIBLE) {
            if(certificatePath==null || certificatePath=="")
            {
                _certificate_pdf.setError("Must Select Certificate");
                valid = false;
            }else{
                _certificate_pdf.setError(null);
            }
            // Its visible
        } else {
            _certificate_pdf.setError(null);
            // Either gone or invisible
        }
        return valid;
    }
    void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this)
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