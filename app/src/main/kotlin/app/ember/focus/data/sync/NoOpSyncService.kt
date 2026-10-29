package app.ember.focus.data.sync

import app.ember.focus.domain.sync.SyncService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpSyncService @Inject constructor() : SyncService {
    override suspend fun sync(): Result<Unit> = Result.success(Unit)
    override suspend fun isSyncEnabled(): Boolean = false
}
