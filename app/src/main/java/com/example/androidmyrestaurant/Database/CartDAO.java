package com.example.androidmyrestaurant.Database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {
        // We Only load Cart by Restaurant Id

    @Query("SELECT * FROM Cart WHERE fbid =:fbid AND restaurantId=:restaurantId")
    Flowable<List<CartItem>> getAllCart(String fbid, int restaurantId);

    @Query("SELECT COUNT(*) from Cart where fbid =:fbid AND restaurantId=:restaurantId")
    Single<Integer> countItemInCart(String fbid, int restaurantId);

    @Query("SELECT SUM(foodPrice*foodQuantity)+(foodExtraPrice*foodQuantity) from Cart where fbid =:fbid AND restaurantId=:restaurantId")
    Single<Long> sumPrice(String fbid, int restaurantId);

    @Query("SELECT * from Cart where foodId =:foodId AND fbid =:fbid AND restaurantId=:restaurantId")
    Single<CartItem> getItemInCart(String foodId, String fbid, int restaurantId);

    @Insert(onConflict = OnConflictStrategy.REPLACE) // If conflic foodId, we will update information
    Completable insertOrReplaceAll(CartItem ... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCart(CartItem cart);

    @Delete
    Single<Integer> deleteCart(CartItem cart);

    @Query("DELETE FROM Cart WHERE fbid=:fbid AND restaurantId=:restaurantId")
    Single<Integer> cleanCart(String fbid, int restaurantId);
}

