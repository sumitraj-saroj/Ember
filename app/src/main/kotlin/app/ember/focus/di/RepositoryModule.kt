package app.ember.focus.di

import app.ember.focus.data.repository.CategoryRepositoryImpl
import app.ember.focus.data.repository.SessionRepositoryImpl
import app.ember.focus.data.repository.SettingsRepositoryImpl
import app.ember.focus.data.repository.TaskRepositoryImpl
import app.ember.focus.data.sync.NoOpSyncService
import app.ember.focus.domain.repository.CategoryRepository
import app.ember.focus.domain.repository.SessionRepository
import app.ember.focus.domain.repository.SettingsRepository
import app.ember.focus.domain.repository.TaskRepository
import app.ember.focus.domain.sync.SyncService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindSyncService(impl: NoOpSyncService): SyncService
}
