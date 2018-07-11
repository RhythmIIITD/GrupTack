package io.github.httpsphoenix30.gruptack;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.rd.PageIndicatorView;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import io.github.httpsphoenix30.gruptack.HomeAdapter;
import io.github.httpsphoenix30.gruptack.HomeScreen;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,View.OnClickListener{

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    private PageIndicatorView pageIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        mAuth = FirebaseAuth.getInstance();

        pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setCount(4);
        ViewPager pager = findViewById(R.id.viewPager);
        System.out.println(pager.getCurrentItem());
        initViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            Intent loginIntent = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
                AVLoadingIndicatorView avi = findViewById(R.id.avi);
                avi.hide();
                ImageView login_button = findViewById(R.id.login_button);
                login_button.setVisibility(View.VISIBLE);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    showSnackBar("Signin: Success");
                    startActivity(new Intent(getApplicationContext(),HomeScreen.class));
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    showSnackBar("Signin: Failed");
                }
            }
        });
    }
    // [END auth_with_google]

    private void signIn() {
        ImageView login_button = findViewById(R.id.login_button);
        login_button.setVisibility(View.INVISIBLE);
        AVLoadingIndicatorView avi = findViewById(R.id.avi);
        avi.show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showSnackBar(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_button:
                signIn();
                break;
        }
    }

    private void initViews() {
        HomeAdapter adapter = new HomeAdapter();
        adapter.setData(createPageList());

        final ViewPager pager = findViewById(R.id.viewPager);
        pager.setAdapter(adapter);
        pageIndicatorView.setViewPager(pager);
    }

    private List<View> createPageList() {
        List<View> pageList = new ArrayList<>();
        View child1 = getLayoutInflater().inflate(R.layout.welcome_one, null);
        View child2 = getLayoutInflater().inflate(R.layout.welcome_two, null);
        View child3 = getLayoutInflater().inflate(R.layout.welcome_three, null);
        View child4 = getLayoutInflater().inflate(R.layout.welcome_four, null);
        pageList.add(child1);
        pageList.add(child2);
        pageList.add(child3);
        pageList.add(child4);
        return pageList;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}