package com.app.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView listView;
    private ArrayList<String> nearByDriveRequests;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengerLatitude;
    private ArrayList<Double> passengerLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        listView = findViewById(R.id.requestListView);
        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);
        nearByDriveRequests = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByDriveRequests);

        listView.setAdapter(adapter);
        nearByDriveRequests.clear();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationListener= new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                }
            };

        }
        listView.setOnItemClickListener(this);

        passengerLatitude= new ArrayList<>();
        passengerLongitude = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.driverLogOutItem) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null) {
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (Build.VERSION.SDK_INT < 23) {

          //  locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0, 0, locationListener);
          Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
          updateRequestsListView(currentDriverLocation);

        } else if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2000);

            } else {
               // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0, 0, locationListener);
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                updateRequestsListView(currentDriverLocation);
            }
        }
    }

    private void updateRequestsListView(Location pLocation) {
        if (pLocation != null) {

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(pLocation.getLatitude(), pLocation.getLongitude());
            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            if (nearByDriveRequests.size() > 0) {
                                nearByDriveRequests.clear();
                            }
                            if (passengerLongitude.size() > 0) {
                                passengerLongitude.clear();
                            }
                            if (passengerLatitude.size() > 0) {
                                passengerLatitude.clear();
                            }
                            for (ParseObject nearRequest : objects) {
                                ParseGeoPoint pLocation = nearRequest.getParseGeoPoint("passengerLocation");
                                Double kmDistanceToPassenger = driverCurrentLocation.distanceInKilometersTo(pLocation);
                                float roundDistanceValue = Math.round(kmDistanceToPassenger * 10) / 10;
                                nearByDriveRequests.add("There are " + roundDistanceValue + " kms to " + nearRequest.get("username"));
                                passengerLatitude.add(pLocation.getLatitude());
                                passengerLongitude.add(pLocation.getLongitude());
                            }
                        } else {
                            Toast.makeText(DriverRequestListActivity.this, "Sorry no requests yet.", Toast.LENGTH_LONG).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode == 2000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                updateRequestsListView(currentDriverLocation);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }
}