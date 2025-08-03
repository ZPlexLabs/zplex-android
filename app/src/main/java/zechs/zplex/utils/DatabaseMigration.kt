package zechs.zplex.utils

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE movies ADD COLUMN modifiedTime INTEGER DEFAULT NULL")

        db.execSQL("ALTER TABLE shows ADD COLUMN modifiedTime INTEGER DEFAULT NULL")
    }
}
