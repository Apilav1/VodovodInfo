package com.pilove.vodovodinfo.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pilove.vodovodinfo.data.Notice

@Dao
interface NoticeDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notice: Notice)

    @Delete
    suspend fun delete(notice: Notice)

    @Query("Select * from notices order by id DESC LIMIT 10")
    fun getLastTenNotices(): LiveData<List<Notice>>

    @Query("Select * from notices WHERE id >= :id LIMIT 10")
    fun getTenNoticesBeforeId(id: Int): LiveData<List<Notice>>


}