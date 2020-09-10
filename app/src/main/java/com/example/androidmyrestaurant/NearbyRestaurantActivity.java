package com.example.androidmyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Model.EventBus.MenuItemEvent;
import com.example.androidmyrestaurant.Model.Restaurant;
import com.example.androidmyrestaurant.Model.RestaurantModel;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NearbyRestaurantActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    AlertDialog dialog;


    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;


    Marker userMarker;

    boolean isFirstLoad = false;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_restaurant);

        init();
        initView();

    }

    private void initView() {
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar.setTitle(getString(R.string.nearby_restaurant));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void init() {
            dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
            myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

            buildLocationRequest();
            buildLocationCallBack();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {

                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();
                addMarkerAndMoveCamera(locationResult.getLastLocation());

                if (!isFirstLoad){
                    isFirstLoad = !isFirstLoad;
                    requestNearbyRestaurant(locationResult.getLastLocation().getAltitude(),
                            locationResult.getLastLocation().getLongitude(),10);
                }
            }
        };
    }


    private void requestNearbyRestaurant (double latitude, double longtitude,int distance){
        dialog.show();

        compositeDisposable.add(myRestaurantAPI.getNearbyRestaurant(Common.API_KEY,latitude,longtitude,distance)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(restaurantModel -> {
            if (restaurantModel.isSuccess()){

                addRestaurantMarker(restaurantModel.getResult());

            }else {
                Toast.makeText(this, ""+restaurantModel.getMessage(), Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();

        }, throwable -> {
            dialog.dismiss();

            Toast.makeText(this, "[NEARBY RESTAURANT]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void addRestaurantMarker(List<Restaurant> restaurantList) {
        for (Restaurant restaurant:restaurantList){
            mMap.addMarker(new MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_marker))
            .position(new LatLng(restaurant.getLat(),restaurant.getLng()))
            .snippet(restaurant.getAddress())
            .title(new StringBuilder()
            .append(restaurant.getId())
            .append(".")
            .append(restaurant.getName()).toString()));
        }
    }

    private void addMarkerAndMoveCamera(Location lastLocation) {
        if (userMarker != null)
            userMarker.remove();


        LatLng userLatLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title(Common.currentUser.getName()));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLatLng,17);
        mMap.animateCamera(yourLocation);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style));
            if (!success)
                Log.e("ERROR_MAP","Load style error");
        }catch (Resources.NotFoundException e){
            Log.e("ERROR_MAP","Resource not found");

        }

        mMap.setOnInfoWindowClickListener(marker -> {
                String id = marker.getTitle().substring(0,marker.getTitle().indexOf("."));
                if (!TextUtils.isEmpty(id)){
                    compositeDisposable.add(myRestaurantAPI.getRestaurantById(Common.API_KEY,id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(restaurantByIdModel -> {
                        if (restaurantByIdModel.isSuccess()){
                            Common.currentRestaurant = restaurantByIdModel.getResult().get(0);
                            EventBus.getDefault().postSticky(new MenuItemEvent(true,Common.currentRestaurant));
                            startActivity(new Intent(NearbyRestaurantActivity.this,MenuActivity.class));
                            finish();
                        }
                        else
                        {
                            Toast.makeText(this, ""+restaurantByIdModel.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }, throwable -> {
                        Toast.makeText(this, "[GET RESTAURANT BY ID]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }));
                }
        });
    }
}
