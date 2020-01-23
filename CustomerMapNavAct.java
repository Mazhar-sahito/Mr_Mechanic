package com.example.mazharali.projectfyp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapNavAct extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{
    private GoogleMap mMap;
    private GoogleApiClient mgoogleApiClient;
    private Location mlastlocation;
    private LocationRequest mlocationRequest;
    private LatLng pickuplocation;
    private Button logout , callaMechanic;
    private Boolean requestbool = false;
    private Marker pickupMarker;
    private TextView t1 ;
    private ImageView profileImage;
    RadioGroup radioGroup;
    private LinearLayout mechanicInfo;
    private ImageView mechanicProfilePhoto;
    private TextView mechanicName , mechanicPhone , mechanicNic;

    String username , requestService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

//        String provider = locationManager.getBestProvider(criteria, true);
//        Location location = locationManager.getLastKnownLocation(provider);

        callaMechanic=(Button)findViewById(R.id.request_call);
        profileImage = (ImageView)findViewById(R.id.profile_photo);
        radioGroup = (RadioGroup) findViewById(R.id.radio_Group);
        radioGroup.check(R.id.bike);

        mechanicInfo=(LinearLayout)findViewById(R.id.MechanicInfo);
        mechanicProfilePhoto=(ImageView)findViewById(R.id.Mechanicprofile);
        mechanicName=(TextView) findViewById(R.id.MechanicName);
        mechanicPhone=(TextView) findViewById(R.id.MechanicPhone);
        mechanicNic=(TextView) findViewById(R.id.MechanicNic);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CustomerMapNavAct.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                    , android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);

        } else {
            mapFragment.getMapAsync(this);

        }

        callaMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(requestbool){
                    endService();

                }else {

                    int selectId = radioGroup.getCheckedRadioButtonId();
                    final RadioButton radioButton= (RadioButton)findViewById(selectId);
                    if (radioButton.getText()==null){
                        return;
                    }

                    requestService= radioButton.getText().toString();

                    requestbool= true;
                    String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Customer_Request");
                    GeoFire geoFire= new GeoFire(databaseReference);
                    geoFire.setLocation(user_id , new GeoLocation(mlastlocation.getLatitude(),mlastlocation.getLongitude()));

                    pickuplocation= new LatLng(mlastlocation.getLatitude(), mlastlocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickuplocation).title("Service need here").
                    icon(BitmapDescriptorFactory
                            .fromResource(R.mipmap.pickup)));
                    callaMechanic.setText("Getting your Mechanic ");

                    getClosestMechanic();
                }

            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user== null){
            Toast.makeText(CustomerMapNavAct.this , "User Not Signed In" , Toast.LENGTH_SHORT).show();
        }else {
            username= user.getUid();

            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                    .child(username);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String , Object> map =(Map<String , Object>)dataSnapshot.getValue();
                    if(map.get("name")!= null && dataSnapshot.getChildrenCount()>0){
                        String  nameofUser= map.get("name").toString();
                        t1= (TextView)findViewById(R.id.text_name);
                        t1.setText(nameofUser);
                    }
                        if(map.get("profileImageUri")!= null && dataSnapshot.getChildrenCount()>0){
                           String profileUri= map.get("profileImageUri").toString();
                            profileImage = (ImageView)findViewById(R.id.profile_photo);
                            Glide.with(getApplication()).load(profileUri).into(profileImage);
//                            Glide.with(getApplicationContext()).load(profileUri).into(profileImage);
                        }
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private  int radius = 1;
    private  boolean mechanicFound = false;
    private String mechanicFoundId;
    GeoQuery geoQuery;
    private void getClosestMechanic(){

        DatabaseReference mechanicLocation = FirebaseDatabase.getInstance().getReference().child("Available_Mechanics");
        GeoFire geoFire= new GeoFire(mechanicLocation);
        geoQuery= geoFire.queryAtLocation(new GeoLocation(pickuplocation.latitude, pickuplocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!mechanicFound && requestbool) {

                    DatabaseReference customerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics")
                            .child(key);
                    customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                                Map<String, Object> mechanicMap = (Map<String, Object>) dataSnapshot.getValue();
                                if(mechanicFound){
                                    return;
                                }
                                if (mechanicMap.get("service") !=null && requestService.toString()!=null && mechanicMap.get("service").equals(requestService)) {
                                    mechanicFound = true;
                                    mechanicFoundId = dataSnapshot.getKey();


                                    DatabaseReference mechanicref = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics")
                                            .child(mechanicFoundId);
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap hashMap = new HashMap();
                                    hashMap.put("CustomerServiceId", customerId);
                                    mechanicref.updateChildren(hashMap);

                                    getMechanicLocation();
                                    getMechanicInfo();
                                    hasServiceEnded();
                                    callaMechanic.setText("Searching for Mechanic ..");


                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!mechanicFound){
                    radius++;
                    getClosestMechanic();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mechMarker;
    private DatabaseReference mechanicLocref;
    private ValueEventListener mechanicLocrefListener;
    private void getMechanicLocation(){

        mechanicLocref= FirebaseDatabase.getInstance().getReference().child("Mechanic_Working")
                .child(mechanicFoundId).child("l");
        mechanicLocrefListener = mechanicLocref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestbool ){
                    List<Object> map= (List<Object>) dataSnapshot.getValue();
                    double locationLat= 0;
                    double locationLng= 0;
                    callaMechanic.setText("Mechanic Found");
                    if(map.get(0)!= null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!= null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng mechanicLatLng =new LatLng(locationLat , locationLng);
                    if (mechMarker!=null){
                        mechMarker.remove();
                    }
                    Location loc1=new Location("");
                    loc1.setLatitude(pickuplocation.latitude);
                    loc1.setLongitude(pickuplocation.longitude);

                    Location loc2=new Location("");
                    loc2.setLatitude(mechanicLatLng.latitude);
                    loc2.setLongitude(mechanicLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if (distance<100){
                        callaMechanic.setText("Mechanic is here ");
                    }else{
                        callaMechanic.setText("Mechanic Found : "+ String.valueOf(distance));
                    }

                    mechMarker = mMap.addMarker(new MarkerOptions().position(mechanicLatLng).title("Your Mechanic")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.mipmap.mechanic)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getMechanicInfo(){
        mechanicInfo.setVisibility(View.VISIBLE);
        final DatabaseReference customerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(mechanicFoundId);
        customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String , Object> map =(Map<String , Object>)dataSnapshot.getValue();
                    if(map.get("name")!= null){
                        mechanicName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!= null){
                        mechanicPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("nic")!= null){
                        mechanicNic.setText(map.get("nic").toString());
                    }
                    if(map.get("profileImageUri")!= null){
                        Glide.with(getApplication()).load( map.get("profileImageUri").toString()).into(mechanicProfilePhoto);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference serviceHasEndedRef;
    private ValueEventListener serviceHasEndedRefListener;


    private void hasServiceEnded(){
        final DatabaseReference serviceHasEndedRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics")
                .child(mechanicFoundId).child("CustomerServiceId");

        serviceHasEndedRefListener=  serviceHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                } else {
                    endService();

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void endService(){
        requestbool= false;

        geoQuery.removeAllListeners();
        mechanicLocref.removeEventListener(mechanicLocrefListener);

        serviceHasEndedRef.removeEventListener(serviceHasEndedRefListener);
        if (mechanicFoundId != null){
            DatabaseReference mechanicref = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics")
                    .child(mechanicFoundId).child("Customer_Request");
            mechanicref.removeValue();
            mechanicFoundId=  null;
        }
        mechanicFound= false;
        radius= 1;

        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Customer_Request");
        GeoFire geoFire= new GeoFire(databaseReference);
        geoFire.removeLocation(user_id);
        if (pickupMarker!=null){
            pickupMarker.remove();
        }
        callaMechanic.setText("call Mechanic");

        mechanicInfo.setVisibility(View.GONE);
        mechanicName.setText("");
        mechanicPhone.setText("");
        mechanicNic.setText("");
        mechanicProfilePhoto.setImageResource(R.drawable.logo);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapNavAct.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mgoogleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CustomerMapNavAct.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mlocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mlastlocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

    }

    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                } else {

                    Toast.makeText(this, "Error Please provide Information", Toast.LENGTH_SHORT).show();

                }
                break;
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.customer_map_nav, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.history) {
            // Handle the camera action

            Intent intent= new Intent(CustomerMapNavAct.this, HistoryActivity.class);
            intent.putExtra("customerOrMechanic", "Customers");
            startActivity(intent);

        } else if (id == R.id.payment) {

            Intent intent= new Intent(CustomerMapNavAct.this, Payment_detail.class);
            startActivity(intent);

        } else if (id == R.id.rates) {
            Intent intent = new Intent (CustomerMapNavAct.this,ListForRepair.class);
            startActivity(intent);

        } else if (id == R.id.setting) {
            Intent intent = new Intent (CustomerMapNavAct.this,CustomerSetting.class);
            startActivity(intent);


        }
        else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent =new Intent(CustomerMapNavAct.this, MainActivity.class);
            startActivity(intent);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();


    }




}
