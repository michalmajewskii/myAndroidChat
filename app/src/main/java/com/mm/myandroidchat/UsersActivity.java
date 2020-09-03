package com.mm.myandroidchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mm.myandroidchat.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");



        mUsersList= findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));



    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUsersDatabase, Users.class)
                        .build();


        FirebaseRecyclerAdapter firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {


            @Override
            public UsersViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_row,parent,false);

                return new UsersViewHolder(view);
            }


            @Override
            protected void onBindViewHolder( UsersViewHolder holder, int position, Users model) {
                holder.setDisplayName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setUserImage(model.getThumb_image());



                final String user_id = getRef(position).getKey();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(UsersActivity.this, ProfileActivity.class);
                        intent.putExtra("value of userid",user_id);
                        startActivity(intent);

                    }
                });

            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView= itemView;

        }

        public void setDisplayName (String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setStatus(String status){
            TextView userStatusView= mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumb){

            CircleImageView userImageView = mView.findViewById(R.id.user_single_avatar);
            Picasso.get().load(thumb).placeholder(R.drawable.avatar_1).into(userImageView);
        }

    }


}
