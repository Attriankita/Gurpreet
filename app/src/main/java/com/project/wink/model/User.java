package com.project.wink.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("User")
public class User extends ParseObject {
    public String getName() {
        return getString("fullName");
    }

    public void setName(String name) {
        put("fullName", name);
    }

    public String getEmail() {
        return getString("providerName");
    }

    public void setEmail(String email) {
        put("enail", email);
    }

    public String getUserType() {
        return getString("userType");
    }

    public void setHomeNumber(String userType) {
        put("userType", userType);
    }

}
