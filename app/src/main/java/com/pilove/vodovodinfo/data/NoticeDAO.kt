package com.pilove.vodovodinfo.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.pilove.vodovodinfo.data.Notice

@Dao
interface NoticeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notice: Notice)

    @Delete
    suspend fun delete(notice: Notice)

//   @Query("Select * FROM Notices")
    @Query("Select * FROM Notices ORDER BY id DESC LIMIT 10")
    fun getLastTenNotices(): LiveData<List<Notice>>

    @Query("SELECT * FROM Notices LIMIT 2")
    fun getLast(): LiveData<List<Notice>>


    @Query("Select * FROM Notices WHERE id >= :id LIMIT 10")
    fun getTenNoticesBeforeId(id: Int): LiveData<List<Notice>>


}