package com.example.androidmyrestaurant;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    private List<AuthUI.IdpConfig> provider;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    private static final int APP_REQUEST_CODE = 1234;
    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    @OnClick(R.id.btn_sign_in)
    void loginUser()
    {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(provider).build(),APP_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else {
                Toast.makeText(this, "Failed to sign in ", Toast.LENGTH_SHORT).show();
            }
                dialog.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        init();
    }

    private void init()
    {
        provider = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        firebaseAuth = FirebaseAuth.getInstance();


        listener = firebaseAuth1 -> {
            dialog.show();
            FirebaseUser user = firebaseAuth1.getCurrentUser();
            if (user != null){ //user already login

                compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY,
                        user.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userModel -> {

                                    //If user already in db
                                    if (userModel.isSuccess())
                                    {
                                        Common.currentUser = userModel.getResult().get(0);
                                        startActivity(new Intent(MainActivity.this,HomeActivity.class));
                                        finish();
                                    }
                                    else// And if not register yet
                                    {
                                        startActivity(new Intent(MainActivity.this,UpdateInfoActivity.class));
                                        finish();
                                    }
                                    dialog.dismiss();

                                },
                                throwable -> {
                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, "[GET USER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                })

                );
            }
            else {

                loginUser();
            }
        };
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

    protected void onStart(){
        super.onStart();
        if (listener != null && firebaseAuth != null){
            firebaseAuth.addAuthStateListener(listener);
        }
    }
    protected void onStop(){
        if (listener != null && firebaseAuth !=null){
            firebaseAuth.addAuthStateListener(listener);
        }
        super.onStop();
    }
}
