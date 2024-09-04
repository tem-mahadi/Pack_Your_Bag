package com.temmahadi.packyourbag.Dao;


import static androidx.room.OnConflictStrategy.REPLACE;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.temmahadi.packyourbag.Models.items;

import java.util.List;

@Dao
public interface itemDao {
    @Insert(onConflict = REPLACE)
    void saveItem(items item);
    @Query("select * from items where category=:category order by ID asc")
    List<items> getAll(String category);
    @Delete
    void delete(items items);
    @Query("update items set checked=:checked where id=:id")
    void checkUncheck(int id, Boolean checked);
    @Query("select count(*) from items")
    Integer getItemsCount();
    @Query("delete from items where addedBy=:addedBy")
    Integer deleteAllSystemItems(String addedBy);
    @Query("delete from items where category=:category")
    Integer deleteAllByCategory(String category);
    @Query("delete from items where category=:category and addedby=:addedBy")
    Integer deleteAllByCategoryAndAddedBy(String category, String addedBy);
    @Query("select * from items where checked=:checked order by ID asc")
    List<items> getAllSelected(Boolean checked);
}
