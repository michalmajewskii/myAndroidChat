package com.mm.myandroidchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.mm.myandroidchat.R;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mToolbar =findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Chat");


        mViewPager= findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);


        mTabLayout=findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        if(mAuth.getCurrentUser()!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();


        if(currentUser==null) // null means not log in
             {

                 sendToStart();

        }
        else {
            mUserRef.child("online").setValue(true);

        }
    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if(currentUser != null){
//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP); //when online is true, that means that user is online. If online is Timestamp - user is offline
//          //  mUserRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);
//
//
//
//        }
//
//
//
//    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId()==R.id.main_logout_btn){

             mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
             FirebaseAuth.getInstance().signOut();


                sendToStart();

         }

         if(item.getItemId()==R.id.main_settings_btn){
             Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
             startActivity(settingsIntent);

         }
         if (item.getItemId()==R.id.main_all_users){

             Intent settingsIntent = new Intent(MainActivity.this, UsersActivity.class);
             startActivity(settingsIntent);

         }


         return true;
    }
}
