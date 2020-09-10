package com.example.androidmyrestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.androidmyrestaurant.Adapter.MyOrderAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Model.OrderModel;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ViewOrderActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_order)
    RecyclerView recycler_view_order;

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        init();
        initView();
        getAllOrder();
    }

    private void getAllOrder() {
        dialog.show();

        compositeDisposable.add(myRestaurantAPI.getOrder(Common.API_KEY,
                Common.currentUser.getFbid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(orderModel -> {

            if (orderModel.isSuccess())
            {
                if (orderModel.getResult().size() > 0)
                {
                    //Create Adapter
                    MyOrderAdapter adapter = new MyOrderAdapter(this,orderModel.getResult());
                    recycler_view_order.setAdapter(adapter);
                }

            }
            dialog.dismiss();


        }, throwable ->
        {
            dialog.dismiss();
            Toast.makeText(ViewOrderActivity.this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }


    private void initView() {
        ButterKnife.bind(this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_view_order.setLayoutManager(layoutManager);
        recycler_view_order.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

        toolbar.setTitle(getString(R.string.your_order));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
