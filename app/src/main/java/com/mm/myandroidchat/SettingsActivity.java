package com.mm.myandroidchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.mm.myandroidchat.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;


    //Layout
    private CircleImageView mImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusBtn;
    private Button mImageBtn;

    private static final int GALLERY_PICK = 1 ;


    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mImage=findViewById(R.id.settings_image);
        mName=findViewById(R.id.settings_name);
        mStatus=findViewById(R.id.settings_status);

        mStatusBtn=findViewById(R.id.settings_status_btn);
        mImageBtn=findViewById(R.id.settings_image_btn);


        mImageStorage= FirebaseStorage.getInstance().getReference();//dzięki temu wie gdzie wrzucić


        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        String current_uid=mCurrentUser.getUid();



        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true); //for offline, keeps only int and string data type

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image= dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if(!image.equals("default")) {
                    //setPictureFromURL() - ustawic defoultowe zdjecie;

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.avatar_1).into(mImage);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String current_status= mStatus.getText().toString();


                Intent status_intent= new Intent(SettingsActivity.this,StatusActivity.class);
                status_intent.putExtra("value of current status",current_status);
                startActivity(status_intent);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT_IMAGE"),GALLERY_PICK);



            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_PICK && resultCode==RESULT_OK){
               Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);


        }



        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog=new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();


                Uri resultUri = result.getUri();

                final String current_user_id = mCurrentUser.getUid();




                File thumb_filePath = new File(resultUri.getPath());

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id +".jpg");






               // StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");

                final StorageReference reference= FirebaseStorage.getInstance().getReference().child("profile_images").child(current_user_id +".jpg");




                reference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       getDownloadUrl(reference);

                        UploadTask uploadTask=thumb_filepath.putBytes(thumb_byte);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                getThumbUrl(thumb_filepath);
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "error",Toast.LENGTH_SHORT).show();
                    }
                });


                 } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }


    private  void getDownloadUrl(StorageReference reference){
            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    setUserProfileUrl(uri);

                }
            });

    }

private void setUserProfileUrl(Uri uri){

    final String download_url = uri.toString();
    mUserDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                  if(task.isSuccessful()){
                                      mProgressDialog.dismiss();
                                      Toast.makeText(SettingsActivity.this, "Success Uploading", Toast.LENGTH_SHORT).show();
                                      setPictureFromURL(download_url,mImage);

                                  }else {
                                      Toast.makeText(SettingsActivity.this, "notWorking", Toast.LENGTH_SHORT).show();
                                         mProgressDialog.dismiss();
                                  }
                                }
                            });


}


    public void setPictureFromURL(String downloard_url, CircleImageView mImage ){
        Picasso.get().load(downloard_url).into(mImage);
    }


    private  void getThumbUrl(StorageReference reference){
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                setUserThumbUrl(uri);

            }
        });

    }



    private void setUserThumbUrl(Uri uri){

        final String thumb_url = uri.toString();
        mUserDatabase.child("thumb_image").setValue(thumb_url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mProgressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, "Success Uploading", Toast.LENGTH_SHORT).show();


                }else {
                    Toast.makeText(SettingsActivity.this, "notWorking", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
            }
        });


    }


}
