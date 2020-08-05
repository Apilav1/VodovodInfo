package com.pilove.vodovodinfo.room

import androidx.room.*

@Dao
interface NoticeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notice: Notice)

    @Delete
    suspend fun delete(notice: Notice)

    @Query("Select * from notices LIMIT 10")
    suspend fun get10Notices(): ArrayList<Notice>

    @Query("Select * from notices WHERE id >= :id LIMIT 10")
    suspend fun getPrevious10Notices(id: Int)
}