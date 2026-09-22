package com.example.kerberos.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

// Tracks whether a local credential is in sync with the remote backend,
// or is waiting to be pushed as a create or a delete
enum class SyncState {SYNCED, PENDING_CREATE, PENDING_DELETE}

// Room entity representing a single saved credential, stored locally
// in the SQLCipher-encrypted database
@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey val id: String,
    val serviceName: String,
    val username: String,
    val password: String,
    val websiteUrl: String,
    val notes: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val syncState: SyncState
)

// Data access object for credential CRUD + sync-state queries
@Dao
interface CredentialDao{
    //select all from credentials
    @Query("SELECT * FROM credentials WHERE syncState != 'PENDING_DELETE' ORDER BY serviceName ASC")
    fun getAllCredentials(): Flow<List<CredentialEntity>>

    //get pending sync
    @Query("SELECT * FROM credentials WHERE syncState != 'SYNCED'")
    suspend fun getPendingSyncCredentials(): List<CredentialEntity>

    //Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(credential: CredentialEntity)

    //Update sync state
    @Query("UPDATE credentials SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: SyncState)

    //delete
    @Query("DELETE FROM credentials WHERE id = :id")
    suspend fun deletePermanently(id: String)
}

// Room database definition; encrypted at rest via SQLCipher
@Database(entities = [CredentialEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){
    abstract fun credentialDao() :CredentialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, passphrase: ByteArray): AppDatabase {
            return INSTANCE ?: synchronized(this){

                System.loadLibrary("sqlcipher")

                //making use of cipher SQL libs
                val factory = SupportOpenHelperFactory(passphrase)

                //create an instance of the database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kerberos_encrypted.db"
                )
                    .openHelperFactory(factory)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

}

// Room type converters so SyncState enum values can be stored as TEXT columns
class Converters {
    @TypeConverter
    fun toSyncState(value: String) = enumValueOf<SyncState>(value)

    @TypeConverter
    fun fromSyncState(value: SyncState) = value.name
}
//References
//Developers, A., 2025. Save data in a local database using Room. [Online]
//Available at: https://developer.android.com/training/data-storage/room
//Developers, A., 2026. Kotlin flows on Android. [Online]
//Available at: https://developer.android.com/kotlin/flow
//SQLCipher, 2024. SQLCipher for Android Application Integration. [Online]
//Available at: https://www.zetetic.net/sqlcipher/sqlcipher-for-android/