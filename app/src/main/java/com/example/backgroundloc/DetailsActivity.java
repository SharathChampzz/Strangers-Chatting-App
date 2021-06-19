package com.example.backgroundloc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
Things to Save:
Phone Number : pnum
User Name : name
URL : url
Pincode : pincode
Email : email
 */
public class DetailsActivity extends AppCompatActivity {

    Button finish;
    ImageView pick, img;
    EditText phoneNumber, username;

    Bitmap bitmap;
    public static final int PICK_IMAGE = 1;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;

    private StorageTask mUploadTask;

    private Uri uri;
    String emailId;
    Boolean status = false;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";

    public static Boolean uploading = false;

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(status){
            super.onBackPressed();
        }
        else if(uploading){
            Toast.makeText(this, "Uploading Task Going on PLease wait!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Please Finish Basic Setup Details! ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        pick =  findViewById(R.id.pickImage);
        img = findViewById(R.id.profilePic);
        phoneNumber = findViewById(R.id.phoneA);
        username = findViewById(R.id.usernameA);
        finish = findViewById(R.id.loginA);
//        String Phnum = loadData("pnum");
//        String url = loadData("url");

        emailId = getIntent().getStringExtra("email");
        if(emailId != null){
            emailId = emailId.replaceAll("[^a-zA-Z0-9]", "");
            Toast.makeText(this, "Email ID : " + emailId, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Email Field is missing.. Your changes might not be saved.. Please Relogin to see the changes...", Toast.LENGTH_SHORT).show();
            emailId = StorageClass.email;
        }

        try{
            if(!StorageClass.phonenumber.equals("") && !StorageClass.username.equals("")){
                username.setText(StorageClass.username);
                phoneNumber.setText(StorageClass.phonenumber);
                username.setVisibility(View.GONE);
                phoneNumber.setVisibility(View.GONE);
                status = true;
            }
        }
        catch (Exception err){
            Log.e("Error Signup", err.getMessage());
        }

//        if(Phnum.equals("")){  // Fresh User
//            //alert = findViewById(R.id.openWindow);
//            Toast.makeText(this, "No Previous Login session Found!", Toast.LENGTH_SHORT).show();
//        }
//        else{ // Old User
//            StorageClass.phonenumber = Phnum;
//            StorageClass.profileurl = url;
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }


        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    selectImage();
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phnum = phoneNumber.getText().toString();
                String uname = username.getText().toString();

                try{
                    mStorageRef = FirebaseStorage.getInstance().getReference("NearByUsers/Users/" + emailId);
                    mDatabaseRef = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + emailId);
                    if(status || !phnum.equals("") && !uname.equals("")) {
                        if(uri != null){
                            Toast.makeText(DetailsActivity.this, "Uploading... Please Wait...", Toast.LENGTH_SHORT).show();
                            uploading = true;
                            uploadFile();
                            pick.setAlpha(.5f);
                            img.setAlpha(.5f);
                            phoneNumber.setAlpha(.5f);
                            username.setAlpha(.5f);
                            finish.setAlpha(.5f);

                            pick.setClickable(false);
                            img.setClickable(false);
                            phoneNumber.setClickable(false);
                            username.setClickable(false);
                            finish.setClickable(false);
                        }
                    }
                    else{
                        Toast.makeText(DetailsActivity.this, "Please Make Sure to add photo and Fill all section!", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void uploadFile() {

        if (uri != null){
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(uri));

            mUploadTask = fileReference.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                }
                            }, 500);

                            Toast.makeText(getApplicationContext(), "Upload successful", Toast.LENGTH_LONG).show();
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri url = uri.getResult();

                            StorageClass.phonenumber = phoneNumber.getText().toString();
                            StorageClass.username = username.getText().toString();
                            StorageClass.profileurl = url.toString();

                                savedata("pnum", phoneNumber.getText().toString());
                                savedata("name", username.getText().toString());
                                savedata("url", url.toString());
                                savedata("email", emailId);

                                //DatabaseReference current = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + emailId);

                                NearByUsers obj = new NearByUsers(StorageClass.username, StorageClass.profileurl, "", "", StorageClass.phonenumber );
                                if (status){
                                    Map <String, Object> hm = new HashMap<>();
                                    hm.put("url", url.toString());
                                    mDatabaseRef.updateChildren(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(DetailsActivity.this, "Profile Pic Updated SucessFully!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    )
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(DetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    ;
                                }
                                else{
                                    mDatabaseRef.setValue(obj);
                                }

//
//                    updateDataToFirebase();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();

//                            storeData.setUsername(name.getText().toString());
//                            storeData.setProfileUrl(url.toString());
//                            Ino information = new Ifo(url.toString() , name.getText().toString(), info.getText().toString()  , storeData.getEmailId()) ;
//                            String uploadId = mDatabaseRef.push().getKey();
//                            mDatabaseRef.child(uploadId).setValue(information);
                            //startActivity(new Intent(InfoActivity.this , MainActivity.class));

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Upload Failed!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
//            finish();
//            Intent inten = new Intent(getApplicationContext() , MainActivity.class);
//            inten.putExtra("email", emailId);
//            startActivity(inten);
        }

        else{
            Toast.makeText(this, "Pick profile pic and give a try!", Toast.LENGTH_SHORT).show();
        }

    }

    private void selectImage() {
        try{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
        catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("OnActivityResult", e.getMessage());
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getApplicationContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    protected final void onActivityResult(final int requestCode, final int
            resultCode, final Intent i) {
        try {
            super.onActivityResult(requestCode, resultCode, i);
            // this matches the request code in the above call
            if (requestCode == 1) {
                uri = i.getData();
                // this will be null if no image was selected...
                if (uri != null) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        img.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("OnActivityResult", e.getMessage());
        }
    }


    private String loadData(String Key) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(Key, "");
    }

    private void savedata(String Key, String username) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Key, username);
        editor.apply();
        Toast.makeText(getApplicationContext(), "Data saved..!", Toast.LENGTH_SHORT).show();
    }
}