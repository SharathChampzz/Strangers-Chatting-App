package com.example.backgroundloc;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    static MapsActivity instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    Marker marker;


    ImageView refresh;
    Button nearbyusers;

    List<Marker> ObjectList = new ArrayList<Marker>();

    public static MapsActivity getInstance() {
        return instance;
    }

    DatabaseReference current = FirebaseDatabase.getInstance().getReference("NearByUsers/");

    String country = "", state = "", pincode = "", oldpincode = "";

    int height = 200;
    int width = 200;
    Bitmap user = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        instance = this;
        nearbyusers = findViewById(R.id.nearby);
        refresh = findViewById(R.id.refreshbtn);


        Toast.makeText(this, StorageClass.phonenumber, Toast.LENGTH_SHORT).show();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must Grant Permission to make it work!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nearbyusers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (country == null || state == null || pincode == null) {
                    Toast.makeText(MapsActivity.this, "Thuu.. Nin Janmake... Ondhu Adress ilvalo nin iro Jagake... 😶", Toast.LENGTH_SHORT).show();
                } else {
                    String path = "NearByUsers/" + country + "/" + state + "/" + pincode;
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(path);

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try{
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                                String name = ds.child("name").getValue(String.class);
//                                names.add(name);
                                    String number = ds.toString();
                                    Log.d("Phone Number", number);
                                    String info = ds.getValue().toString();  // {name=Champzz, latitude=13.9965017, longitude=75.9215433, url=Url goes here}
                                    Log.d("TAG", info);
                                    String name = ds.child("name").getValue().toString();
                                    Double latitude = Double.valueOf(ds.child("latitude").getValue().toString());
                                    Double longitude = Double.valueOf(ds.child("longitude").getValue().toString());
                                    String url = ds.child("url").getValue().toString();

                                    Log.d("Name", name);
                                    Log.d("Latitude", latitude.toString());
                                    Log.d("Longitude", longitude.toString());
                                    Log.d("URL", url);

                                    LatLng location = new LatLng(latitude, longitude);

                                    showUserNearBy(name, location, url);
                                }
                            }
                            catch (Exception e){
                                Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MapsActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });


        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearOtherUsers();
            }
        });
    }

    private void clearOtherUsers() {
        for (int i = 0; i < ObjectList.size(); i++){
            ObjectList.get(i).remove();
        }
        ObjectList.clear();
    }

    private void showUserNearBy(String name, LatLng location, String url) {
// add marker to Map
        Bitmap bitmap = getBitmapFromUrl(url);
        Marker obj;

        if(bitmap != null){
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);
            obj = mMap.addMarker(new MarkerOptions()
                    .position(location)
//                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.logo))
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                    .title(name)
                    .snippet(url)
                    // Specifies the anchor to be at a particular point in the marker image.
                    .anchor(0.5f, 1));
            ObjectList.add(obj);
        }

        
    }

    private Bitmap getBitmapFromUrl(String url) {

        final Bitmap[] bmap = new Bitmap[1];
        bmap[0] = null;
        Picasso.get().load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // loaded bitmap is here (bitmap)
                bmap[0] = bitmap;
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(MapsActivity.this, "Error : " + e.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        });

        return bmap[0];
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                //Toast.makeText(MapsActivity.this, marker.getTitle() + "Using Default Maccha!" + marker.getSnippet(), Toast.LENGTH_SHORT).show();
                ViewDialog alert = new ViewDialog();
                alert.showDialog(getInstance(), marker.getTitle(), marker.getSnippet());
                return false;
            }
        });

        //updateLocation();
    }

    private void updateLocation() {
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(1f);
    }

    public void updateMarker(Double latitude, Double longitude){
        MapsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng currentLocation = new LatLng(latitude, longitude);
                marker.remove();
                if(user == null){
                    marker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current User"));
                }
                else{

                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
                Toast.makeText(MapsActivity.this, "Location: " + latitude + " , " + longitude, Toast.LENGTH_SHORT).show();
                getAddress(latitude, longitude);

                String path = country + "/" + state + "/" + pincode + "/" + StorageClass.phonenumber;
                //Toast.makeText(MapsActivity.this, "Path : " + path, Toast.LENGTH_SHORT).show();
//                if(oldpincode.equals("")){ // First Entry  // Make use of shared preference - What if user went offline or cleared app... old entry should go from FB..
//
//                }

                if(country == null || state == null || pincode == null){
                    Toast.makeText(MapsActivity.this, "We are not supporting this location", Toast.LENGTH_SHORT).show();
                }
                else{
                    NearByUsers object = new NearByUsers(StorageClass.email, StorageClass.profileurl, String.valueOf(latitude), String.valueOf(longitude));
                    current.child(path).setValue(object);
                }

            }
        });
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            country = obj.getCountryName();
            state = obj.getAdminArea();
            pincode = obj.getPostalCode();

            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName(); //
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();  //
            add = add + "\n" + obj.getPostalCode();  //
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}

/*
AddressUnnamed Road, Karnataka 577245, India
AddressNH 69, Karnataka 577245, India
AddressNH 69, Mosarahalli, Karnataka 577245, India
AddressUnnamed Road, MPM- Mysore Paper and Sugar mills, Bhadravathi, Karnataka 577302, India
 */