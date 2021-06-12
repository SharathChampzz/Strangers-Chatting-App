package com.example.backgroundloc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static android.content.Context.MODE_PRIVATE;

public class ViewDialog {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    ImageView user;
    TextView name, close;
    Button chat;

    public void showDialog(Activity activity, String userN, String url){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.welcome);

        user = dialog.findViewById(R.id.userprofile);
        name = dialog.findViewById(R.id.unameA);
        chat = dialog.findViewById(R.id.chatA);
        close = dialog.findViewById(R.id.closeA);
        name.setText(userN);

        showImage(url, activity);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "Opening Chat Activity!", Toast.LENGTH_SHORT).show();
                Intent i=new Intent(activity,ChatActivity.class);
                i.putExtra("username",userN);
                activity.startActivity(i);
//                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
                dialog.dismiss();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showImage(String url, Activity activity) {
        Picasso.get().load(url)
                .resize(500, 500)
                .into(user, new Callback() {
                    @Override
                    public void onSuccess() {
                        //Toast.makeText(activity, "Shishya ee url ok na : " + url, Toast.LENGTH_SHORT).show();
                        Bitmap imageBitmap = ((BitmapDrawable) user.getDrawable()).getBitmap();
                        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(activity.getResources(), imageBitmap);
                        imageDrawable.setCircular(true);
                        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                        user.setImageDrawable(imageDrawable);
                    }

                    @Override
                    public void onError(Exception e) {
                        user.setImageResource(R.drawable.logo);
                        Toast.makeText(activity, "Error Occured While Setting Profile Picture : " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void saveData(Context context, EditText type, String pplno) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT, type.getText().toString() + "," + pplno);
        editor.apply();
        Toast.makeText(context, "Data saved!  Please Refresh to see updated result!!", Toast.LENGTH_SHORT).show();
    }


}
