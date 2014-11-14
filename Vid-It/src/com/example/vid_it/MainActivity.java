package com.example.vid_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
 
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.login);
 
        Button loginNow = (Button) findViewById(R.id.btnLogin);
        Button registerScreen = (Button) findViewById(R.id.link_to_register);
        Button tosScreen = (Button) findViewById(R.id.tos);
 
        // Listening to register new account link
        registerScreen.setOnClickListener(new OnClickListener() {
 
            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
        
        // Listening to register new account link
        tosScreen.setOnClickListener(new OnClickListener() {
 
            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), TOSActivity.class);
                startActivity(i);
            }
        });
        
     // Listening to login link
        loginNow.setOnClickListener(new OnClickListener() {
 
            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), AndroidCamera.class);
                startActivity(i);
            }
        });
    }
}