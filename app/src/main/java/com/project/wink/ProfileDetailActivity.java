package com.project.wink;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
import com.thomashaertel.widget.MultiSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileDetailActivity extends AppCompatActivity {
    private static final String TAG = "UpdateProfile";
    @BindView(R.id.input_name) EditText _fullName;
    @BindView(R.id.input_email) EditText _email;
    @BindView(R.id.input_phone) EditText _phone;
    @BindView(R.id.btn_update) Button _buttonUpdate;

    @BindView(R.id.places_autocomplete)
    PlacesAutocompleteTextView _street;
    @BindView(R.id.input_city) TextView _city;
    @BindView(R.id.input_state) TextView _state;
    @BindView(R.id.input_country) TextView _country;
    @BindView(R.id.input_postal_code) TextView _postal;
    @BindView(R.id.input_longitude) TextView _longitude;
    @BindView(R.id.input_latitude) TextView _latitude;
    @BindView(R.id.input_rate) EditText _rate;

    List<String> categoriesList = new ArrayList<String>();

    private MultiSpinner spinner;
    private ArrayAdapter<String> adapter;
    SessionManager session;

    String userType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Profile Details");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        session = new SessionManager(getApplicationContext());
        final GeocodingLocation locationAddress = new GeocodingLocation();
        ButterKnife.bind(this);
        getUserDetails();

        // create spinner list elements
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);

        serverQuery();

        _buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
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
    private MultiSpinner.MultiSpinnerListener onSelectedListener = new MultiSpinner.MultiSpinnerListener() {
        public void onItemsSelected(boolean[] selected) {

            categoriesList.clear();

            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    categoriesList.add(adapter.getItem(i));
                }
            }

        }
    };
    private void serverQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Categories");
        query.orderByAscending("name");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, @Nullable ParseException e) {
                if (e == null){
                    if(list!=null && !list.isEmpty())
                    {
                        for (int i = 0; i < list.size(); i++) {
                            adapter.add(list.get(i).getString("name"));
                        }
                        // get spinner and set adapter
                        spinner = (MultiSpinner) findViewById(R.id.spinnerMulti);
                        if(userType.equals("Service Provider")) {
                            spinner.setAdapter(adapter, false, onSelectedListener);

                            // set initial selection
                            boolean[] selectedItems = new boolean[adapter.getCount()];
                           // String ss = adapter.getItem()
                          //   selectedItems[1] = true; // select second item
                           // Toast.makeText(getApplicationContext(), "List Empty",Toast.LENGTH_SHORT).show();
                            spinner.setSelected(selectedItems);
                        }
                        else{
                            spinner.setVisibility(View.GONE);
                        }
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
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.delete_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            deleteConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteConfirmation(){
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Do you really want to delete your account?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteUser();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void deleteUser(){
        final ProgressDialog progressDialog = new ProgressDialog(ProfileDetailActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Deleting Account...");
        progressDialog.show();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        ParseUser user = ParseUser.getCurrentUser();
                        user.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    progressDialog.dismiss();
                                    // Update user login session
                                    alertDisplayer("Account Delete", "Your Account deleted successfully");
                                    session.logoutUser();

                                } else {
                                    progressDialog.dismiss();
                                    alertDisplayer("Deletion Failed", e.getMessage());
                                }
                            }
                        });
                    }
                }, 3000);
    }
    public void getUserDetails() {
//  t_username.setText(ParseUser.getCurrentUser().getUsername());
        ParseUser user = ParseUser.getCurrentUser();
        userType = user.getString("userType");
        if(user.getString("userType").equals("Service Provider"))
        {
            _rate.setVisibility(View.VISIBLE);
            if(user.getNumber("rate")!=null){
                _rate.setText(String.valueOf(user.getNumber("rate")));
            }else{
                _rate.setText("");
            }


        }

        _fullName.setText(user.getString("fullName"));
        _phone.setText(user.getString("phone"));
        _email.setText(user.getEmail());

        _street.setText(user.getString("street"));
        _city.setText(user.getString("city"));

        _state.setText(user.getString("state"));
        _postal.setText(user.getString("postalCode"));
        _country.setText(user.getString("country"));

        _latitude.setText(user.getString("latitude"));
        _longitude.setText(user.getString("longitude"));
    }

    public void submitForm() {
        Log.d(TAG, "Updating Profile");

        if (!validate()) {
            //  onSignupFailed();
            return;
        }


        final ProgressDialog progressDialog = new ProgressDialog(ProfileDetailActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();

        final String name = _fullName.getText().toString();
        final String email = _email.getText().toString();
        final String phone = _phone.getText().toString();

        final String street = _street.getText().toString();
        final String city = _city.getText().toString();
        final String state = _state.getText().toString();
        final String postal = _postal.getText().toString();
        final String country = _country.getText().toString();
        final String longitude = _longitude.getText().toString();
        final String latitude = _latitude.getText().toString();
        final String rate = _rate.getText().toString();

    //    String ss =spinner.getSelectedItem().toString();

        // TODO: Implement your own signup logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        ParseUser user = ParseUser.getCurrentUser();
                        user.setUsername(email);
                        user.put("fullName",name);
                        user.put("phone",phone);
                        user.put("street",street);
                        user.put("city",city);
                        user.put("state",state);
                        user.put("postalCode",postal);
                        user.put("country",country);

                        user.put("longitude",longitude);
                        user.put("latitude",latitude);
                  if(userType.equals("Service Provider"))
                  {
                      user.put("rate",Integer.parseInt(rate));
                      user.put("categories",categoriesList);
                  }
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    progressDialog.dismiss();
                                    // Update user login session
                                    // For testing i am stroing name, providerName as follow
                                    // Use user real data
                                    session.createLoginSession(name, email);

                                  alertDisplayer("Profile Update", "Your Profile updated successfully");

                                } else {
                                    progressDialog.dismiss();
                                    alertDisplayer("Updation Failed", e.getMessage());
                                }
                            }
                        });
                    }
                }, 3000);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _fullName.getText().toString();
        String email = _email.getText().toString();
        String phone = _phone.getText().toString();
        String street = _street.getText().toString();
        String state = _state.getText().toString();
        String postal_Code = _postal.getText().toString();
        String city = _city.getText().toString();
        String country = _country.getText().toString();
        String rate = _rate.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _fullName.setError("at least 3 characters");
            valid = false;
        } else {
            _fullName.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _email.setError("enter a valid providerName fullName");
            valid = false;
        } else {
            _email.setError(null);
        }

        if (phone.isEmpty()) {
            _phone.setError("enter a valid phone number");
            valid = false;
        } else {
            _phone.setError(null);
        }

        if (street.isEmpty()) {
            _street.setError("Enter street");
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
        if(userType.equals("Service Provider"))
        {
            if (rate.isEmpty()) {
                _rate.setError("Enter country");
                valid = false;
            } else {
                _rate.setError(null);
            }
        }
        return valid;
    }
    void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileDetailActivity.this)
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
