package com.example.michaelusa.screengods;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainMenu extends AppCompatActivity {

    /**
     * OnCreate's purpose in this activity is to:
     *
     * 1. Initialize all UI elements not covered in XML File.
     * 2. Provide code for all interactivity (SignOut, MyAlerts, PopAlerts) in activity
     * 3. On Sign-Out, bundle appropriate trigger string ("SO") to unpack in MainActivity for
     * full sign-out functionality.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        initMainMenuTheme();

        final Button signOut = (Button) findViewById(R.id.sign_out);
        signOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        final Button myAlerts = (Button) findViewById(R.id.my_alerts);
        myAlerts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchToMyAlerts(getCurrentFocus());
            }
        });

        final Button popAlerts = (Button) findViewById(R.id.pop_alerts);
        popAlerts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchToPopularAlerts(getCurrentFocus());
            }
        });
    }

    /**
     * Initializes theme and design elements like typeface for the MainMenu Screen.
     */

    private void initMainMenuTheme() {

        getSupportActionBar().hide();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Gilroy-ExtraBold.otf");

        TextView mainMenu = (TextView) findViewById(R.id.mainMenu);
        mainMenu.setTypeface(typeface);

        TextView tv_popAlerts = (TextView) findViewById(R.id.pop_alerts);
        tv_popAlerts.setTypeface(typeface);

        TextView tv_myAlerts = (TextView) findViewById(R.id.my_alerts);
        tv_myAlerts.setTypeface(typeface);

        TextView tv_signOut = (TextView) findViewById(R.id.sign_out);
        tv_signOut.setTypeface(typeface);
    }

    private void signOut() {

        FirebaseAuth.getInstance().signOut();
        switchToSignInScreen(getCurrentFocus());
    }

    /**
     * Basic intent switch from Main Menu back to the Sign in screen. Only possible if
     * user is already authenticated with Firebase Auth.
     * Passes back trigger for trySignOut method required to properly sign out in MainActivity.
     *
     * @param view (The current focus; the activity we reside in currently)
     */

    private void switchToSignInScreen(View view) {
        Intent intent = new Intent(MainMenu.this, MainActivity.class);
        intent.putExtra("callSignOut", "SO");
        startActivity(intent);
    }

    private void switchToMyAlerts(View view) {
        Intent intent = new Intent(MainMenu.this, MyAlerts.class);
        startActivity(intent);
    }

    private void switchToPopularAlerts(View view) {
        Intent intent = new Intent(MainMenu.this, PopularAlerts.class);
        startActivity(intent);
    }
}
