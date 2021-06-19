package com.example.backgroundloc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


//AIzaSyA-tgQldxr-4MD5yRfS9ZgoqG5O8IqxVdw
public class MainActivity extends AppCompatActivity {

    static MainActivity instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    TextView textView, usernam;

    public static MainActivity getInstance() {
        return instance;
    }

    ImageView gmap, logout, showFrndsName;
    ImageView profilepic, edit, addfriend, confirmadd;
    EditText friendName;
    LinearLayout addFriendLayout;
    String emailId;

    private static final String TAG = "MainActivity";
    DatabaseReference ref;
    DatabaseReference userReference;
    RecyclerView recyclerView;
    SearchView searchView;

    ArrayList<String> emailIdList, userNameList;
    String friendname;
    AdapterClass adapterClass;
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

//                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    public void onClick(DialogInterface arg0, int arg1) {
//                        finishAndRemoveTask();
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting Colors
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.pinktop)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(getResources().getColor(R.color.chatnotColor));
            window.setStatusBarColor(getResources().getColor(R.color.pinktop));
        }


//        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        gmap = findViewById(R.id.map);

        instance = this;
        profilepic = findViewById(R.id.profileA);
        usernam = findViewById(R.id.usernameA);
        edit = findViewById(R.id.edit);
        addfriend = findViewById(R.id.addfriend);
        friendName = findViewById(R.id.friendId);
        confirmadd = findViewById(R.id.add);
        addFriendLayout = findViewById(R.id.addfriendLayout);
        logout = findViewById(R.id.logout);
        String chatter = getIntent().getStringExtra("username");
        if(chatter != null){
            Toast.makeText(this, "You Selected to Chat With : " + chatter, Toast.LENGTH_SHORT).show();
        }

        emailId = getIntent().getStringExtra("email");
        if(emailId != null){
            Toast.makeText(this, "Logged In as  " + emailId, Toast.LENGTH_SHORT).show();
        }

        updateProfile();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        //updateLocation();
                        gmap.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "To Chat with NearBy Users, You must Grant Permission to make it work!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, "Close App and Reopen to Give Location permission!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        // Opening Maps Activity -> Chat with Near By Users
        gmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });

        // Edit Profile
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inten = new Intent(getApplicationContext() , DetailsActivity.class);
                inten.putExtra("email", StorageClass.email);
                startActivity(inten);
            }
        });

        //////////// Related to Chat
        ref = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + StorageClass.email + "/Chats");
        recyclerView = findViewById(R.id.rv);
        searchView = findViewById(R.id.searchview);

        // Button Which Opens EditText and asks for Friends Email ID
        addfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addFriendLayout.getVisibility() == View.VISIBLE) {
                    // EditText is Not visible, Show Cross Mark
                    addfriend.setImageResource(R.drawable.ic_baseline_person_add_alt_1_24);
                    profilepic.setVisibility(View.VISIBLE);
                    edit.setVisibility(View.VISIBLE);
                    gmap.setVisibility(View.VISIBLE);
                    logout.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    addFriendLayout.setVisibility(View.GONE);
                } else {
                    // Edit Text is visible, Show Add Mark
                    addfriend.setImageResource(R.drawable.ic_baseline_cancel_24);
                    profilepic.setVisibility(View.GONE);
                    edit.setVisibility(View.GONE);
                    gmap.setVisibility(View.GONE);
                    logout.setVisibility(View.GONE);
                    searchView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);

                    addFriendLayout.setVisibility(View.VISIBLE);
                    friendName.requestFocus();
                }
            }
        });

        // Adding Friend After Typing Email ID
        confirmadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check Whether User exists before adding
                String name = friendName.getText().toString().replaceAll("[^a-zA-Z0-9]", "");;

                if(!name.equals("")){

                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("NearByUsers/Users");

                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(name)){
                                Toast.makeText(MainActivity.this, "User Found! Chatting Allowed!", Toast.LENGTH_SHORT).show();
                                friendName.setText("");
                                addFriendLayout.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "Redirecting to Chatting window : " + name, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                intent.putExtra("email", name);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Sorry! User couldn't find. ask Your friend to Join Strangers territory", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else{
                    Toast.makeText(MainActivity.this, "Please Enter Your Friend Email Adress", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Logout Button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAppData();
            }
        });

    }

    private void showfriendsName() {
        try{
            if(emailIdList != null){
                getUserIDsFromEmails(emailIdList);
                if(emailIdList.size() == StorageClass.userIds.size()){
                    Log.d("Eligibility", "Eligible For Changing email IDs to username");
                    Toast.makeText(this, "Eligible For Changing email IDs to username", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("Eligibility", "Failed to convert Email IDs to username");
                    Toast.makeText(this, "Failed to convert Email IDs to username", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Email ID is Empty.", Toast.LENGTH_SHORT).show();
            }


        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAppData() {
        try {
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


        @Override
    protected void onStart() {
        super.onStart();

        if (ref != null) {
            ref.addValueEventListener(new ValueEventListener() {
                @NonNull
                @Override
                protected Object clone() throws CloneNotSupportedException {
                    return super.clone();
                }

                // Checking for Past users with whom user chatted
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists()) {
                            emailIdList = new ArrayList<>();
                            userNameList = new ArrayList<>();
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        emailIdList.add(ds.getKey());
                                        userNameList.add(ds.getKey().replace("gmailcom",""));
                                        ///
                                        /*
                                        userReference = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + ds.getKey());
                                        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Log.d("Snapshot : ", snapshot.toString());
                                                friendname = snapshot.child("name").getValue().toString();
                                                Log.d("Name Extracted ", friendname);
//                                        StorageClass.friendname = friendname;
                                                userNameList.add(friendname);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(MainActivity.this, "Error When Changing Email ID to Name : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });  */
                                        ///
//                                getUserName(ds.getKey());
//                                Toast.makeText(MainActivity.this, StorageClass.friendname + " | " + friendname, Toast.LENGTH_SHORT).show();
                                    }


                                    //hear is result part same
                                    //same like post execute
                                    //UI Thread(update your UI widget)
//                                    Log.d("Size Of Users ", String.valueOf(userNameList.size()));
//                                    getUserIDsFromEmails(emailIdList);
                                    adapterClass = new AdapterClass(userNameList); // Here User ID list is to be Passed
                                    recyclerView.setAdapter(adapterClass);

                                    adapterClass.setOnItemClickListener(new AdapterClass.OnClickItemListener() {
                                        @Override
                                        public void onItemClick(int position) {
                                            String key = emailIdList.get(position);

                                            Log.d(TAG, "Normal Key : " + key);
                                            Log.d(TAG, "Position : " + position);

                                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                            intent.putExtra("email", key);
                                            startActivity(intent);
                                        }
                                    });


                            //
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    // do your stuff
//
//
//                                    /// Background Task is done
//                                    runOnUiThread(new Runnable() {
//                                        public void run() {
//                                            // do onPostExecute stuff
//                                            //                            ArrayList<String> userlist = getUserIDFromEmail(emailIdList);
//
//
//                                        }
//                                    });
//                                }
//                            }).start();

                        }
                    }
                    catch (Exception e){
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Search Bar -> On Text Enter -> Perform Search
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    profilepic.setVisibility(View.GONE);
                    usernam.setVisibility(View.GONE);
                    edit.setVisibility(View.GONE);
                    addfriend.setVisibility(View.GONE);
                    logout.setVisibility(View.GONE);
                    gmap.setVisibility(View.GONE);
                    search(newText);
                    return true;
                }
            });
        }
    }

    private void getUserIDsFromEmails(ArrayList<String> emailIdList) {
        userNameList.clear();
        StorageClass.userIds.clear();
        Log.d(TAG, "I am getting user name's from Email's");
        for(int i = 0; i < emailIdList.size(); i++){
//            getUserName(emailIdList.get(i));
            userReference = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + emailIdList.get(i));
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("Snapshot : ", snapshot.toString());
                    friendname = snapshot.child("name").getValue().toString();
                    Log.d("Name Extracted ", friendname);
                    StorageClass.userIds.add(friendname);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error When Changing Email ID to Name : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        Log.d("Size Of Users", String.valueOf(userNameList.size()));
        for(int i = 0; i < userNameList.size(); i++) {
            Toast.makeText(this, userNameList.get(i), Toast.LENGTH_SHORT).show();
        }
        if (emailIdList.size() == userNameList.size()){
            Log.d("Updating Name", "Updating Username");
            AdapterClass adapterClass = new AdapterClass(userNameList); // Here User ID list is to be Passed
            recyclerView.setAdapter(adapterClass);
        }
        else{
            Toast.makeText(this, "User IDs seems to fetched in a wrong way!", Toast.LENGTH_SHORT).show();

        }
    }

    private void getUserName(String email) {
        Log.d(TAG, "I am getting user name from Email : " + email);
        final String[] name = new String[1];

        userReference = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + email);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("Snapshot : ", snapshot.toString());
                    friendname = snapshot.child("name").getValue().toString();
                    Log.d("Name Extracted ", friendname);
                    StorageClass.friendname = friendname;
                    name[0] = friendname;
                    userNameList.add(friendname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error When Changing Email ID to Name : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("I escaped User Name ", name[0]);
//        return StorageClass.friendname;
    }

    // Searching For Users
    private void search(String str) {
        if (!str.equals("")) {
            ArrayList<String> myList = new ArrayList<>();
            ArrayList<String> userEmailList = new ArrayList<>();


            for (String object : emailIdList){
                if(object.toLowerCase().contains(str.toLowerCase())){
                    myList.add(userNameList.get(emailIdList.indexOf(object)));
                    userEmailList.add(object);
                }
            }
            AdapterClass adapterClass = new AdapterClass(myList);
            recyclerView.setAdapter(adapterClass);

            adapterClass.setOnItemClickListener(new AdapterClass.OnClickItemListener() {
                @Override
                public void onItemClick(int position) {
                    String key = userEmailList.get(position);
                    Log.d(TAG, " Searching Method : Key : " + key);
                    Log.d(TAG, "Position : " + position);

                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("email", key);
//                    Bundle bundle = new Bundle();
//                    bundle.putString();
                    startActivity(intent);
                }
            });
        }
        else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class)); // It will lead to unstability, Lets see in future
            finish();
        }
    }

    private void updateProfile() {
        String path = "NearByUsers/Users/" + StorageClass.email;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(path);

        DatabaseReference chatref = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + StorageClass.email + "/Chats");

        // Listening For New Messages  // You need to keep this is background services to listening for incoming messages
        chatref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    Log.d("ChildAdded", snapshot.toString());
                    Log.d("ChildAdded", previousChildName);
                }
                catch (Exception e){
                    Log.d("Error onChildAdded", e.getMessage());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { // Means -> Some Chatting Going On -> Read Messages -> Push Notifications Suitably
                try{
                    final String[] senderPhoneNumber = {""};
                    final String[] message = { "" };
                    String senderEmail = snapshot.getKey();
//                    Log.d("onChildChanged", "New Message By : " + senderEmail); // Rise Notification Here

                    DatabaseReference userref = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + StorageClass.email + "/Chats/" + senderEmail);

                    Query query =  userref.orderByKey().limitToLast(1);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try{
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    Message obj = ds.getValue(Message.class);
                                    assert obj != null;
                                    senderPhoneNumber[0] = obj.getPhonenumber();
                                    message[0] = obj.getMessage();
                                    Log.d("Phone Number ", senderPhoneNumber[0]);
                                    Log.d("Message ", message[0]);

                                    if (senderPhoneNumber[0].equals(StorageClass.phonenumber)) {
                                            Log.d("Message : ", "Message is from user, dont push notification here.");
                                            Log.d("Sender, My no are sam", senderPhoneNumber[0] + "," + StorageClass.phonenumber);
                                        } else {
                                            Log.d("Message : ", "Message is from other side push notification.");
                                            Log.d("Message : ", senderEmail + " : " + message[0]);
                                            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                            notificationHelper.sendHighPriorityNotification(senderEmail, message[0], ChatActivity.class);
                                            break;
                                        }
//                                    try {
//                                        Log.d("JSon String", ds.getValue().toString());
//                                        JSONObject jsonObject = new JSONObject(ds.getValue().toString());
//                                        senderPhoneNumber[0] = String.valueOf(jsonObject.get("phonenumber"));
//                                        message[0] = String.valueOf(jsonObject.get("message"));
//                                        Log.d("sender phone number : ", senderPhoneNumber[0]);
//                                        Log.d("sender Message : ", message[0]);
//                                    } catch (JSONException err) {
//                                        Log.d("Error", err.toString());
//                                    }
//
//                                    if (!senderPhoneNumber[0].equals("")) {
//                                        if (senderPhoneNumber[0].equals(StorageClass.phonenumber)) {
//                                            Log.d("Message : ", "Message is from user, dont push notification here.");
//                                            Log.d("Sender, My no are sam", senderPhoneNumber[0] + "," + StorageClass.phonenumber);
//                                        } else {
//                                            Log.d("Sender, My no are dif", senderPhoneNumber[0] + "," + StorageClass.phonenumber);
//                                            Log.d("Message : ", "Message is from other side push notification.");
//                                            Log.d("Message : ", senderEmail + " : " + message[0]);
//                                            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
//                                            notificationHelper.sendHighPriorityNotification(senderEmail, message[0], ChatActivity.class);
//                                            break;
//                                        }
//                                    }
                                }
                            }
                            catch(Exception e){
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                catch (Exception e){
                    Log.d("Error On Child Added", e.getMessage());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Updating Profile Details
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {  // Loading details, Set Profile Pic and Storing Details
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    String number = dataSnapshot.toString();
                    Log.d("Phone Number", number);
                    String info = dataSnapshot.getValue().toString();  // {name=Champzz, latitude=13.9965017, longitude=75.9215433, url=Url goes here}
                    Log.d("TAG", info);

                    String name = dataSnapshot.child("name").getValue().toString(); // Handle Try catch here, If uploading or entity is missing app crashes
                    String url = dataSnapshot.child("url").getValue().toString();
                    String Phnum = dataSnapshot.child("phonenumber").getValue().toString();

                    Log.d("Name", name);
                    Log.d("URL", url);

                    StorageClass.username = name;
                    StorageClass.profileurl = url;
                    StorageClass.phonenumber = Phnum;
                    setProfilePic(url);
                    usernam.setText(name);

                    Log.d(" Current User name : ", StorageClass.username);
                    Log.d("URL : ", StorageClass.profileurl);
                    Log.d("Phone Number : ", StorageClass.phonenumber);
                    Log.d("Email", StorageClass.email);
                }
                catch(Exception e){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error While Fetching Profile Information : " + databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setProfilePic(String url) {
        Picasso.get().load(url)
                .resize(500, 500)
                .into(profilepic, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap imageBitmap = ((BitmapDrawable) profilepic.getDrawable()).getBitmap();
                        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                        imageDrawable.setCircular(true);
                        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                        profilepic.setImageDrawable(imageDrawable);
                    }

                    @Override
                    public void onError(Exception e) {
                        profilepic.setImageResource(R.drawable.logo);
                        Toast.makeText(MainActivity.this, "Error Occured While Setting Profile Picture : " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}