package io.github.httpsphoenix30.gruptack;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class DisplayPins extends AppCompatActivity {

    public class DisplayPinAdapter extends RecyclerView.Adapter<DisplayPinAdapter.DisplayPinHolder> {

        private ArrayList<Message> myGroups;

        public DisplayPinAdapter(ArrayList<Message> myGroups) {
            this.myGroups = myGroups;
        }

        @NonNull
        @Override
        public DisplayPinHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.message_card,parent,false);
            return new DisplayPinHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DisplayPinHolder holder, int position) {
            Message friendlyMessage = myGroups.get(position);
            if (friendlyMessage.getText() != null) {
                if(friendlyMessage.getSender().equals(curUser.getUid())) {
                    holder.receiverLayout.setVisibility(View.GONE);
                    holder.senderMessageTextView.setText(friendlyMessage.getText());
                    holder.senderDateTextView.setText(friendlyMessage.getTimeStamp());
                    Glide.with(holder.senderImageView).load((friendlyMessage.getUri())).into(holder.senderImageView);
                }
                else {
                    holder.senderLayout.setVisibility(View.GONE);
                    holder.receiverMessageTextView.setText(friendlyMessage.getText());
                    holder.receiverDateTextView.setText(friendlyMessage.getTimeStamp());
                    Glide.with(holder.receiverImageView).load((friendlyMessage.getUri())).into(holder.receiverImageView);
                }
            }
        }

        @Override
        public int getItemCount() {
            return myGroups.size();
        }


        public class DisplayPinHolder extends RecyclerView.ViewHolder {
            TextView receiverMessageTextView;
            TextView receiverDateTextView;
            CircleImageView receiverImageView;
            TextView senderMessageTextView;
            TextView senderDateTextView;
            CircleImageView senderImageView;
            RelativeLayout receiverLayout;
            RelativeLayout senderLayout;

            public DisplayPinHolder(View v) {
                super(v);
                receiverMessageTextView = (TextView) itemView.findViewById(R.id.receiverMessageText);
                receiverDateTextView = (TextView) itemView.findViewById(R.id.receiverTimeText);
                receiverImageView = (CircleImageView) itemView.findViewById(R.id.receiverImage);
                senderMessageTextView = (TextView) itemView.findViewById(R.id.senderMessageText);
                senderDateTextView = (TextView) itemView.findViewById(R.id.senderTimeText);
                senderImageView = (CircleImageView) itemView.findViewById(R.id.sender_image);
                receiverLayout = (RelativeLayout) itemView.findViewById(R.id.receiver_message_layout);
                senderLayout = (RelativeLayout) itemView.findViewById(R.id.sender_message_layout);

            }
        }
    }
    private ArrayList<Message> myGroup = new ArrayList<>();
    private FirebaseUser curUser;
    private CardView yesButton, noButton;
    private FloatingActionButton deletePinGroup;
    private String something, groupid, c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_pins);
        myGroup.clear();
        Intent i = getIntent();
        something = i.getStringExtra("pinGroupId");
        groupid = i.getStringExtra("groupId");
        c = i.getStringExtra("captionName");
        getSupportActionBar().setTitle(c);
        curUser = FirebaseAuth.getInstance().getCurrentUser();
        deletePinGroup = (FloatingActionButton)findViewById(R.id.deletePinGroup);
        RecyclerView displayPinView = (RecyclerView)findViewById(R.id.displayPinView);
        displayPinView.setLayoutManager(new LinearLayoutManager(this));
        final DisplayPinAdapter mAdapter = new DisplayPinAdapter(myGroup);
        displayPinView.setAdapter(mAdapter);
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference();
        dbr.child("Pinned").child(groupid).child(something).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    if(!ds.getKey().equals("Caption")) {
                        Message message = ds.getValue(Message.class);
                        myGroup.add(message);
                        mAdapter.notifyDataSetChanged();
                    }
                    Log.d("TESTING", ds.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        deletePinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(DisplayPins.this);
                d.setContentView(R.layout.delete_dialog);
                d.show();
                yesButton = (CardView)d.findViewById(R.id.yesButton);
                noButton = (CardView)d.findViewById(R.id.noButton);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.cancel();
                        ChatScreen.fa.finish();
                        startActivity(new Intent(getApplicationContext(), HomeScreen.class));
                        finish();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.cancel();
                    }
                });
            }
        });
    }
}
