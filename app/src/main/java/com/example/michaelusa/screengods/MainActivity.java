package com.example.michaelusa.screengods;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.example.michaelusa.screengods.Utilities.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity {

    public GoogleApiClient mGoogleApiClient;
    public GoogleSignInOptions gso;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * OnCreate's purpose in this activity is to:
     *
     * 1. Initialize all neccessary instance variables and UI elements.
     * 2. Provide code for all interactivity (Sign-In Button) in activity
     * 3. Provide decently abstracted google sign-in infrastructure.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeSignInTheme();

        initGoogleSignInCred();
        mGoogleApiClient.connect();
        trySignOut(getIntent().getExtras());

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("B", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("B", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {

        super.onStop();
        mGoogleApiClient.disconnect();

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Initializes theme and design elements like typeface for the Sign-In Screen.
     */

    private void initializeSignInTheme() {

        getSupportActionBar().hide();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Gilroy-ExtraBold.otf");
        TextView screenGods = (TextView) findViewById(R.id.sg);
        screenGods.setTypeface(typeface);
    }

    /**
     * Initializes instance variables for sign-in procedures.
     * Code credit goes largely to: Firebase Docs
     *
     * https://firebase.google.com/docs/auth/android/google-signin?utm_source=studio
     */

    private void initGoogleSignInCred() {

        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    /**
     * Method that checks if Data bundle from Main Menu Activity is not null,
     * and if so, that indicates the user is ready to sign out, so it calls the local
     * signOut method.
     *
     * @param extras (Data bundle from Main Menu Activity)
     */

    private void trySignOut(Bundle extras) {

        if (extras != null) {
            String signOutMarker = extras.getString("callSignOut");

            if (signOutMarker.equals("SO")){
                signOut();
            }
        }
    }

    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Reconnects to the GoogleApiClient on function call, and uses a ConnectionCallback listener
     * to properly sign a user out of both firebase and google.
     *
     * Thanks to the Firebase Docs for the majority of this code:
     * https://firebase.google.com/docs/auth/android/google-signin?utm_source=studio
     */

    public void signOut() {

        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

            @Override
            public void onConnected(@Nullable Bundle bundle) {

                FirebaseAuth.getInstance().signOut();
                if(mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {if (status.isSuccess()){}}});
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d("D", "Google API Client Connection Suspended");
            }
        });
    }

    /**
     * Basic Auth Method that verifies the user's credentials with Google and Firebase
     * and completes the log-in "handshake".
     *
     * @param acct Google's Sign-In Object for Auth
     */

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        Log.d("G", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d("G", "signInWithCredential:onComplete:" + task.isSuccessful());

                if (!task.isSuccessful()) {
                    Log.w("E", "signInWithCredential", task.getException());
                    Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * If the user's information matches the provided keychain RC_SIGN_IN, a Toast is thrown,
     * and the activity is switched to the Main Menu.
     *
     * @param requestCode Constant param; junk variable for post-auth.
     * @param resultCode Google provided resultCode; black box, no interaction.
     * @param data Also black box for intent switching for Google pop-up.
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Utilities.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {

                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

                String successfulSignIn = "Sign-In Succeeded!";
                Toast succSignIn = Toast.makeText(getApplicationContext(), successfulSignIn, Toast.LENGTH_SHORT);

                succSignIn.show();
                switchToMainMenu(getCurrentFocus());

            } else {

                String badAttempt = "Sign-In Failed!";
                Toast badSignIn = Toast.makeText(getApplicationContext(), badAttempt, Toast.LENGTH_SHORT);
                badSignIn.show();
            }
        }
    }

    /**
     * Basic intent switch from Sign-In Screen to Main Menu. Only possible if
     * user is already authenticated with Firebase Auth.
     *
     * @param view (The current focus; the activity we reside in currently)
     */

    public void switchToMainMenu(View view) {

        Intent intent = new Intent(MainActivity.this, MainMenu.class);
        startActivity(intent);
    }
}