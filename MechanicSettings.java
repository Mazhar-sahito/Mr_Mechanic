package com.example.mazharali.projectfyp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MechanicSettings extends AppCompatActivity {
    private EditText cNameField , cPhoneField , nic;
    private Button cConfirm , cBack;
    RadioGroup radioGroup;

    private ImageView profilephoto;

    private FirebaseAuth cAuth;
    private DatabaseReference mechanicDatabase;
    String userId ;
    String cService;
    private Uri resultUri;
    String profileUri;


    String cName , cPhone , cNic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_settings);


        nic = (EditText)findViewById(R.id.nic_mechanic);
        cNameField = (EditText)findViewById(R.id.customer_name);
        cPhoneField = (EditText)findViewById(R.id.customer_phone);
        cConfirm= (Button)findViewById(R.id.customer_confirm_btn);
        cBack = (Button)findViewById(R.id.back_btn);
        profilephoto= (ImageView)findViewById(R.id.profile_image);
        radioGroup = (RadioGroup) findViewById(R.id.radio_Group);

        cAuth = FirebaseAuth.getInstance();
        userId = cAuth.getCurrentUser().getUid();
        mechanicDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userId);

        // Calling the function which will get customers data from Database
        getCustomerInfo();

        profilephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent , 1);

            }
        });

        cConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCustomerInfo();
            }
        });

        cBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

    }

    private void getCustomerInfo(){
        mechanicDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String , Object> map =(Map<String , Object>)dataSnapshot.getValue();
                    if(map.get("name")!= null){
                        cName= map.get("name").toString();
                        cNameField.setText(cName);
                    }
                    if(map.get("phone")!= null){
                        cPhone= map.get("phone").toString();
                        cPhoneField.setText(cPhone);
                    }
                    if(map.get("nic")!= null){
                        cNic= map.get("nic").toString();
                        nic.setText(cNic);
                    }
                    if(map.get("service")!= null){
                        cService= map.get("service").toString();
                        switch (cService){
                            case "Bike":
                                radioGroup.check(R.id.bike);
                                break;
                            case "Car":
                                radioGroup.check(R.id.car);
                                break;
                            case "Both":
                                radioGroup.check(R.id.both);
                                break;
                        }
                    }
                    if(map.get("profileImageUri")!= null){
                        profileUri= map.get("profileImageUri").toString();
                        Glide.with(getApplication()).load(profileUri).into(profilephoto);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void saveCustomerInfo() {
        cName = cNameField.getText().toString();
        cPhone = cPhoneField.getText().toString();
        cNic = nic.getText().toString();

        int selectId = radioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(selectId);
        if (radioButton.getText() == null) {
            return;
        }

        cService = radioButton.getText().toString();

        Map customerInfo = new HashMap();
        customerInfo.put("name", cName);
        customerInfo.put("phone", cPhone);
        customerInfo.put("nic", cNic);
        customerInfo.put("service", cService);
        mechanicDatabase.updateChildren(customerInfo);


        if (resultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("Profile_Images").child(userId);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;

                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    Map newImage = new HashMap();
                    newImage.put("profileImageUri", downloadUri.toString());
                    mechanicDatabase.updateChildren(newImage);
                    finish();
                    return;
                }
            });
        } else {
            finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode== Activity.RESULT_OK);
        final Uri imageUri = data.getData();
        resultUri = imageUri;
        profilephoto.setImageURI(resultUri);
    }
}
