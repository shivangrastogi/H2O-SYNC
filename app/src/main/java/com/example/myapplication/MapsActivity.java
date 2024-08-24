package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    private final static int REQUEST_CODE = 100;
    FirebaseAuth auth;
    Button button;
    Button enter_info;

    FirebaseUser user;
    Spinner mapTypeSpinner;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.btn_logout);
        enter_info = findViewById(R.id.btn_enter_problem);
        user = auth.getCurrentUser();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (user == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), login.class);
            startActivity(intent);
            finish();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });

        enter_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        Intent intent = new Intent(getApplicationContext(), info.class);
                        intent.putExtra("LATITUDE", latitude);
                        intent.putExtra("LONGITUDE", longitude);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Check and request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            // Permission already granted, proceed with map initialization
            initializeMap();
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);

        mapTypeSpinner = findViewById(R.id.mapTypeSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.map_types,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapTypeSpinner.setAdapter(adapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // Change map type based on the selected item
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0:
                        changeMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        changeMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 2:
                        changeMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 3:
                        changeMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeMap() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Check if the map is ready before adding a marker
                        if (map != null) {
                            MarkerOptions markerOptions = new MarkerOptions().position(currentLocation).title("Current Location");
                            map.addMarker(markerOptions);
                            map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f));
                        }
                    }
                });
    }

    private void changeMapType(int mapType) {
        if (map != null) {
            map.setMapType(mapType);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions().position(currentLocation).title("Current Location");
                        map.addMarker(markerOptions);
                        map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f));
                    } else {
                        LatLng ims = new LatLng(28.682303735555678, 77.50823973755483);
                        MarkerOptions markerOptions = new MarkerOptions().position(ims).title("IMS");
                        map.addMarker(markerOptions);
                        map.moveCamera(CameraUpdateFactory.newLatLng(ims));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(ims, 16f));
                    }
                });
    }
}
