package com.temmahadi.packyourbag.Dao;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.temmahadi.packyourbag.Models.Trip;

import java.util.List;

@Dao
public interface tripDao {

    @Insert(onConflict = REPLACE)
    long insertTrip(Trip trip);

    @Update
    void updateTrip(Trip trip);

    @Delete
    void deleteTrip(Trip trip);

    @Query("select * from trips order by createdat desc")
    List<Trip> getAllTrips();

    @Query("select * from trips where id=:tripId limit 1")
    Trip getTripById(int tripId);

    @Query("select count(*) from trips")
    Integer countTrips();
}
