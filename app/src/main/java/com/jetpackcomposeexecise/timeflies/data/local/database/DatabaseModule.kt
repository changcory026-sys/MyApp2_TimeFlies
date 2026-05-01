package com.jetpackcomposeexecise.timeflies.data.local.database

import android.content.Context
import androidx.room.Room
import com.jetpackcomposeexecise.timeflies.data.local.dao.TimeFliesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TimeFliesDatabase {
        return Room.databaseBuilder(
            context,
            TimeFliesDatabase::class.java,
            "time_flies_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTimeFliesDao(database: TimeFliesDatabase): TimeFliesDao {
        return database.timeFliesDao()
    }
}
