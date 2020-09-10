package com.example.androidmyrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.androidmyrestaurant.Adapter.MyFavoriteAdapter;
import com.example.androidmyrestaurant.Adapter.MyFoodAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Model.Category;
import com.example.androidmyrestaurant.Model.FavoriteModel;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FavoriteActivity extends AppCompatActivity {

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @BindView(R.id.recycler_fav)
    RecyclerView recycler_fav;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    MyFavoriteAdapter adapter;

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        if (adapter != null)
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        init();
        initView();

        loadFavoriteItems();
    }

    private void loadFavoriteItems() {
        dialog.show();

        compositeDisposable.add(myRestaurantAPI.getFavoriteByUser(Common.API_KEY,
                Common.currentUser.getFbid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(favoriteModel -> {

            if (favoriteModel.isSuccess())
            {
                adapter = new MyFavoriteAdapter(FavoriteActivity.this,favoriteModel.getResult());
                recycler_fav.setAdapter(adapter);
            }
            else
            {
                Toast.makeText(this, "[GET FAV RESULT]"+favoriteModel.getMessage(), Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();

        }, throwable -> {
            dialog.dismiss();
            Toast.makeText(this, "[GET FAV]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void initView() {
        ButterKnife.bind(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_fav.setLayoutManager(layoutManager);
        recycler_fav.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

        toolbar.setTitle(getString(R.string.menu_fav));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

    }
}
