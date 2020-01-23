package com.example.mazharali.projectfyp;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.mazharali.projectfyp.R.id.custom;
import static com.example.mazharali.projectfyp.R.id.map;

public class MechanicMapAct extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener ,RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient mgoogleApiClient;
    Location mlastlocation;
    LocationRequest mlocationRequest;
    private Button logout , settings , serviceStatusBtn;
    private int status = 0;
    private Switch workingSwitch;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};


    private String customerId = "";
    private boolean isLoggingOut= false;

    // private SupportMapFragment mapFragment;
    private LinearLayout customerInfo;
    private ImageView customerProfilePhoto;
    private TextView customerName , customerPhone ;
//    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_map);
        logout=(Button)findViewById(R.id.logout);
        settings =(Button)findViewById(R.id.setting_for_mechanic);
        serviceStatusBtn =(Button)findViewById(R.id.serviceStatus);
        workingSwitch = (Switch) findViewById(R.id.working_switch);
        polylines = new ArrayList<>();

        serviceStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (status){
                    case 1:
                        status = 2;
                        erasePolylines();
                        serviceStatusBtn.setText("Service Completed");
                        break;
                    case 2:
                        recordService();
                        endService();
                        break;
                }
            }
        });


        workingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    connectMechanic();
                }else {
                    dissconnectDriver();
                }
            }
        });

        customerInfo=(LinearLayout)findViewById(R.id.customerInfo);
        customerProfilePhoto=(ImageView)findViewById(R.id.customerprofile);
        customerName=(TextView) findViewById(R.id.customerName);
        customerPhone=(TextView) findViewById(R.id.customerPhone);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MechanicMapAct.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }else {
            mapFragment.getMapAsync(this);

        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isLoggingOut=true;
                dissconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent =new Intent(MechanicMapAct.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MechanicMapAct.this , MechanicSettings.class);
                startActivity(intent);
                finish();
                return;
            }
        });


        getAssignedCustomer();


    }

    private void getAssignedCustomer(){
        String mechanicId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignedCustomerId= FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics")
                .child(mechanicId).child("CustomerServiceId");

        assignedCustomerId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){
                   status = 1;
                   customerId= dataSnapshot.getValue().toString();
                   getAssignedCustomerPickupLoc();
                   getAssignedCustomerInfo();
               }else {
                   endService();
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLoc;
    private ValueEventListener assignedCustomerPickupLocListener;
    private void getAssignedCustomerPickupLoc(){

         assignedCustomerPickupLoc = FirebaseDatabase.getInstance().getReference().child("Customer_Request")
                .child(customerId).child("i");

        assignedCustomerPickupLocListener = assignedCustomerPickupLoc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map= (List<Object>) dataSnapshot.getValue();
                    double locationLat= 0;
                    double locationLng= 0;
                    if(map.get(0)!= null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!= null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng mechanicLatLng =new LatLng(locationLat , locationLng);
                    pickupMarker= mMap.addMarker(new MarkerOptions().position(mechanicLatLng).title("pickup Location").icon(
                            BitmapDescriptorFactory.fromResource(R.mipmap.pickup)));
                    getRouterToMarker(mechanicLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRouterToMarker (LatLng mechanicLatLng){
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mlastlocation.getLatitude(), mlastlocation.getLongitude()), mechanicLatLng)
                .build();
        routing.execute();

    }

    private void getAssignedCustomerInfo(){
        customerInfo.setVisibility(View.VISIBLE);
        final DatabaseReference customerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String , Object> map =(Map<String , Object>)dataSnapshot.getValue();
                    if(map.get("name")!= null){
                        customerName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!= null){
                       customerPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUri")!= null){
                        Glide.with(getApplication()).load( map.get("profileImageUri").toString()).into(customerProfilePhoto);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void endService(){
        serviceStatusBtn.setText("Served Customer");
        erasePolylines();

        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mechanicref = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics")
                .child(user_id).child("Customer_Request");
        mechanicref.removeValue();

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Customer_Request");
        GeoFire geoFire= new GeoFire(databaseReference);
        geoFire.removeLocation(customerId);
        customerId ="";
        if (pickupMarker!=null){
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocListener != null){
            assignedCustomerPickupLoc.removeEventListener(assignedCustomerPickupLocListener );
        }
        customerInfo.setVisibility(View.GONE);
        customerName.setText("");
        customerPhone.setText("");
        customerProfilePhoto.setImageResource(R.drawable.logo);
    }


    private void recordService(){
        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mechanicref = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics")
                .child(user_id).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                .child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");

        String requestServiceId = historyRef.push().getKey();
        mechanicref.child(requestServiceId).setValue(true);
        customerRef.child(requestServiceId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver" , user_id);
        map.put("customer" , customerId);
        map.put("location/Lat" , mlastlocation.getLatitude());
        map.put("location/Lng", mlastlocation.getLongitude());
        map.put("rating" , 0);
        historyRef.child(requestServiceId).updateChildren(map);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mgoogleApiClient= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mgoogleApiClient.connect();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext()!=null) {
            mlastlocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("Available_Mechanics");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("Mechanic_Working");
            GeoFire geoFirerefAvailable = new GeoFire(refAvailable);
            GeoFire geoFirerefWorking = new GeoFire(refWorking);

            switch (customerId) {
                case "":
                    geoFirerefWorking.removeLocation(user_id);
                    geoFirerefAvailable.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFirerefAvailable.removeLocation(user_id);
                    geoFirerefWorking.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }



        }


    }

    final  int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_REQUEST_CODE :
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(map);
                    mapFragment.getMapAsync(this);
                }else {

                    Toast.makeText(this, "Error Please provide Permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    private void connectMechanic(){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MechanicMapAct.this , new String[] {Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION} , LOCATION_REQUEST_CODE );

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mlocationRequest, this);

    }


    private void dissconnectDriver(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient, this);

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Available_Mechanics");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(user_id);
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for (Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

}