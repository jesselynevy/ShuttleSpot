package com.beg.shuttlespot.di

import android.content.Context
import androidx.room.Room
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import com.beg.shuttlespot.data.local.ShuttleSpotDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShuttleSpotDatabase =
        Room.databaseBuilder(context, ShuttleSpotDatabase::class.java, "shuttlespot.db").build()

    @Provides fun provideDao(database: ShuttleSpotDatabase): ShuttleSpotDao = database.shuttleSpotDao()
}
