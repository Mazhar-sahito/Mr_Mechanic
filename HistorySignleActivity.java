package com.example.mazharali.projectfyp;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.Map;

public class HistorySignleActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String serviceId , currentUserId , customerId , mechanicId , userMechanicOrCustomer;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private TextView serviceLoc;
    private TextView serviceDate;
    private TextView userName;
    private TextView userPhone;
    private LatLng pickupLatLng;

    private ImageView userImage;
    private DatabaseReference historyDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_signle);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        serviceId = getIntent().getExtras().getString("serviceId");

        serviceLoc = (TextView)findViewById(R.id.service_loc);
        serviceDate = (TextView)findViewById(R.id.service_date);
        userName = (TextView)findViewById(R.id.user_Name);
        userPhone = (TextView)findViewById(R.id.user_Phone);

        userImage = (ImageView)findViewById(R.id.mechanic_Image);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        historyDbRef = FirebaseDatabase.getInstance().getReference().child("history").child(currentUserId);
        getServiceInfo();

    }

    private void getServiceInfo() {
        historyDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getValue().equals("customer")) {
                            customerId = child.getValue().toString();
                            if (!customerId.equals(currentUserId)) {
                                userMechanicOrCustomer = "Mechanics";
                                getUserInformation("Customers" , customerId);
                            }
                        }
                        if (child.getValue().equals("mechanic")) {
                            mechanicId = child.getValue().toString();
                            if (!mechanicId.equals(currentUserId)) {
                                userMechanicOrCustomer = "Customers";
                                getUserInformation("Mechanics" , mechanicId);
                            }
                        }
//                        if (child.getValue().equals("timestamp")) {
//                          //  serviceDate.setText(getDate(Long.valueOf(child.getValue().toString())));
//                        }
                        if (child.getValue().equals("location")) {
                            pickupLatLng = new LatLng(Double.valueOf(child.child("Lat").getValue().toString()) ,
                                    Double.valueOf( child.child("Lat").getValue().toString()));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserInformation(String otherUserMecOrCustomers, String otherUsersId) {
        DatabaseReference otherUserdb= FirebaseDatabase.getInstance().getReference().child("Users")
                .child(otherUserMecOrCustomers).child(otherUsersId);
        otherUserdb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String , Object> map = (Map<String , Object>) dataSnapshot.getValue();
                    if (map.get("name")!=null){
                        userName.setText(map.get("name").toString());
                    }
                    if (map.get("phone")!=null){
                        userPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("profileImageUri")!=null){
                        Glide.with(getApplicationContext()).load(map.get("profileImageUri").toString()).into(userImage);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


//    public String getDate(Long time){
//        Calendar cal= Calendar.getInstance(Locale.getDefault());
//        cal.setTimeInMillis(time*1000);
//        //String date= DateFormat.format("MM-dd-yyyy hh:mm:ss" , cal).toString();
//        return date;
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setMyLocationEnabled(true);
    }



}
