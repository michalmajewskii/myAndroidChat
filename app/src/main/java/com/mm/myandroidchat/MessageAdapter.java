package com.mm.myandroidchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mm.myandroidchat.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;

    private FirebaseUser firebaseUser;

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent,false);
            return new MessageViewHolder(view);
        }else {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent,false);
            return new MessageViewHolder(view);
        }


       // View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent,false);

       // return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        mAuth=FirebaseAuth.getInstance();
        final String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);
        final String from_user = c.getFrom();
        String message_type=c.getType();


        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String current_name = dataSnapshot.child("name").getValue().toString();
             //   holder.messageName.setText(current_name);

                if(!from_user.equals(current_user_id)) {
                    final String current_image = dataSnapshot.child("thumb_image").getValue().toString();
                    Picasso.get().load(current_image).placeholder(R.drawable.avatar_1).into(holder.profileImage);
                }


            }




            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        if(from_user.equals(current_user_id)){
//
//        }else {
//            ((MessageViewHolder)holder).messageText.setBackgroundResource(R.drawable.message_text_background);
//           ((MessageViewHolder)holder).messageText.setTextColor(Color.WHITE);
//        }


        if (message_type.equals("text")) {
            holder.messageText.setText(c.getMessage());
           holder.messageImage.setVisibility(View.GONE);
        }

        else {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);

            Picasso.get().load(c.getMessage()).placeholder(R.drawable.avatar_1).into(holder.messageImage); //TODO insert in placeholder thumbnail
        }






    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder{


        View mView;
        public TextView messageText, messageName, messageTime;
        public CircleImageView profileImage;
        public ImageView messageImage;

        public MessageViewHolder(View view){
            super(view);

            mView=view;
//            messageText=view.findViewById(R.id.message_text);
//            profileImage=view.findViewById(R.id.message_user_image);
//           messageName=view.findViewById(R.id.message_name);
            messageImage=view.findViewById(R.id.message_imageView);
            messageText=view.findViewById(R.id.show_message);
            profileImage=view.findViewById(R.id.message_user_image);


        }




        }



        @Override
        public int getItemViewType(int position){
           firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
            Messages c = mMessageList.get(position);
            String from_user = c.getFrom();
            if(from_user.equals(firebaseUser.getUid())){
                return MSG_TYPE_RIGHT;
            }else return MSG_TYPE_LEFT;


        }



    }




