package com.example.backgroundloc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    Button signup,login;
    EditText mail, password;
    ProgressBar loading;
    private FirebaseAuth mAuth;

    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();


        signup = findViewById(R.id.signup);
        login = findViewById(R.id.haveAccount);
        mail = findViewById(R.id.pdfname);
        password = findViewById(R.id.password);
        loading = findViewById(R.id.pbar);

        signup.setOnClickListener(new View.OnClickListener() {
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

                    mAuth.createUserWithEmailAndPassword(email,pword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loading.setVisibility(View.GONE);
                            if (task.isSuccessful()){
                                email = email.replaceAll("[^a-zA-Z0-9]", "");
                                StorageClass.email = email;
                                savedata("email", email);
                                finish();
                                Intent inten = new Intent(SignUpActivity.this , DetailsActivity.class);
                                inten.putExtra("email", email);
                                startActivity(inten);
                                Toast.makeText(getApplicationContext(), "User Registered Successfully!", Toast.LENGTH_SHORT).show();

                            }
                            else
                            {
                                if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                    Toast.makeText(getApplicationContext(), "This account is already Registered!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "An error Occured..!!", Toast.LENGTH_SHORT).show();
                                }
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

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent inten = new Intent(SignUpActivity.this , LoginActivity.class);
                startActivity(inten);
            }
        });
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