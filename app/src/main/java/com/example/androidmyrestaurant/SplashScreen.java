package com.example.androidmyrestaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SplashScreen extends AppCompatActivity {

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable  compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {


                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null){
                            compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY,user.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(userModel -> {

                                                if (userModel.isSuccess()) //if user available in db
                                                {
                                                    Common.currentUser = userModel.getResult().get(0);
                                                    Intent intent = new Intent(SplashScreen.this,HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else// if user not available in db, star in MainActivity to create account
                                                {
                                                    Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                dialog.dismiss();
                                            },
                                            throwable -> {
                                                dialog.dismiss();
                                                Toast.makeText(SplashScreen.this, "[GET USER API] "+throwable  .getMessage(), Toast.LENGTH_SHORT).show();
                                            }));
                        }
                        else
                        {
                            Toast.makeText(SplashScreen.this, "Not Sign in! Please sign in ", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SplashScreen.this,MainActivity.class));
                            finish();
                        }

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(SplashScreen.this, "You must accept this permission to use our app", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();



    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

//    private void printKeyHash() {
//        try{
//            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
//                    PackageManager.GET_SIGNATURES);
//            for(Signature signature:info.signatures)
//            {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KEY HASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
}
