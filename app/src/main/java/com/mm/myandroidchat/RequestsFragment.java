package com.mm.myandroidchat;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mm.myandroidchat.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {


    private RecyclerView mRequestList;
    private View mMainView;
    private FirebaseAuth mAuth;
    private DatabaseReference mRequestDatabase;

    private DatabaseReference mUsersDatabase;
    private String mCurrent_user_id;

    private DatabaseReference mReqDatabase;
    private DatabaseReference mRootRef;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView=inflater.inflate(R.layout.fragment_requests, container, false);
        mRequestList=mMainView.findViewById(R.id.request_list);
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mRequestDatabase= FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mRequestDatabase.keepSynced(true);

        mReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");


        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);


        LinearLayoutManager linearLayoutManager=new LinearLayoutManager((getContext()));
        linearLayoutManager.setReverseLayout(true); //odwrac kolejnosc dodawania do recycleview
        linearLayoutManager.setStackFromEnd(true);

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(linearLayoutManager);

        mRootRef=FirebaseDatabase.getInstance().getReference();


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(mRequestDatabase, Request.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Request, RequestsFragment.RequestViewHolder>(options){


            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_single_row,parent,false);


                return new RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Request model) {

                final String list_user_id=getRef(position).getKey();
                Query lastRequestQuery=mRequestDatabase.child(list_user_id);
//                lastRequestQuery.addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                    //    String req_type= dataSnapshot.child("request_type").getValue().toString();
//                    //    holder.setRequestType(req_type);
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
                mRequestDatabase.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot!=null) {
                            String req_type = dataSnapshot.child("request_type").getValue().toString();
                            holder.setRequestType(req_type, list_user_id);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName=dataSnapshot.child("name").getValue().toString();
                        String userThumb= dataSnapshot.child("thumb_image").getValue().toString();
                        String userStatus=dataSnapshot.child("status").getValue().toString();

                        holder.setNameRequest(userName);
                        holder.setRequestImage(userThumb);
                        holder.setUserStatus(userStatus);

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });












            }




        };


        mRequestList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    public class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public RequestViewHolder( View itemView) {
            super(itemView);

            mView=itemView;
        }



        public void setRequestType(String request_type, final String list_user_id){
            Button buttonAccept = mView.findViewById(R.id.request_button_ok);
            Button buttonCancel=mView.findViewById(R.id.request_button_decline);

            if(request_type.equals("received")){
                buttonAccept.setText("Accept");
                buttonAccept.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                buttonAccept.setTextColor(Color.WHITE);
                buttonCancel.setVisibility(View.VISIBLE);
                buttonCancel.setEnabled(true);

                buttonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        acceptRequest(mCurrent_user_id,list_user_id);

                    }


                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        declineRequest(mCurrent_user_id,list_user_id);
                    }
                });



            }else if(request_type.equals("sent")){
                buttonAccept.setText("Cancel Friend Request");
                buttonCancel.setVisibility(View.GONE); // usuwa przycisk
                buttonAccept.getBackground().clearColorFilter();
                buttonCancel.setEnabled(false);


                buttonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        declineRequest(mCurrent_user_id,list_user_id);

                    }
                });


            }


        }


        public void setNameRequest(String userName){
            TextView userNameView=mView.findViewById(R.id.request_single_name);
            userNameView.setText(userName);


        }

        public void setRequestImage(String thumb){

            CircleImageView userImageView = mView.findViewById(R.id.request_single_avatar);
            Picasso.get().load(thumb).placeholder(R.drawable.avatar_1).into(userImageView);
        }

        public void setUserStatus(String status){

            TextView userStatus=mView.findViewById(R.id.request_single_status);
            userStatus.setText(status);


        }









    }

    //--------ACCEPT REQUEST------------

    public void acceptRequest(String mCurrent_user_id, String list_user_id ){

        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

        Map friendsMap = new HashMap();
        friendsMap.put("Friends/"+mCurrent_user_id + "/" + list_user_id + "/date", currentDate);
        friendsMap.put("Friends/"+list_user_id+"/"+mCurrent_user_id + "/date", currentDate);

        friendsMap.put("Friend_req/" + mCurrent_user_id +"/"+list_user_id, null);
        friendsMap.put("Friend_req/" + list_user_id +"/"+mCurrent_user_id, null);

        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError==null){

                    Toast.makeText(getContext(),"You Accepted the Request !",Toast.LENGTH_SHORT).show();

                }else {

                    String error = databaseError.getMessage();
                    Toast.makeText(getContext(),error,Toast.LENGTH_SHORT).show();
                }


            }
        });

    }


    //------DECLINE REQUEST-------------

    public void declineRequest(final String mCurrent_user_id, final String list_user_id){
        mRequestDatabase.child(list_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mReqDatabase.child(list_user_id).child(mCurrent_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getContext(), "Canceled", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });
    }





}
