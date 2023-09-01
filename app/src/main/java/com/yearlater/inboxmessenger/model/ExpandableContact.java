package com.yearlater.inboxmessenger.model;

import android.os.Parcelable;

import com.yearlater.inboxmessenger.model.realms.PhoneNumber;
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;
import com.yearlater.inboxmessenger.model.realms.PhoneNumber;

import io.realm.RealmList;

/**
 * Created by yearlater on 15/01/2018.
 */

//expandable contact
//make user selects which numbers want to send for the contact
public class ExpandableContact extends MultiCheckExpandableGroup implements Parcelable {


    public ExpandableContact(String contactName, RealmList<PhoneNumber> phoneNumbers) {
        super(contactName, phoneNumbers);

    }


}
