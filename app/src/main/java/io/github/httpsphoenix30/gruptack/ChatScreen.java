package io.github.httpsphoenix30.gruptack;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatScreen extends AppCompatActivity {

    public static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMessageTextView;
        TextView receiverDateTextView;
        CircleImageView receiverImageView;
        TextView senderMessageTextView;
        TextView senderDateTextView;
        CircleImageView senderImageView;
        RelativeLayout receiverLayout;
        RelativeLayout senderLayout;

        public ReceiverMessageViewHolder(View v) {
            super(v);
            receiverMessageTextView = (TextView) itemView.findViewById(R.id.receiverMessageText);
            receiverDateTextView = (TextView) itemView.findViewById(R.id.receiverTimeText);
            receiverImageView = (CircleImageView) itemView.findViewById(R.id.receiverImage);
            senderMessageTextView = (TextView) itemView.findViewById(R.id.senderMessageText);
            senderDateTextView = (TextView) itemView.findViewById(R.id.senderTimeText);
            senderImageView = (CircleImageView) itemView.findViewById(R.id.sender_image);
            receiverLayout = (RelativeLayout) itemView.findViewById(R.id.receiver_message_layout);
            senderLayout = (RelativeLayout) itemView.findViewById(R.id.sender_message_layout);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
            });
        }
        private ReceiverMessageViewHolder.ClickListener mClickListener;

        public interface ClickListener{
            public void onItemClick(View view, int position);
            public void onItemLongClick(View view, int position);
        }

        public void setOnClickListener(ReceiverMessageViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }
    }
    //View Holder for pin

    public static class PinAdapter extends RecyclerView.Adapter<PinAdapter.PinMessageViewHolder> {

        private ArrayList<String> myCaptions;

        public PinAdapter (ArrayList<String> myCaptions) {
            this.myCaptions = myCaptions;
            Log.d("CaptionSize2", Integer.toString(myCaptions.size()));
        }

        @NonNull
        @Override
        public PinMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.caption_card,parent,false);
            return new PinMessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PinMessageViewHolder holder, int position) {
                holder.captionHead.setText(myCaptions.get(position));
        }

        @Override
        public int getItemCount() {
            return myCaptions.size();
        }


        public class PinMessageViewHolder extends RecyclerView.ViewHolder {
            private TextView captionHead;
            private CardView cc;
            public PinMessageViewHolder(View v) {
                super(v);
                captionHead = (TextView)v.findViewById(R.id.captionHead);
                cc = (CardView)v.findViewById(R.id.cc);
            }
        }
    }

    public interface PinClickListener{
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    public static class PinRecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private PinClickListener clickListener;

        public PinRecyclerTouchListener(Context context, final RecyclerView recyclerView, final PinClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
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

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private static FirebaseRecyclerAdapter<Message, ReceiverMessageViewHolder> mFirebaseAdapter;
    private static LinearLayoutManager mLinearLayoutManager;
    private static RecyclerView mMessageRecyclerView;
    private static RelativeLayout fakeRelativeLayout;
    private static ImageView pinIcon, deleteIcon;

    private static RecyclerView pinRecyclerView;
    private static PinAdapter pinAdapter;
    private static ArrayList<String> mCaption = new ArrayList<>(200);
    private static HashMap<Integer, String> parentOfCaption = new HashMap<>(200);

    private static FirebaseUser curUser;
    private FirebaseAuth mAuth;
    private static DatabaseReference mDBR;

    private static String groupId, groupName;
    public static Activity fa;
    private static ArrayList<Message> selectedMessage = new ArrayList<>(200);
    private static ArrayList<View> selectedViews = new ArrayList<>(200);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        fa = this;
        TextView groupTitle = (TextView) findViewById(R.id.groupTitle);
        fakeRelativeLayout = (RelativeLayout) findViewById(R.id.fakeRelativeLayout);
        pinIcon = (ImageView)findViewById(R.id.pinIcon);
        deleteIcon = (ImageView)findViewById(R.id.deleteIcon);
        mAuth = FirebaseAuth.getInstance();
        curUser = mAuth.getCurrentUser();
        if(curUser == null){
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        }

        mDBR = FirebaseDatabase.getInstance().getReference();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Intent intent = getIntent();
        groupId = intent.getStringExtra("GroupKey");
        groupName = intent.getStringExtra("GroupName");
        groupTitle.setText(groupName);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


    }
    public void back(View v) {
        finish();
    }

    public void openGroupDetails(View v) {
        Intent intent = new Intent(getApplicationContext(),GroupDetails.class);
        intent.putExtra("GroupKey",groupId);
        startActivity(intent);
    }
    @Override
    public void onBackPressed(){
        finish();
        super.onBackPressed();
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public void sendMessage (View v) {
        EditText editText = (EditText)findViewById(R.id.messageString);
        if(!editText.getText().toString().equals("")) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            Message msg = new Message(editText.getText().toString(),curUser.getUid(),curUser.getPhotoUrl().toString(),"",sdf.format(cal.getTime()));
            mDBR.child("Messages").child(groupId).push().setValue(msg, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null){
                    String key = databaseReference.getKey();
                    mDBR.child("Messages").child(groupId).child(key).child("key").setValue(key);
                }
                }
            });
            editText.setText("");
        }
    }

    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        private void initializePinnedScreen() {
            mCaption.clear();
            parentOfCaption.clear();
            mDBR.child("Pinned").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() == null) {

                    }else{
                        int x=0;
                        for(DataSnapshot ds: dataSnapshot.getChildren()) {

                            mCaption.add(ds.child("Caption").getValue().toString());
                            parentOfCaption.put(x, ds.getKey());
                            x++;
                            //Log.d("DsSize", Integer.toString(ds));
                            pinAdapter.notifyDataSetChanged();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError error){
                }
            });
        }

        public PlaceholderFragment() {
        }

        @Override
        public void onPause() {
            mFirebaseAdapter.stopListening();
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
            mFirebaseAdapter.startListening();
            if(pinAdapter!=null) {
                //initializePinnedScreen();
                pinAdapter.notifyDataSetChanged();
            }

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView;
            if(getArguments().getInt(ARG_SECTION_NUMBER)==1) {
                rootView = inflater.inflate(R.layout.fragment_chat_screen_all, container, false);
                mMessageRecyclerView = (RecyclerView) rootView.findViewById(R.id.messagesRecyclerView);
                pinIcon.setOnClickListener(this);
                deleteIcon.setOnClickListener(this);
                SnapshotParser<Message> parser = new SnapshotParser<Message>() {
                    @Override
                    public Message parseSnapshot(DataSnapshot dataSnapshot) {
                        Message message = dataSnapshot.getValue(Message.class);
                        if (message != null) {
                            message.setKey(dataSnapshot.getKey());
                        }
                        return message;
                    }
                };

                DatabaseReference messagesRef = mDBR.child("Messages").child(groupId);
                FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(messagesRef, parser).build();

                mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, ReceiverMessageViewHolder>(options) {

                    @Override
                    public ReceiverMessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                        ReceiverMessageViewHolder VH = new ReceiverMessageViewHolder(inflater.inflate(R.layout.message_card, viewGroup, false));
                        VH.setOnClickListener(new ReceiverMessageViewHolder.ClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                final Message msg = mFirebaseAdapter.getItem(position);
                                if(msg.isSelected()) {
                                    selectedMessage.remove(msg);
                                    selectedViews.remove(view);
                                    view.setBackgroundColor(getResources().getColor(R.color.transparent, null));
                                    msg.setSelected(false);
                                    Log.d("Size", Integer.toString(selectedMessage.size()));
                                }
                                else {
                                    selectedMessage.add(msg);
                                    selectedViews.add(view);
                                    view.setBackgroundColor(getResources().getColor(R.color.colorSelection, null));
                                    msg.setSelected(true);
                                    Log.d("Size", Integer.toString(selectedMessage.size()));
                                }
                                if(selectedMessage.size()>0) {
                                    fakeRelativeLayout.setVisibility(View.VISIBLE);
                                }
                                else {
                                    fakeRelativeLayout.setVisibility(View.GONE);
                                }
                                //Toast.makeText(getActivity(), "Item long clicked at " + msg.getText(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        return VH;
                    }

                    @Override
                    protected void onBindViewHolder(final ReceiverMessageViewHolder viewHolder, int position, Message friendlyMessage) {

                        if (friendlyMessage.getText() != null) {
                            if(friendlyMessage.getSender().equals(curUser.getUid())) {
                               viewHolder.receiverLayout.setVisibility(View.GONE);
                               viewHolder.senderMessageTextView.setText(friendlyMessage.getText());
                               viewHolder.senderDateTextView.setText(friendlyMessage.getTimeStamp());
                               Glide.with(viewHolder.senderImageView).load((friendlyMessage.getUri())).into(viewHolder.senderImageView);
                            }
                            else {
                                viewHolder.senderLayout.setVisibility(View.GONE);
                                viewHolder.receiverMessageTextView.setText(friendlyMessage.getText());
                                viewHolder.receiverDateTextView.setText(friendlyMessage.getTimeStamp());
                                Glide.with(viewHolder.receiverImageView).load((friendlyMessage.getUri())).into(viewHolder.receiverImageView);
                            }
                        }
                    }
                };

                mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                        int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                        // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                        // to the bottom of the list to show the newly added message.
                        if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                            mMessageRecyclerView.scrollToPosition(positionStart);
                        }
                    }
                });

                mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
                mMessageRecyclerView.setAdapter(mFirebaseAdapter);

            }
            else { //Pinned layout

                rootView = inflater.inflate(R.layout.fragment_chat_screen_pinned, container, false);
                initializePinnedScreen();
                pinRecyclerView = (RecyclerView) rootView.findViewById(R.id.pinnedRecyclerView);
                pinRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                pinAdapter = new PinAdapter(mCaption);
                pinRecyclerView.setAdapter(pinAdapter);


                pinRecyclerView.addOnItemTouchListener(new PinRecyclerTouchListener(getActivity(), pinRecyclerView, new PinClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        String c = mCaption.get(position);
                        //Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getActivity(), DisplayPins.class);
                        i.putExtra("pinGroupId", parentOfCaption.get(position));
                        i.putExtra("groupId", groupId);
                        i.putExtra("captionName", c);
                        startActivity(i);

                    }
                    public void onLongClick(View view, int position) {

                    }
                }));
            }

            return rootView;
        }

        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.pinIcon) {
                final Dialog d = new Dialog(getActivity());
                d.setContentView(R.layout.caption_dialog);
                d.show();
                final EditText captionText = (EditText)d.findViewById(R.id.captionText);
                CardView captionButton = (CardView) d.findViewById(R.id.captionButton);
                captionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String mCaption = captionText.getText().toString();
                        pinMessage(mCaption);
                        d.cancel();
                    }
                });
            } else if (v.getId() == R.id.deleteIcon){
                deleteFromGroup();
                //deleteFromPin();
            }
        }

        private void deleteFromGroup(){
            DatabaseReference deleteFromDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(groupId);
            for(int i=0;i<selectedMessage.size();i++){
                deleteFromDatabase.child(selectedMessage.get(i).getKey()).setValue(null);
                selectedViews.get(i).setBackgroundColor(getResources().getColor(R.color.transparent, null));
            }
            selectedMessage.clear();
            selectedViews.clear();
            fakeRelativeLayout.setVisibility(View.GONE);
            //Do something here
        }

        private void pinMessage(final String msg){
            DatabaseReference pinReference = FirebaseDatabase.getInstance().getReference().child("Pinned").child(groupId);
            String pinKey = pinReference.push().getKey();
            pinReference.child(pinKey).child("Caption").setValue(msg);
            for(int i=0; i<selectedMessage.size(); i++){
                pinReference.child(pinKey).push().setValue(selectedMessage.get(i), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Toast.makeText(getActivity(),"Pinned",Toast.LENGTH_SHORT).show();
                        for(int i=0;i<selectedViews.size();i++){
                            selectedViews.get(i).setBackgroundColor(getResources().getColor(R.color.transparent, null));
                        }
                        selectedMessage.clear();
                        selectedViews.clear();
                        fakeRelativeLayout.setVisibility(View.GONE);
                    }
                });
            }
            mCaption.add(msg);
            parentOfCaption.put(mCaption.size()-1, pinKey);
            pinAdapter.notifyDataSetChanged();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }
}
