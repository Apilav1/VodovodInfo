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

    @Query("SELECT n.* FROM Notices n," +
            "(SELECT * FROM Notices ORDER by id DESC LIMIT 1) d " +
            "WHERE d.dateForComparison = n.dateForComparison")
    fun getNotices(): LiveData<List<Notice>>


}