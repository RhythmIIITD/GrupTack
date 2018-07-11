package io.github.httpsphoenix30.gruptack;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wang.avi.AVLoadingIndicatorView;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class CreateGroup extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int SELECT_PICTURE = 100;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 101;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseRefUsers;
    private EditText creategroup_deadline,creategroup_title;
    private Uri selectedImageUri;
    private boolean imageUploaded;
    private Group mGroup;
    private FirebaseUser mAuthUser;
    private GoogleApiClient mGoogleApiClient;
    private RelativeLayout av_loader;
    private TextView createGroupButton;
    private LinearLayout parent2;
    private RelativeLayout parent1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        //mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
        mDatabaseRefUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        mAuthUser = FirebaseAuth.getInstance().getCurrentUser();
        selectedImageUri = null;

        creategroup_deadline= (EditText)findViewById(R.id.creategroup_deadline);
        creategroup_title= (EditText)findViewById(R.id.creategroup_title);
       av_loader=(RelativeLayout)findViewById(R.id.av_loader);
        createGroupButton = (TextView) findViewById(R.id.creategroup_btn);
        parent1 = (RelativeLayout) findViewById(R.id.parent1);
        parent2 = (LinearLayout) findViewById(R.id.parent2);

    }

    //TODO: Use actual validation library
    /* Validations*/
    public void validate()
    {
        String deadline= creategroup_deadline.getText().toString();
        String title= creategroup_title.getText().toString();
        Integer date=0,month=0,year=0;
        Boolean check=true;
        if(title.equals(""))
        {
            creategroup_title.setError("Required Field");
        }
        else{
            if(!deadline.equals(""))
            {
                String pieces[]= deadline.split("/");
                if(pieces.length == 3){
                    date=Integer.parseInt(pieces[0]);
                    month=Integer.parseInt(pieces[1]);
                    year=Integer.parseInt(pieces[2]);
                    if(date < 1 || date > 31 ){
                        creategroup_deadline.setError("Type Appropriate Date");
                        check=false;
                    }
                    else if( month < 1 || month > 12  ) {

                        creategroup_deadline.setError("Type Appropriate Month");
                        check=false;
                    }
                    else if( year < 2018 )
                    {
                        creategroup_deadline.setError("Type Appropriate Year");
                        check=false;
                    }
                }
                else{
                    creategroup_deadline.setError("Invalid Format");
                }
            }
            if(check){
                if(selectedImageUri != null){
                    mGroup = new Group(title,date,month,year,selectedImageUri.toString());
                } else{
                    mGroup = new Group(title,date,month,year,"");
                }
                checkPermissions();
            }
        }
    }

    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    ((ImageView) findViewById(R.id.creategroup_image)).setImageURI(selectedImageUri);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

       switch (v.getId()) {
            case R.id.change_dp:
                openImageChooser();
                break;
            case R.id.creategroup_btn:
               validate();
                break;
        }
   }


    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Permission has already been granted
            writeToDatabase();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeToDatabase();
                } else {
                    Toast.makeText(getApplicationContext(),"Permission not granted",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void writeToDatabase(){
        parent1.setVisibility(View.GONE);
        parent2.setVisibility(View.GONE);
        av_loader.setVisibility(View.VISIBLE);

        imageUploaded = false;
        // Set group details
        mDatabase.push().setValue(mGroup, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null){
                    String key = databaseReference.getKey();
                    databaseReference.child("key").setValue(key);
                    mGroup.setKey(key);
                    if(selectedImageUri != null){
                        imageUploaded = true;
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(key).child("Group_DP");
                        putImageInStorage(storageReference, selectedImageUri, key);
                    }
                    addGroupToUser();
                    if (!imageUploaded) {
                        Toast.makeText(getApplicationContext(),"Created",Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Unable to write message to database: " + databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, String key) {
        grantUriPermission(getPackageName(),uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
        storageReference.putFile(uri).addOnCompleteListener(CreateGroup.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mGroup.setUri(task.getResult().getMetadata().getDownloadUrl().toString());
                    mDatabase.child(mGroup.getKey()).setValue(mGroup);
                    Toast.makeText(getApplicationContext(),"Created",Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    System.out.println("Image upload task was not successful: " + task.getException());
                    av_loader.setVisibility(View.GONE);
                    parent1.setVisibility(View.VISIBLE);
                    parent2.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void addGroupToUser(){
        DatabaseReference databaseReference = mDatabaseRefUsers.child(mAuthUser.getUid()).child("Groups");
        databaseReference.child(mGroup.getKey()).setValue(true);
        addMemberToGroups();
    }

    private void addMemberToGroups(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Members");
        databaseReference.child(mGroup.getKey()).child(mAuthUser.getUid()).setValue(true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
