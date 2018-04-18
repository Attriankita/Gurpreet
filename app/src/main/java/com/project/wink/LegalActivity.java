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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LegalActivity extends AppCompatActivity {
    private static final String TAG = "Legal";
    @BindView(R.id.legalText) TextView _legalText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ButterKnife.bind(this);

        toolbar.setTitle("Wink Copyright Policy");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String sourceString = "<h2>Wink Copyright Policy</h2>" +
                "Notification of Copyright Infringement<br><br>" +
                "Wink respects the intellectual property rights of others and expects its users to do the same. It is Wink’s policy, in appropriate circumstances and its discretion, to disable or terminate the accounts of users who repeatedly infringe or are repeatedly charged with infringing the copyrights or other intellectual property rights of others.<br><br>" +
                "<b>Terms and Conditions</b>" +
                "<br><br><b>1. Contractual Relationship</b><br>" +
                "These Terms of Use govern the access or use by you, an individual, from within any county in the world of applications, content and services made available by Wink" +
                "<br><br>" +
                "PLEASE READ THESE TERMS CAREFULLY BEFORE ACCESSING OR USING THE SERVICES<br><br>" +
                "You access and use the services constitutes your agreement to be bound by these terms, which establishes a contractual relationships between you and Wink. If you do not agree to these Terms, you may not access or use the Services. These terms expressly supersede prior agreements or arrangements with you." +
                "<br><br><b>2. The Services</b><br>" +
                "The Services constitute a technology platform that enables users of Wink’s mobile application provided as part of the services to arrange and schedule transportation or logistics services with independent third party providers of such services, including independent third party logistics providers under agreement with Wink." +
                "The service providers and Wink are not responsible for any loss of the customer. Wink does not take any liability of the loss of any user during service or after that. The services are provided “as it is” and “as available”. Wink shall not be liable for indirect,ibcidental,special, expemplary,punitive or consequential damages, including lost profits, lost data, personal injury or property damage related to, in connection with , or otherwise resulting from any use of the services, even if Wink has been advised of the possibility of such damages, liability or losses arising out of" +
                "<ul><li>Your use of or reliance on the services or your inability to access or use the service.</li>" +
                "<li>Any transaction or relationship between you and any third party provider.</li></ul>";
        _legalText.setText(Html.fromHtml(sourceString));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(LegalActivity.this)
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
