package com.example.emily.qrcontact;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.EmailType;
import ezvcard.property.StructuredName;

public class Make extends AppCompatActivity {

    final String[] VCardTags = { "VERSION", "PRODID", "N:", "EMAIL", "TEL", "END" };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make);
        Picasso.get().setLoggingEnabled(true);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Button refresh = (Button) findViewById(R.id.button);
        final EditText firstName = (EditText) findViewById(R.id.firstName);
        final EditText lastName = (EditText) findViewById(R.id.lastName);
        final EditText phone = (EditText) findViewById(R.id.phone);
        final EditText email = (EditText) findViewById(R.id.email);
        final EditText profileName = findViewById(R.id.profileName);
        refresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                VCard vcard = makeVCard(firstName, lastName, email, phone);
                String fullContact = Ezvcard.write(vcard).version(VCardVersion.V3_0).go();
                fullContact = getUrlVCard(vcard);
                Log.d("myTag", "Registered Click");
                Picasso.get().load("https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=" + fullContact).into(imageView);
            }
        });
        loadImage(imageView);
        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.navigation_scan:
                        Intent intent1 = new Intent(Make.this, MainActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.navigation_make:
                        break;
                    case R.id.navigation_help:
                        Intent intent3 = new Intent(Make.this, Help.class);
                        startActivity(intent3);
                        break;
                }
                return false;
            }
        });
        final ListView profileList = findViewById(R.id.profileList);
        final Button saveProfile = findViewById(R.id.saveProfile);
        final Button chooseProfile = findViewById(R.id.chooseProfile);
        final List<Profile> profileArrayList = loadData();
        final ConstraintLayout profileLayout = findViewById(R.id.profileLayout);
        ArrayAdapter<Profile> arrayAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_1, profileArrayList);
        profileList.setAdapter(arrayAdapter);
        chooseProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (profileArrayList.size() == 0) {
                    Log.d("Choose Profile", "No profiles");
                    return;
                }
                profileLayout.setVisibility(View.GONE);
                bottomNavigationView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                profileList.setVisibility(View.VISIBLE);
            }
        });
        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Profile temp = profileArrayList.get(position);
                firstName.setText(temp.getvCard().getStructuredName().getGiven());
                lastName.setText(temp.getvCard().getStructuredName().getFamily());
                email.setText(temp.getvCard().getEmails().get(0).getValue());
                phone.setText(temp.getvCard().getTelephoneNumbers().get(0).getText());
                profileName.setText(temp.getProfileName());
                profileLayout.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                profileList.setVisibility(View.GONE);
            }
        });
        saveProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isThereACopy = false;
                VCard toAddToProfile = makeVCard(firstName, lastName, email, phone);
                Profile toAddToList = new Profile(toAddToProfile, profileName.getText().toString());
                if (toAddToList == null || toAddToList.getProfileName().equals("")) {
                    Log.d("Save Profile", "No Profile Name");
                    return;
                }
                for (Profile profile : profileArrayList) {
                    if (profile.equals(toAddToList) || toAddToList.getProfileName().equals(profile.getProfileName())) {
                        isThereACopy = true;
                        break;
                    }
                    isThereACopy = false;
                }
                if (isThereACopy) {
                    Log.d("Save Profile", "Profile already exists or Profile Name is Already Taken");
                    return;
                }
                profileArrayList.add(toAddToList);
                saveData(profileArrayList);
                Log.d("Persistence", "Saved Data");
            }
        });
    }

    private void saveData(List<Profile> profileArrayList) {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Log.d("Persistence", "Make Shared Pref");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("Persistence", "Make Editor");
        Gson gson = new Gson();
        Log.d("Persistence", "Made Gson");
        String json = gson.toJson(profileArrayList);
        Log.d("Persistence", "Made Json");
        editor.putString("task list", json);
        Log.d("Persistence", "Put Json");
        editor.apply();
        Log.d("Persistence", "Applied");
    }

    private ArrayList<Profile> loadData() {
        try {
            ArrayList<Profile> profileArrayList = new ArrayList<>();
            SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("task list", null);
            Type type = new TypeToken<ArrayList<Profile>>() {
            }.getType();
            profileArrayList = gson.fromJson(json, type);
            if (profileArrayList == null) {
                profileArrayList = new ArrayList<>();
            }
            return profileArrayList;
        } catch (Exception e) {
            Log.d("Persistence", e.toString());
        }
        return new ArrayList<Profile>();
    }

    private String getUrlVCard(VCard vCard) {
        String fullContact = Ezvcard.write(vCard).version(VCardVersion.V3_0).go();
        String newFullContact = fullContact;
        for (int x = 0; x < VCardTags.length; x++) {
            if (newFullContact.contains(VCardTags[x])) {
                int parseStop = newFullContact.indexOf(VCardTags[x]);
                if (VCardTags[x].equals("N:")) {
                    parseStop = newFullContact.indexOf(VCardTags[x], newFullContact.indexOf(VCardTags[x - 1]));
                }
                try {
                    newFullContact = newFullContact.substring(0, parseStop) + "%0A" + newFullContact.substring(parseStop);
                } catch (Exception e) {
                    Log.d("out of bounds", e.toString() + " " + parseStop);
                }
            }
        }
        Log.d("test", newFullContact);
        return newFullContact;
    }

    private void loadImage(ImageView toSet) {
        Log.d("fucntion", "Default code");
        Picasso.get().load("https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=Example").into(toSet);
    }

    private VCard makeVCard(EditText firstName, EditText lastName, EditText email, EditText phone) {
        VCard vcard = new VCard();
        StructuredName fullName = new StructuredName();
        fullName.setFamily(String.valueOf(lastName.getText()));
        fullName.setGiven(String.valueOf(firstName.getText()));
        vcard.setStructuredName(fullName);
        vcard.addEmail(String.valueOf(email.getText()), EmailType.WORK);
        vcard.addTelephoneNumber(String.valueOf(phone.getText()));
        return vcard;
    }

    public class Profile {

        private VCard vCard;

        private String profileName;

        public VCard getvCard() {
            return vCard;
        }

        public String getProfileName() {
            return profileName;
        }

        public Profile(VCard toVCard, String toProfileName) {
            vCard = toVCard;
            profileName = toProfileName;
        }

        public boolean equals(Object toCompare) {
            if (!(toCompare instanceof Profile)) {
                return false;
            }
            Profile newCompare = (Profile) toCompare;
            String firstName = nullToEmpty(vCard.getStructuredName().getGiven());
            String toCompareFirstName = nullToEmpty(newCompare.getvCard().getStructuredName().getGiven());
            String lastName = nullToEmpty(vCard.getStructuredName().getFamily());
            String toCompareLastName = nullToEmpty(newCompare.getvCard().getStructuredName().getGiven());
            String email = nullToEmpty(vCard.getEmails().get(0).getValue());
            String toCompareEmail = nullToEmpty(newCompare.getvCard().getEmails().get(0).getValue());
            String phone = nullToEmpty(vCard.getTelephoneNumbers().get(0).getText());
            String toComparePhone = nullToEmpty(newCompare.getvCard().getTelephoneNumbers().get(0).getText());
            return (firstName.equals(toCompareFirstName) && lastName.equals(toCompareLastName) && email.equals(toCompareEmail) && phone.equals(toComparePhone) && profileName.equals(newCompare.getProfileName()));
        }

        private String nullToEmpty(String maybeNull) {
            if (maybeNull == null) {
                return "";
            }
            return maybeNull;
        }

        public String toString() {
            return profileName;
        }
    }
}
