package com.chandan.halo;

import java.util.ArrayList;

/**
 * Created by chandan on 13-06-2016.
 */
public class SelectUser {
    String name,phone;
    String thumbString;
    Boolean visiblity;
    public String getThumbString() {
        return thumbString;
    }

    public void setThumbString(String thumbString) {
        this.thumbString = thumbString;
    }

    public Boolean getButtonVisibility() {
        return visiblity;
    }

    public void setButtonVisibilty(Boolean visiblity) {
        this.visiblity = visiblity;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private static int lastContactId=0;

//    public static ArrayList<SelectUser> createContactsList(int numContacts) {
//        ArrayList<SelectUser> contacts = new ArrayList<SelectUser>();
//
//        for (int i = 1; i <= numContacts; i++) {
//            SelectUser selectUser=new SelectUser();
//            selectUser.setName("Person " + ++lastContactId);
//            selectUser.setPhone("12345678 p"+ ++lastContactId);
//            selectUser.setButtonVisibilty(true);
//            contacts.add(selectUser);
//        }
//
//        return contacts;
//    }

}
