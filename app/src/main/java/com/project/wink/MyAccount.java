package com.project.wink;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseUser;
import com.project.wink.admin.HomeMainAdmin;

import java.util.HashMap;

import butterknife.BindView;


public class MyAccount extends Fragment {

    SessionManager session;

    TextView userFullName;
    String userType;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view;
        ParseUser user = ParseUser.getCurrentUser();
        userType = user.getString("userType");
        if(userType.equals("Admin"))
        {
             view = inflater.inflate(R.layout.my_account_admin, container, false);
        }else{
             view = inflater.inflate(R.layout.my_account, container, false);
        }


        userFullName = (TextView) view.findViewById(R.id.userFullName);

        session = new SessionManager(getActivity());

        userFullName.setText(user.getString("fullName"));

        Button profileDetail = (Button) view.findViewById(R.id.buttonProfile);
        Button payment = (Button) view.findViewById(R.id.buttonPaymentDetails);

        profileDetail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), ProfileDetailActivity.class);
                startActivity(intent);

            }
        });
        payment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), AccountBalance.class);
                startActivity(intent);

            }
        });

        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return view;

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("My Account");

    }

}