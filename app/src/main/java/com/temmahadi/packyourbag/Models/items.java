package com.temmahadi.packyourbag.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
@Entity(tableName= "items")
public class items implements Serializable{
    @PrimaryKey(autoGenerate = true)
    int ID=0;
    @ColumnInfo(name= "itemname")
    String itemName;
    @ColumnInfo(name = "category")
    String category;
    @ColumnInfo(name = "addedby")
    String addedBy;
    @ColumnInfo(name = "checked")
    Boolean checked=false;
    public items(){}

    public items(String itemName, String category, String addedBy, Boolean checked) {
        this.itemName = itemName;
        this.category = category;
        this.addedBy = addedBy;
        this.checked = checked;
    }

    public items(String itemName, String category, Boolean checked) {
        this.addedBy = "system";
        this.itemName = itemName;
        this.category = category;
        this.checked = checked;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}
