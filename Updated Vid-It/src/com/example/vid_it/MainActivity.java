package com.example.vid_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
 
public class MainActivity extends Activity {
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.login);
        
        // DatabaseHandler
        
        //Buttons
        Button Photo = (Button) findViewById(R.id.photo);
        Button Video = (Button) findViewById(R.id.video);
        Button AboutUs = (Button) findViewById(R.id.about_link);

        //Listening to take a photo link
        Photo.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent i = new Intent(getApplicationContext(), PhotoActivity.class);
        		startActivity(i);
        	}
        });
        
        //Listening to take a photo link
        Video.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent i = new Intent(getApplicationContext(), VideoActivity.class);
        		startActivity(i);
        	}
        });
               
        // Listening to about us link  
        AboutUs.setOnClickListener(new OnClickListener() {
 
            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(i);
            }
        });
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
}