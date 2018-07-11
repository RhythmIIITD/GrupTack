package io.github.httpsphoenix30.gruptack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDetails extends AppCompatActivity {

    public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder>{

        private ArrayList<Members> mMembers;

        public MembersAdapter(ArrayList<Members> mMembers) { this.mMembers = mMembers; }

        @NonNull
        @Override
        public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.members_list_layout,parent,false);
            return new GroupDetails.MembersAdapter.MembersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
            Members member = mMembers.get(position);
            holder.name.setText(member.getName());
            holder.email.setText(member.getEmail());
            Glide.with(holder.img).load(member.getImg()).into(holder.img);
        }

        @Override
        public int getItemCount() {
            return mMembers.size();
        }

        public class MembersViewHolder extends RecyclerView.ViewHolder{

            TextView name,email;
            CircleImageView img;

            public MembersViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.nameGroupDetails);
                email = itemView.findViewById(R.id.emailGroupDetails);
                img = itemView.findViewById(R.id.imageGroupDetails);
            }
        }
    }

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;
    private ImageView groupImage;
    private DatabaseReference mDatabaseReference;
    private String groupId;
    private FirebaseUser mFirebaseUser;
    private ArrayList<Members> membersList = new ArrayList<>(20);
    private MembersAdapter membersAdapter;
    private CollapsingToolbarLayout collapsing;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        groupId = intent.getStringExtra("GroupKey");

        CardView share = (CardView) findViewById(R.id.share_group_card);
        CardView leave = (CardView) findViewById(R.id.leave_group_card);
        groupImage = (ImageView) findViewById(R.id.groupImageDetails);
        toolbar = (Toolbar)findViewById(R.id.toolbarGroupDetails);
        collapsing = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        share.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent myintent = new Intent(Intent.ACTION_SEND);
                myintent.setType("text/plain");
                myintent.putExtra(Intent.EXTRA_TEXT,"http://www.gruptack.com/join/" + groupId);
                startActivity(Intent.createChooser(myintent, "Share using"));
            }
        });
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGroup();
            }
        });

        loadGroupDetails();

        recyclerView = (RecyclerView)findViewById(R.id.members_list_recycler);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        membersAdapter = new MembersAdapter(membersList);
        recyclerView.setAdapter(membersAdapter);

        loadMembersList();
    }

    private void leaveGroup(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Users").child(mFirebaseUser.getUid()).child("Groups").child(groupId).setValue(null);
        databaseReference.child("Members").child(groupId).child(mFirebaseUser.getUid()).setValue(null);
        ChatScreen.fa.finish();
        finish();
    }

    private void loadGroupDetails(){
        mDatabaseReference.child("Groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                //getSupportActionBar().setTitle(group.getName());
                Toast.makeText(getApplicationContext(), group.getName(), Toast.LENGTH_SHORT).show();
                //System.out.println(group.getUri());
                collapsing.setTitle(group.getName());
                //groupImage.setImageURI(Uri.parse(group.getUri()));
                Glide.with(groupImage).load(group.getUri()).into(groupImage);

                //TODO: Set Image and Title
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadMembersList(){
        mDatabaseReference.child("Members").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    searchMembers(ds.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void searchMembers(String member){
        mDatabaseReference.child("Users").child(member).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
                User user = dataSnapshot.getValue(User.class);
                //System.out.println(user.getName());
                Members members = new Members(user.getName(),user.getEmail(),user.getImage());
                membersList.add(members);
                membersAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}