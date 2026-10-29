package app.ember.focus.di

import android.content.Context
import androidx.room.Room
import app.ember.focus.core.time.Clock
import app.ember.focus.core.time.SystemClockImpl
import app.ember.focus.data.local.EmberDatabase
import app.ember.focus.data.local.dao.CategoryDao
import app.ember.focus.data.local.dao.SessionDao
import app.ember.focus.data.local.dao.TaskDao
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
    fun provideEmberDatabase(@ApplicationContext context: Context): EmberDatabase {
        return Room.databaseBuilder(
            context,
            EmberDatabase::class.java,
            "ember.db"
        )
            .addCallback(EmberDatabase.SeedCallback())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideSessionDao(db: EmberDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideTaskDao(db: EmberDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideCategoryDao(db: EmberDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideClock(systemClockImpl: SystemClockImpl): Clock = systemClockImpl
}
