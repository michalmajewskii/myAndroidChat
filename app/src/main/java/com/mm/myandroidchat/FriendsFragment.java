package com.mm.myandroidchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mm.myandroidchat.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView mFriendsList;
    private DatabaseReference mFridendsDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;
    private DatabaseReference mUsersDatabase;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView= inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList=mMainView.findViewById(R.id.friends_list);
        mAuth=FirebaseAuth.getInstance();

        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mFridendsDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFridendsDatabase.keepSynced(true); // offline feature
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);//offline feature

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;



    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFridendsDatabase, Friends.class)
                        .build();


        FirebaseRecyclerAdapter firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder>(options) {


            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_row,parent,false);
                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {

                holder.setDate(model.getDate());
                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String username = dataSnapshot.child("name").getValue().toString();
                        String string=dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")){

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);

                        }



                        holder.setDisplayName(username);
                        holder.setUserImage(string);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {

                                        if(i==0){
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("value of userid", list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if(i==1){
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("value of userid", list_user_id);
                                            chatIntent.putExtra("user_name",username);
                                            startActivity(chatIntent);

                                        }


                                    }
                                });
                                builder.show();

                            }
                        });



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        };

        mFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;

        }
        public void setDate(String date){

            TextView userNameView = mView.findViewById(R.id.user_single_status);
            userNameView.setText(date);


        }

        public void setDisplayName (String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

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
