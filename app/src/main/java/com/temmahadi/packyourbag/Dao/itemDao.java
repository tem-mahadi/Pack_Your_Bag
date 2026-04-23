package com.temmahadi.packyourbag.Dao;


import static androidx.room.OnConflictStrategy.REPLACE;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.temmahadi.packyourbag.Models.items;

import java.util.List;

@Dao
public interface itemDao {
    @Insert(onConflict = REPLACE)
    void saveItem(items item);

    @Query("select * from items where category=:category and tripid=:tripId order by ID asc")
    List<items> getAll(String category, int tripId);

    @Delete
    void delete(items items);

    @Query("update items set checked=:checked where id=:id")
    void checkUncheck(int id, Boolean checked);

    @Query("select count(*) from items where tripid=:tripId")
    Integer getItemsCount(int tripId);

    @Query("select count(*) from items where checked=1 and tripid=:tripId")
    Integer getPackedCount(int tripId);

    @Query("select count(*) from items where category=:category and tripid=:tripId")
    Integer getCountByCategory(String category, int tripId);

    @Query("select count(*) from items where category=:category and checked=1 and tripid=:tripId")
    Integer getPackedCountByCategory(String category, int tripId);

    @Query("select count(*) from items where category=:category and tripid=:tripId and lower(itemname)=lower(:itemName)")
    Integer getCountByCategoryAndName(String category, String itemName, int tripId);

    @Query("update items set checked=:checked where category=:category and tripid=:tripId")
    Integer updateCheckedByCategory(String category, Boolean checked, int tripId);

    @Query("delete from items where category=:category and checked=1 and addedby=:addedBy and tripid=:tripId")
    Integer deleteCheckedByCategoryAndAddedBy(String category, String addedBy, int tripId);

    @Query("delete from items where addedBy=:addedBy and tripid=:tripId")
    Integer deleteAllSystemItems(String addedBy, int tripId);

    @Query("delete from items where category=:category and tripid=:tripId")
    Integer deleteAllByCategory(String category, int tripId);

    @Query("delete from items where category=:category and addedby=:addedBy and tripid=:tripId")
    Integer deleteAllByCategoryAndAddedBy(String category, String addedBy, int tripId);

    @Query("select * from items where checked=:checked and tripid=:tripId order by ID asc")
    List<items> getAllSelected(Boolean checked, int tripId);

    @Query("delete from items where tripid=:tripId")
    Integer deleteAllByTripId(int tripId);
}
