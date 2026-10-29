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
                colorHex = "#E07A5F",
                sortOrder = 1
            ),
            CategoryEntity(
                id = "cat_coding",
                name = "Coding",
                colorHex = "#F4A261",
                sortOrder = 2
            ),
            CategoryEntity(
                id = "cat_writing",
                name = "Writing",
                colorHex = "#DDA15E",
                sortOrder = 3
            ),
            CategoryEntity(
                id = "cat_reading",
                name = "Reading",
                colorHex = "#8AB17D",
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
    }
}
