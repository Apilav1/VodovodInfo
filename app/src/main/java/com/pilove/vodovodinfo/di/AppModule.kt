package com.pilove.vodovodinfo.di

import android.content.Context
import androidx.room.Room
import com.pilove.vodovodinfo.other.Constants.NOTICE_DATABASE_NAME
import com.pilove.vodovodinfo.room.NoticeDAO
import com.pilove.vodovodinfo.room.NoticeDatabase
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

}