package io.github.httpsphoenix30.gruptack;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinGroup extends AppCompatActivity {

    private TextView textView2;
    private Group mGroup;
    private FirebaseUser curUser;
    private DatabaseReference dbr;
    private boolean alreadyJoined;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        curUser = FirebaseAuth.getInstance().getCurrentUser();
        //System.out.println(curUser.getDisplayName());
        onNewIntent(getIntent());

        SwipeButton swipeButton = (SwipeButton)findViewById(R.id.swipe_btn);
        swipeButton.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
            if(curUser == null){
                showToast("You need to Login first !");
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            } else{
                dbr = FirebaseDatabase.getInstance().getReference();
                checkIfJoined();
            }
            }
        });
    }

    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String groupId = data.substring(data.lastIndexOf("/") + 1);
            searchGroup(groupId);
        }
    }

    private void searchGroup(String groupId){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //System.out.println(dataSnapshot);
                if(dataSnapshot.getValue() == null){
                    System.out.println("No such group");
                    showToast("No such group");
                    finish();
                } else{
                    //System.out.println(dataSnapshot.getValue());
                    mGroup = dataSnapshot.getValue(Group.class);
                    loadProject();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadProject(){
        ImageView imageView = (ImageView) findViewById(R.id.join_image);
        TextView textView1 = (TextView) findViewById(R.id.join_title);
        textView2 = (TextView) findViewById(R.id.join_members);
        Glide.with(imageView.getContext()).load(Uri.parse(mGroup.getUri())).into(imageView);
        textView1.setText(mGroup.getName());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Members").child(mGroup.getKey());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textView2.setText("" + dataSnapshot.getChildrenCount() + " Members");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkIfJoined(){
        alreadyJoined = false;
        dbr.child("Users").child(curUser.getUid()).child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.getKey().equals(mGroup.getKey())){
                        alreadyJoined = true;
                        break;
                    }
                }
                if(alreadyJoined){
                    showToast("You are already a member !");
                    finish();
                } else {
                    completeJoinProcess();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void completeJoinProcess(){
        System.out.println("Joined");
        showToast("JOINED");
        dbr.child("Users").child(curUser.getUid()).child("Groups").child(mGroup.getKey()).setValue(true);
        dbr.child("Members").child(mGroup.getKey()).child(curUser.getUid()).setValue(true);
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }

    private void showToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }
}
