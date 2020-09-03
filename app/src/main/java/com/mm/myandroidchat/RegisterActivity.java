package com.mm.myandroidchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mm.myandroidchat.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText mDisplayName;
    EditText  mEmail;
    EditText  mPassword;
    Button  mCreateBtn;

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ProgressDialog mRegProgress;

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();




        mToolbar=findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mRegProgress=new ProgressDialog(this);


        mDisplayName =  findViewById(R.id.reg_name);
        mEmail=findViewById(R.id.reg_email);
        mPassword=findViewById(R.id.reg_password);
        mCreateBtn=findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name=mDisplayName.getText().toString();
                String email=mEmail.getText().toString();
                String password=mPassword.getText().toString();

              if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password) ){

                    if(password.length()>5) {
                        mRegProgress.setTitle("Registering User");
                        mRegProgress.setMessage("Please wait while we create your account !");
                        mRegProgress.setCanceledOnTouchOutside(false);
                        mRegProgress.show();

                        register_user(display_name, email, password);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Create your password using 6 characters or more.",Toast.LENGTH_LONG).show();
                    }

                }


            }
        });
    }


    private void register_user(final String display_name, String email, String password){

       mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {
               if (task.isSuccessful()){

                   FirebaseUser current_user= FirebaseAuth.getInstance().getCurrentUser();
                   String uid = current_user.getUid();
                   mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                   HashMap<String,String> userMap = new HashMap<>();
                   userMap.put("name", display_name);
                   userMap.put("status", "Hi there, I'm using Chat App.");
                   userMap.put("image","default");
                   userMap.put("thumb_image", "default");
                   String deviceToken= FirebaseInstanceId.getInstance().getToken();
                   userMap.put("device_token", deviceToken);

                   mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if(task.isSuccessful()){
                               Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                               mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //wstacz wylacza aplikacje a nie cofa do wczesniejszego activity
                               startActivity(mainIntent);
                               finish();
                           }
                       }
                   });


               }else {
                   mRegProgress.hide();
                   Toast.makeText(RegisterActivity.this,"Cannot Sign in. Please check the form and try again.", Toast.LENGTH_SHORT).show();
               }
           }
       });


    }



}
