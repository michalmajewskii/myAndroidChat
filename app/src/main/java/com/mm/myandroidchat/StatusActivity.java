package com.mm.myandroidchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mm.myandroidchat.R;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button mSavebtn;
    private EditText mStatus;


    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;


    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid= mCurrentUser.getUid();



        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        mToolbar = findViewById(R.id.status_appBarr);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mStatus=findViewById(R.id.status_input);
        mSavebtn=findViewById(R.id.status_save_btn);

        String status_value = getIntent().getStringExtra("value of current status");

        mStatus.setText(status_value);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgress=new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();



                String status = mStatus.getText().toString();


                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();


                        }else {

                            Toast.makeText(getApplicationContext(),"There was some error in saving Changes.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });




    }
}
