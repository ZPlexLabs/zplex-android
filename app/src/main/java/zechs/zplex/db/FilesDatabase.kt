package zechs.zplex.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import zechs.zplex.models.drive.File


@Database(
    entities = [File::class],
    version = 1,
    exportSchema = false
)

abstract class FilesDatabase : RoomDatabase() {

    abstract fun getFilesDao(): FilesDao

    companion object {
        @Volatile
        private var instance: FilesDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                FilesDatabase::class.java,
                "files_db.db"
            ).fallbackToDestructiveMigration().build()
    }
}