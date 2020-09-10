package com.example.androidmyrestaurant.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Model.Order;
import com.example.androidmyrestaurant.R;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder> {

    Context context;
    List<Order> orderList;
    SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_cart,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_num_of_item.setText(new StringBuilder("Num Of Item:  ").append(orderList.get(position).getNumOfItem()));
        holder.txt_order_address.setText(new StringBuilder(orderList.get(position).getOrderAddress()));
        holder.txt_order_date.setText(new StringBuilder(simpleDateFormat.format(orderList.get(position).getOrderDate())));

        holder.txt_order_number.setText(new StringBuilder("Order Number : #").append(orderList.get(position).getOrderId()));
        holder.txt_order_phone.setText(new StringBuilder(orderList.get(position).getOrderPhone()));

        holder.txt_order_total_price.setText(new StringBuilder(context.getString(R.string.money_sign)).append(orderList.get(position).getTotalPrice()));
        holder.txt_order_status.setText(Common.convertStatusToString(orderList.get(position).getOrderStatus()));


        if (orderList.get(position).isCod())
            holder.txt_payment_method.setText(new StringBuilder("Cash On Delivery"));
        else
            holder.txt_payment_method.setText(new StringBuilder("TransID :").append(orderList.get(position).getTransactionId()));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder   {
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_order_phone)
        TextView txt_order_phone;
        @BindView(R.id.txt_order_address)
        TextView txt_order_address;
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @BindView(R.id.txt_order_total_price)
        TextView txt_order_total_price;
        @BindView(R.id.txt_num_of_item)
        TextView txt_num_of_item;
        @BindView(R.id.txt_payment_method)
        TextView txt_payment_method;

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
