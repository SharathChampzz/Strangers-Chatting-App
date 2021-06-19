package com.example.backgroundloc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.ViewCompat;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ChatActivity extends AppCompatActivity implements ExampleDialog.ExampleDialogListener {

    ImageView sendbutton, report, user;
    TextView name;
    EditText msg, disease;
    LinearLayout linearLayout;
    ScrollView scroll;
    String friendname, friendId;

    DatabaseReference myChatLocation, FriendChatLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        View view = findViewById(R.id.chatLayoutA);
//        View root = view.getRootView();
//        root.setBackgroundColor(getResources().getColor(R.color.pinkbackground));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(getResources().getColor(R.color.chatnotColor));
            window.setStatusBarColor(getResources().getColor(R.color.pinktop));
        }
        report = findViewById(R.id.report);
        name = findViewById(R.id.username);
        user = findViewById(R.id.profile);
        disease = findViewById(R.id.userreport);

        Objects.requireNonNull(getSupportActionBar()).hide();
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.pinktop)));

        String mainA = getIntent().getStringExtra("email"); // This is from Mainactivity
        String dialogA = getIntent().getStringExtra("username"); // This is from Dialog Maps Activity

        if(mainA == null){ // MainA is empty seems like input is comming from dialog so consider that
            friendId = dialogA;
        }
        else{
            friendId = mainA;
        }

        //
        name.setText(friendId);
//        actionBar.setTitle(friendId);
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + friendId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Snapshot : ", snapshot.toString());
                friendname = snapshot.child("name").getValue().toString();
                String url = snapshot.child("url").getValue().toString();
                Log.d("Name Extracted ", friendname);
//                actionBar.setTitle(friendname);
                try{
                    name.setText(friendname);
                    Picasso.get().load(url)
                            .resize(75, 75)
                            .into(user, new Callback() {
                                @Override
                                public void onSuccess() {
                                    //Toast.makeText(activity, "Shishya ee url ok na : " + url, Toast.LENGTH_SHORT).show();
                                    Bitmap imageBitmap = ((BitmapDrawable) user.getDrawable()).getBitmap();
                                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                                    imageDrawable.setCircular(true);
                                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                                    user.setImageDrawable(imageDrawable);
                                }

                                @Override
                                public void onError(Exception e) {
                                    user.setImageResource(R.drawable.logo);
                                    Toast.makeText(getApplicationContext(), "Error Occured While Setting Profile Picture : " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                catch(Exception err){
                    Toast.makeText(ChatActivity.this, "Failed to Set : " + err.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Error When Changing Email ID to Name : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //

        Toast.makeText(this, friendId, Toast.LENGTH_SHORT).show();
        myChatLocation = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + StorageClass.email  + "/Chats/" + friendId);
        FriendChatLocation = FirebaseDatabase.getInstance().getReference("NearByUsers/Users/" + friendId  + "/Chats/" + StorageClass.email);

        sendbutton = findViewById(R.id.send);
        msg = findViewById(R.id.messager);
        linearLayout = (LinearLayout) findViewById(R.id.chatLayout);
        scroll = findViewById(R.id.scrollView);

        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = msg.getText().toString();
                msg.setText("");

                if(!message.equals("")){  // Not empty Upload it to firebase
                    Message obj = new Message(message, StorageClass.phonenumber);
                    myChatLocation.push().setValue(obj);
                    FriendChatLocation.push().setValue(obj);
                }
                else{
                    Toast.makeText(ChatActivity.this, "Please Type Message Before Sending!", Toast.LENGTH_SHORT).show();
                }
                msg.setFocusableInTouchMode(true);
                msg.requestFocus();
            }
        });

        myChatLocation.addValueEventListener(new ValueEventListener() {  // Listening For new Messages
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    linearLayout.removeAllViews();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Message obbj = ds.getValue(Message.class);
                        String message = obbj.getMessage();
                        String sendder = obbj.getPhonenumber();

                        if (sendder.equals(StorageClass.phonenumber)) {  // Keep this message to right side
                            // Right side Text View
                            TextView textView2 = new TextView(getApplicationContext());
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.gravity = Gravity.END;
                            layoutParams.setMargins(10, 10, 10, 10); // (left, top, right, bottom)
                            textView2.setLayoutParams(layoutParams);
                            textView2.setText(message);
                            textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                            textView2.setBackgroundColor(0x00e0fbca); // hex color 0xAARRGGBB
//                            textView2.setBackgroundResource(R.color.whatsappsender);
                            textView2.setBackgroundResource(R.color.pinktop);
                            textView2.setPadding(20, 20, 20, 20);
                            linearLayout.addView(textView2);

                        } else {  // Keep this message to left side
                            // Left side Text View
                            TextView textView1 = new TextView(getApplicationContext());
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(10, 10, 10, 10); // (left, top, right, bottom)
                            textView1.setLayoutParams(layoutParams);
                            textView1.setText(message);
//                            textView1.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
//                            textView1.setBackgroundResource(R.color.white);
                            textView1.setBackgroundResource(R.color.pinkleft);
                            textView1.setPadding(20, 20, 20, 20);// in
                            // pixels (left, top, right, bottom)
                            textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                            ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
//                                    .toBuilder()
//                                    .setAllCorners(CornerFamily.ROUNDED,12f)
//                                    .build();
//
////                            MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
////                            ViewCompat.setBackground(textView1,shapeDrawable);

                            linearLayout.addView(textView1);
                        }
//                        Log.d("Messages : ", message);
                    }
                    scroll.post(new Runnable() {    // Scroll to bottom after message sent
                        @Override
                        public void run() {
                            scroll.fullScroll(ScrollView.FOCUS_DOWN);
                            msg.setFocusableInTouchMode(true);
                            msg.requestFocus();
                        }
                    });
                    //scroll.requestFocus(); //scroll.scrollTo(0, scroll.getBottom());  // Scroll to bottom after message sent
                }
                catch (Exception e){
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                risetoast("Help us letting Know... What went wrong..");
                openDialog();
            }
        });
    }

    private void risetoast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void openDialog() {
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void applyTexts(String reporttext) {
        risetoast(reporttext);
        Report obj = new Report(friendId, reporttext);
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference("NearByUsers/Reports");
        mDatabase.push().setValue(obj);
        risetoast("Thank you So Much For Letting Us Know!");
    }
}