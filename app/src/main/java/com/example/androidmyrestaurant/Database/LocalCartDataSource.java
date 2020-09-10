package com.example.androidmyrestaurant.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalCartDataSource implements CartDataSource {

    private CartDAO cartDAO;

    public LocalCartDataSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Flowable<List<CartItem>> getAllCart(String fbid, int restaurantId) {
        return cartDAO.getAllCart(fbid,restaurantId);
    }

    @Override
    public Single<Integer> countItemInCart(String fbid, int restaurantId) {
        return cartDAO.countItemInCart(fbid,restaurantId);
    }

    @Override
    public Single<Long> sumPrice(String fbid, int restaurantId) {
        return cartDAO.sumPrice(fbid,restaurantId);
    }

    @Override
    public Single<CartItem> getItemInCart(String foodId, String fbid, int restaurantId) {
        return cartDAO.getItemInCart(foodId,fbid,restaurantId);
    }

    @Override
    public Completable insertOrReplaceAll(CartItem... cartItems) {
        return cartDAO.insertOrReplaceAll(cartItems);
    }

    @Override
    public Single<Integer> updateCart(CartItem cart) {
        return cartDAO.updateCart(cart);
    }

    @Override
    public Single<Integer> deleteCart(CartItem cart) {
        return cartDAO.deleteCart(cart);
    }

    @Override
    public Single<Integer> cleanCart(String fbid, int restaurantId) {
        return cartDAO.cleanCart(fbid,restaurantId);
    }
}
