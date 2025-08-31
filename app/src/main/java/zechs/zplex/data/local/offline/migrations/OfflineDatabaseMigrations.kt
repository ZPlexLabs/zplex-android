package zechs.zplex.data.local.offline.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS movies (
                id INTEGER NOT NULL PRIMARY KEY,
                json TEXT NOT NULL,
                filePath TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}
