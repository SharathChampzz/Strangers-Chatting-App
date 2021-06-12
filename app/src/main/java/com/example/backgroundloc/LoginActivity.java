package com.example.backgroundloc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";

    FirebaseAuth mAuth;
    String email;

    Button login,signup , forgot;
    EditText mail, password;
    ProgressBar loading;

    private String loginStatus;
    //private FirebaseAuth mAuth;

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if(mAuth.getCurrentUser() != null){
//            String data = loadData("email");
//            Log.d("Email Found : ", data);
//            Intent inten = new Intent(this, MainActivity.class);
//            StorageClass.email = data;
//            inten.putExtra("email", data);
//            startActivity(inten);
//            finish();
////            finish();
////            Intent inten = new Intent(LoginActivity.this, MainActivity.class);
////            //inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////            // inten.putExtra("email", username);
////            startActivity(inten);
////            //startActivity(new Intent(LoginActivity.this, MainActivity.class));
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("Login A", "I am in Login Activity");
        String data = loadData("email");

        if(!data.equals("")){ // "User already Logged in"
            Log.d("Email Found : ", data);
            Intent inten = new Intent(this, MainActivity.class);
            StorageClass.email = data;
            inten.putExtra("email", data);
            startActivity(inten);
            finish();
        }
        else{
            Log.d("Debug : " , "Email Not found asking the user to login again");
        }
        mAuth = FirebaseAuth.getInstance();


        login = findViewById(R.id.login);
        signup = findViewById(R.id.noAccount);
        mail = findViewById(R.id.pdfname);
        password = findViewById(R.id.password);
        loading = findViewById(R.id.pbar);
        forgot = findViewById(R.id.forgot);

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mail.getText().toString().trim();
                String pword = password.getText().toString().trim();

                if(email.equals("")){
                    mail.setError("Email is Required..!!");
                    mail.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mail.setError("PLease Enter Valid Email Address");
                    mail.requestFocus();
                }
                else if(pword.equals("")){
                    password.setError("Password is required..!!");
                    password.requestFocus();
                }
                else if(pword.length() < 6){
                    password.setError("Minimum Password Length is 6 Characters");
                    password.requestFocus();
                }
                else{
                    loading.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, pword).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loading.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                email = email.replaceAll("[^a-zA-Z0-9]", "");
                                Log.d("Email ", email);
                                StorageClass.email = email;
                                finish();
                                Toast.makeText(getApplicationContext(), "Login Successfull", Toast.LENGTH_SHORT).show();
                                loginStatus = "login";
                                savedata("email", email);
                                Intent inten = new Intent(LoginActivity.this, MainActivity.class);
                                inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                inten.putExtra("email", email);
                                startActivity(inten);
                                finish();

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String err = e.getLocalizedMessage();
                            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
                        }
                    });

                }


            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent inten = new Intent(LoginActivity.this , SignUpActivity.class);
                startActivity(inten);
            }
        });
    }

//    private void saveData() {
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(TEXT, loginStatus);
//        editor.apply();
//    }

    private void sendEmail() {
        String usermail = mail.getText().toString();
        if(usermail.equals("")){
            Toast.makeText(this, "Please Enter Your Email ID and then try Again!", Toast.LENGTH_SHORT).show();
        }
        else{
            mAuth.sendPasswordResetEmail(usermail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Check Your Mail to reset Password!", Toast.LENGTH_SHORT).show();
                    }
                    else if (task.isCanceled()){
                        Toast.makeText(LoginActivity.this, "An Error Occured Try Later!!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
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