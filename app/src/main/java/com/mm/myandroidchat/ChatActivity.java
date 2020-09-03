package com.mm.myandroidchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mm.myandroidchat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {


    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private  MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD=17;
    private int mCurrentPAge=1;

    private SwipeRefreshLayout mRefreshLayout;

    private int itemPos=0;
    private String mLastKey="";
    private String mPrevKey="";

    private static final int GALLERY_PICK=1;

    private StorageReference mImageStorage;
    private DatabaseReference mUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar=findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();


        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mImageStorage= FirebaseStorage.getInstance().getReference();
        mRootRef= FirebaseDatabase.getInstance().getReference();
        mAuth= FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();
        mChatUser= getIntent().getStringExtra("value of userid");
        String userName= getIntent().getStringExtra("user_name");
       // getSupportActionBar().setTitle(userName);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view= inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);


        // -----Custom Action bar Items


        mTitleView= findViewById(R.id.custom_bar_name);
        mLastSeenView=findViewById(R.id.custom_bar_lastseen);
       // mProfileImage= findViewById(R.id.custom_bar_image);

        mChatAddBtn=findViewById(R.id.chat_add_btn);
        mChatSendBtn=findViewById(R.id.chat_send_btn);
        mChatMessageView=findViewById(R.id.chat_message_view);
        mMessagesList=findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mTitleView.setText(userName);

        mRefreshLayout = findViewById(R.id.swipe_message_layout);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mAdapter=new MessageAdapter(messagesList);
        mMessagesList.setAdapter(mAdapter);

////

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        FirebaseUser currentUser = mAuth.getCurrentUser();
        mUserRef.child("online").setValue(true);


        loadMessages();


        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image= dataSnapshot.child("image").getValue().toString();

                if(online.equals("true")){
                    mLastSeenView.setText("Online");

                }else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime);
                    mLastSeenView.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId +"/"+mChatUser,chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser +"/"+mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError!=null){
                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPAge++;
                itemPos=0;

                loadMoreMessages();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        mUserRef.child("online").setValue(true);

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
//            final String current_user_ref = "messages/" + mCurrentUserId +"/" + mChatUser;
//            final String chat_user_ref = "messages/" + mChatUser +"/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            final String push_id=user_message_push.getKey();

            final StorageReference filpath = mImageStorage.child("message_images").child(push_id+".jpg");

            filpath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                      getDownloadUrl(filpath);
                    }
                }
            });
        }


    }


    private  void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                setMessageImage(uri);
            }
        });

    }


    private void setMessageImage(Uri uri){

        final String current_user_ref = "messages/" + mCurrentUserId +"/" + mChatUser;
        final String chat_user_ref = "messages/" + mChatUser +"/" + mCurrentUserId;

        DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
        final String push_id=user_message_push.getKey();

        final String message_download_url = uri.toString();
        Map messageMap = new HashMap();
        messageMap.put("message",message_download_url);
        messageMap.put("seen",false);
        messageMap.put("type","image");
        messageMap.put("time", ServerValue.TIMESTAMP);
        messageMap.put("from",mCurrentUserId);

        Map messageUserMap=new HashMap();
        messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
        messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

        mChatMessageView.setText("");

        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null){
                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                }
            }
        });


    }



    private void loadMoreMessages(){

        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery=messageRef.orderByKey().endAt(mLastKey).limitToLast(10);



        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey= dataSnapshot.getKey();


                //mechanizm zapisywania od której wiadomości naliczyć kolejne 10 wiadomości  zabezpieczenie przed dublowaniem wiadomosci

                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);
                }else {
                    mPrevKey=mLastKey;
                }

                if(itemPos==1){
                    mLastKey=messageKey;
                }


                mAdapter.notifyDataSetChanged();

            //    mMessagesList.scrollToPosition(messagesList.size()-1); // ustawia widok listy na dole przy załadowaniu

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(itemPos,0);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    private void loadMessages() {
            //Only once use
            DatabaseReference messageRef=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
            Query messageQuery=messageRef.limitToLast(mCurrentPAge * TOTAL_ITEMS_TO_LOAD);



            messageQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Messages message = dataSnapshot.getValue(Messages.class);

                    itemPos++;

                    if(itemPos==1){

                        String messageKey= dataSnapshot.getKey();
                        mLastKey=messageKey;
                        mPrevKey=messageKey;
                    }


                    messagesList.add(message);
                    mAdapter.notifyDataSetChanged();

                    mMessagesList.scrollToPosition(messagesList.size()-1); // ustawia widok listy na dole przy załadowaniu

                    mRefreshLayout.setRefreshing(false);


                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    private void sendMessage(){
        String message= mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){

            String current_user_ref="messages/" + mCurrentUserId+"/"+mChatUser;
            String chat_user_ref="messages/"+mChatUser+"/"+mCurrentUserId;


            DatabaseReference user_message_push=mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            String push_id=user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });


        }

    }

}
