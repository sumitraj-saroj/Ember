package app.ember.focus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.ember.focus.data.local.dao.CategoryDao
import app.ember.focus.data.local.dao.SessionDao
import app.ember.focus.data.local.dao.TaskDao
import app.ember.focus.data.local.entity.CategoryEntity
import app.ember.focus.data.local.entity.SessionEntity
import app.ember.focus.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        SessionEntity::class,
        TaskEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class EmberDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(
                id = "cat_work",
                name = "Work",
                colorHex = "#E8A15A",
                sortOrder = 0
            ),
            CategoryEntity(
                id = "cat_study",
                name = "Study",
                colorHex = "#E07A6A",
                sortOrder = 1
            ),
            CategoryEntity(
                id = "cat_coding",
                name = "Coding",
                colorHex = "#78A9C7",
                sortOrder = 2
            ),
            CategoryEntity(
                id = "cat_writing",
                name = "Writing",
                colorHex = "#B38FD6",
                sortOrder = 3
            ),
            CategoryEntity(
                id = "cat_reading",
                name = "Reading",
                colorHex = "#8DBE8A",
                sortOrder = 4
            )
        )
    }

    class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            DEFAULT_CATEGORIES.forEach { category ->
                db.execSQL(
                    """
                    INSERT INTO categories (id, name, colorHex, sortOrder, archived, createdAt, updatedAt, isDeleted)
                    VALUES ('${category.id}', '${category.name}', '${category.colorHex}', ${category.sortOrder}, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 0)
                    """.trimIndent()
                )
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // G4: One-time migration for default categories
            // Updates default colors ONLY if they still equal their old seed values
            db.execSQL("UPDATE categories SET colorHex = '#E07A6A' WHERE id = 'cat_study' AND colorHex = '#E07A5F'")
            db.execSQL("UPDATE categories SET colorHex = '#78A9C7' WHERE id = 'cat_coding' AND colorHex = '#F4A261'")
            db.execSQL("UPDATE categories SET colorHex = '#B38FD6' WHERE id = 'cat_writing' AND colorHex = '#DDA15E'")
            db.execSQL("UPDATE categories SET colorHex = '#8DBE8A' WHERE id = 'cat_reading' AND colorHex = '#8AB17D'")
        }
    }
}
