package com.mm.myandroidchat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mm.myandroidchat.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    public ChatsFragment() {
        // Required empty public constructor
    }

    private RecyclerView mConvList;
    private View mMainView;
    private FirebaseAuth mAuth;
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private String mCurrent_user_id;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView=inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList=mMainView.findViewById(R.id.conv_list);
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mConvDatabase=FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mConvDatabase.keepSynced(true);

        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase=FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);


        LinearLayoutManager linearLayoutManager=new LinearLayoutManager((getContext()));
        linearLayoutManager.setReverseLayout(true); //odwrac kolejnosc dodawania do recycleview
        linearLayoutManager.setStackFromEnd(true);


        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(conversationQuery, Conv.class)
                        .build();


        FirebaseRecyclerAdapter firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Conv, ChatsFragment.ConvViewHolder>(options){

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_row,parent,false);

                return new ConvViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull Conv model) {

            final String list_user_id=getRef(position).getKey();

            Query lastMessageQuery=mMessageDatabase.child(list_user_id).limitToLast(1);
            lastMessageQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    if(dataSnapshot.child("type").getValue().equals("text")) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessageConv(data, Conv.isSeen());
                    }else if(dataSnapshot.child("type").getValue().equals("image")){

                        if(dataSnapshot.child("from").getValue().equals(mCurrent_user_id)){

                            String data= "You sent a photo.";
                            holder.setMessageConv(data, Conv.isSeen());


                        }else if (dataSnapshot.child("from").getValue().equals(list_user_id)){
                            mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    final String userName= dataSnapshot.child("name").getValue().toString();
                                    String data= userName + " sent you a photo.";
                                    holder.setMessageConv(data, Conv.isSeen());


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                            }

                    }
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


            mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final String userName=dataSnapshot.child("name").getValue().toString();
                    String userThumb= dataSnapshot.child("thumb_image").getValue().toString();

                    if(dataSnapshot.hasChild("online")){

                        String userOnline=dataSnapshot.child("online").getValue().toString();
                        holder.setUserOnline(userOnline);

                    }

                    holder.setNameConv(userName);
                    holder.setUserImage(userThumb);


                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                            chatIntent.putExtra("value of userid", list_user_id);
                            chatIntent.putExtra("user_name", userName);
                            startActivity(chatIntent);


                        }
                    });



                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            }
        };

        mConvList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();






    }


    public class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView){
                super(itemView);

                mView=itemView;

        }


        public void setMessageConv (String message, boolean isSeen){

            TextView userStatusView= mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);
            if(!isSeen){

                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            }else {
                userStatusView.setTypeface(userStatusView.getTypeface(),Typeface.NORMAL);

            }
        }

        public void setNameConv(String userName){
            TextView userNameView=mView.findViewById(R.id.user_single_name);
            userNameView.setText(userName);


        }

        public void setUserImage(String thumb){

            CircleImageView userImageView = mView.findViewById(R.id.user_single_avatar);
            Picasso.get().load(thumb).placeholder(R.drawable.avatar_1).into(userImageView);
        }


        public void setUserOnline(String online){
            ImageView userOnlineView= mView.findViewById(R.id.user_single_online_icon);

            if(online.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }


        }


    }
}
