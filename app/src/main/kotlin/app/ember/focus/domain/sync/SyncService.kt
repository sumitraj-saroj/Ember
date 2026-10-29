package app.ember.focus.domain.sync

interface SyncService {
    suspend fun sync(): Result<Unit>
    suspend fun isSyncEnabled(): Boolean
}
