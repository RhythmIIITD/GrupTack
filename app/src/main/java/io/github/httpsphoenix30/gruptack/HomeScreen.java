package io.github.httpsphoenix30.gruptack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.ag.floatingactionmenu.OptionsFabLayout;
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

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

        private ArrayList<Group> myGroups;

        public GroupAdapter(ArrayList<Group> myGroups) {
            this.myGroups = myGroups;
        }

        @NonNull
        @Override
        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.group_homepage_view,parent,false);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
            Group group = myGroups.get(position);
            holder.grpTitle.setText(group.getName());
            System.out.println(group.getDeadline().get("Date"));
            if(group.getDeadline().get("Date") != 0){
                holder.grpDeadline.setText(group.getDeadline().get("Date")+"/"+group.getDeadline().get("Month")+"/"+group.getDeadline().get("Year"));
            }
            holder.grpUnread.setText("9");
            holder.grpPinned.setText("21");
            Glide.with(holder.grpImage.getContext()).load(Uri.parse(group.getUri())).into(holder.grpImage);
        }

        @Override
        public int getItemCount() {
            return myGroups.size();
        }

        public class GroupViewHolder extends RecyclerView.ViewHolder {
            TextView grpTitle,grpDeadline,grpUnread,grpPinned;
            ImageView grpImage;

            public GroupViewHolder(View v) {
                super(v);
                grpTitle = (TextView) v.findViewById(R.id.group_title);
                grpDeadline = (TextView) v.findViewById(R.id.group_deadline);
                grpUnread = (TextView) v.findViewById(R.id.group_unread);
                grpPinned = (TextView) v.findViewById(R.id.group_pinned);
                grpImage = (ImageView) v.findViewById(R.id.group_image);
            }
        }
    }

    public interface GroupClickListener{
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    public class GroupRecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private HomeScreen.GroupClickListener clickListener;

        public GroupRecyclerTouchListener(Context context, final RecyclerView recyclerView, final HomeScreen.GroupClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    private FirebaseUser mAuthUser;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabaseRefUsers,mFirebaseDatabaseReference;
    private RecyclerView groupsRecyclerView;
    private GroupAdapter mAdapter;
    private User curUser;
    private ArrayList<Group> mGroups = new ArrayList<>(20); //My joined groups
    private ArrayList<String> mGroupNames = new ArrayList<>(20);
    private OptionsFabLayout fabWithOptions;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private TextView logoutText;
    private TextView createtext;
    private boolean isFABOpen = false;
    private View homeView;

    private void showFABMenu(){
        isFABOpen=true;
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_60));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_115));
        createtext.setVisibility(View.VISIBLE);
        logoutText.setVisibility(View.VISIBLE);
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        mAuth = FirebaseAuth.getInstance();
        mAuthUser = mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        mDatabaseRefUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        //fabWithOptions = (OptionsFabLayout) findViewById(R.id.fab_l);
        groupsRecyclerView = (RecyclerView) findViewById(R.id.groups_recyclerView);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupAdapter(mGroups);
        //homeRelativelayout = (RelativeLayout)findViewById(R.id.homeRelativeLayout);
        homeView = (View)findViewById(R.id.homeView);
        /*
        //Set main fab clicklistener.
        fabWithOptions.setMainFabOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        //Set mini fabs clicklisteners.
        fabWithOptions.setMiniFabSelectedListener(new OptionsFabLayout.OnMiniFabSelectedListener() {
            @Override
            public void onMiniFabSelected(MenuItem fabItem) {
                switch (fabItem.getItemId()) {
                    case R.id.fab_create:
                        openCreate();
                        break;
                    case R.id.fab_logout:
                        Toast.makeText(getApplicationContext(),
                                fabItem.getTitle() + "Logout Successful!",
                                Toast.LENGTH_SHORT).show();
                        signout();
                    default:
                        break;
                }
            }
        });
        */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        logoutText = (TextView) findViewById(R.id.logoutText);
        createtext = (TextView) findViewById(R.id.createText);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                    homeView.setVisibility(View.VISIBLE);
                }else{
                    closeFABMenu();
                    logoutText.setVisibility(View.GONE);
                    createtext.setVisibility(View.GONE);
                    homeView.setVisibility(View.INVISIBLE);
                }
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreate();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signout();
            }
        });

        homeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFABMenu();
                logoutText.setVisibility(View.GONE);
                createtext.setVisibility(View.GONE);
                homeView.setVisibility(View.INVISIBLE);
            }
        });

        groupsRecyclerView.addOnItemTouchListener(new GroupRecyclerTouchListener(getApplicationContext(), groupsRecyclerView, new GroupClickListener() {
            @Override
            public void onClick(View view, int position) {
                Group group = mGroups.get(position);
                Intent intent = new Intent(getApplicationContext(),ChatScreen.class);
                intent.putExtra("GroupKey",group.getKey());
                intent.putExtra("GroupName", group.getName());
                startActivity(intent);
            }
            @Override
            public void onLongClick(View view, int position) {
            }
        }));



    }


    @Override
    public void onStart(){
        super.onStart();
        checkAndAddUser();
        groupsRecyclerView.setAdapter(mAdapter);

    }

    private void checkAndAddUser(){
        mGroups.clear();
        mGroupNames.clear();
        curUser = new User();
        curUser.setUuid(mAuthUser.getUid());
        curUser.setName(mAuthUser.getDisplayName());
        curUser.setImage(mAuthUser.getPhotoUrl().toString());
        curUser.setEmail(mAuthUser.getEmail());
        DatabaseReference userRef = mDatabaseRefUsers.child(mAuthUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                   mDatabaseRefUsers.child(curUser.getUuid()).setValue(curUser);
                }else{
                    displayProjects();
                }
            }
            @Override
            public void onCancelled(DatabaseError error){
            }
        });
    }

    private void displayProjects(){
        DatabaseReference databaseReference = mDatabaseRefUsers.child(mAuthUser.getUid()).child("Groups");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        mGroupNames.add(ds.getKey());
                    }
                    fetchGroups();
                }
            }
            @Override
            public void onCancelled(DatabaseError error){
            }
        });
    }

    private void fetchGroups(){
        for (String grpID : mGroupNames){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(grpID);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Group group = dataSnapshot.getValue(Group.class);
                    mGroups.add(group);
                    //System.out.println(mGroups.size() + " : " + mGroupNames.size());
                    mAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void openCreate(){
        startActivity(new Intent(getApplicationContext(),CreateGroup.class));
    }

    private void signout() {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }
}
