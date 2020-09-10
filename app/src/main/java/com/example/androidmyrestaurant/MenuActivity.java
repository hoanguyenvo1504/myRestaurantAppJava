package com.example.androidmyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.androidmyrestaurant.Adapter.MyCategoryAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Database.CartDataSource;
import com.example.androidmyrestaurant.Database.CartDatabase;
import com.example.androidmyrestaurant.Database.LocalCartDataSource;
import com.example.androidmyrestaurant.Model.EventBus.MenuItemEvent;
import com.example.androidmyrestaurant.Model.FavoriteOnlyIdModel;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.example.androidmyrestaurant.Utils.SpacesItemDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nex3z.notificationbadge.NotificationBadge;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MenuActivity extends AppCompatActivity {

    @BindView(R.id.img_restaurant)
    ImageView img_restaurant;
    @BindView(R.id.recycler_category)
    RecyclerView recycler_category;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton btn_cart;
    @BindView(R.id.badge)
    NotificationBadge badge;


    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    MyCategoryAdapter adapter;
    CartDataSource cartDataSource;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartByRestaurant();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        init();
        initView();
        countCartByRestaurant();
        loadFavoriteByRestaurant();
    }

    private void loadFavoriteByRestaurant() {
        compositeDisposable.add(myRestaurantAPI.getFavoriteByRestaurant(Common.API_KEY,
               Common.currentUser.getFbid(),
               Common.currentRestaurant.getId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
       .subscribe(favoriteOnlyIdModel -> {
                    if (favoriteOnlyIdModel.isSuccess()) {
                        if (favoriteOnlyIdModel.getResult() != null && favoriteOnlyIdModel.getResult().size() > 0) {
                            Common.currentFavOfRestaurant = favoriteOnlyIdModel.getResult();
                        } else {
                           Common.currentFavOfRestaurant = new ArrayList<>();
                        }
                    }
                    else {
                        Toast.makeText(this, "[GET FAVORITE]"+favoriteOnlyIdModel.getMessage(), Toast.LENGTH_SHORT).show();
                    }
        }, throwable -> {
            Toast.makeText(this, "[GET FAVORITE]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
       }));
    }

    private void countCartByRestaurant() {
        cartDataSource.countItemInCart(Common.currentUser.getFbid(),
                Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        badge.setText(String.valueOf(integer));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MenuActivity.this, "[COUNT CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initView() {
        ButterKnife.bind(this);

        btn_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this,CartListActivity.class));
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        //This code will select item view type
        //If item is last, it will set full width on Grid layout
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter != null)
                {
                    switch (adapter.getItemViewType(position))
                    {
                        case Common.DEFAULT_COLUMN_COUNT: return 1;
                        case Common.FULL_WIDTH_COLUMN: return 2;
                        default : return -1;
                    }
                }
                else
                    return -1;
            }
        });
        recycler_category.setLayoutManager(layoutManager);
        recycler_category.addItemDecoration(new SpacesItemDecoration(8));

    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Handle action bar item clicks here. The action bar will
        //automatically handle clicks on the Home/Up button , so long
        // as your specify a parent  activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStament
        if (id == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Event Bus
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void loadMenuByRestaurant(MenuItemEvent event) {
        if (event.isSuccess())
        {
            Picasso.get().load(event.getRestaurant().getImage()).into(img_restaurant);
            toolbar.setTitle(event.getRestaurant().getName());

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            //Request Category by restaurant Id
            compositeDisposable.add(
                    myRestaurantAPI.getCategories(Common.API_KEY,event.getRestaurant().getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(menuModel -> {
                                adapter = new MyCategoryAdapter(MenuActivity.this,menuModel.getResult());
                                recycler_category.setAdapter(adapter);
                                    },
                                    throwable -> {
                                        Toast.makeText(this, "[GET CATEGORY]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
            );
        }
    }
}
