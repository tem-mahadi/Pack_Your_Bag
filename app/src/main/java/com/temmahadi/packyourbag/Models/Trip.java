package com.temmahadi.packyourbag.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trips")
public class Trip {

    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "name")
    String name;

    @ColumnInfo(name = "destination")
    String destination;

    @ColumnInfo(name = "triptype")
    String tripType;

    @ColumnInfo(name = "startdate")
    long startDate;

    @ColumnInfo(name = "enddate")
    long endDate;

    @ColumnInfo(name = "createdat")
    long createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
