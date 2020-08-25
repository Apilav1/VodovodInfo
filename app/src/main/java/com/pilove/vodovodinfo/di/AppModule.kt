package com.pilove.vodovodinfo.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.pilove.vodovodinfo.other.Constants.NOTICE_DATABASE_NAME
import com.pilove.vodovodinfo.data.NoticeDatabase
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_LAT
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_LNG
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_STREET_NAME
import com.pilove.vodovodinfo.other.Constants.KEY_IS_FIRST_TIME
import com.pilove.vodovodinfo.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        NoticeDatabase::class.java,
        NOTICE_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideNoticeDao(db: NoticeDatabase) = db.getNoticeDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideIsFirstTime(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true)

    @Singleton
    @Provides
    fun provideDefaultLocationName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_DEFAULT_LOCATION_STREET_NAME, "")

    @Singleton
    @Provides
    fun provideDefaultLocationLat(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_DEFAULT_LOCATION_LAT, "")

    @Singleton
    @Provides
    fun provideDefaultLocationLng(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_DEFAULT_LOCATION_LNG, "")


}