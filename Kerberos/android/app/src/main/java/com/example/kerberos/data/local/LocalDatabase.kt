package com.example.kerberos.data.local

import android.content.Context
import androidx.room.*
import com.example.kerberos.data.Credential
import kotlinx.coroutines.flow.Flow
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory


enum class SyncState {SYNCED, PENDING_CREATE, PENDING_DELETE}

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

@Database(entities = [CredentialEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){
    abstract fun credentialDao() :CredentialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, passphrase: ByteArray): AppDatabase {
            return INSTANCE ?: synchronized(this){
                //making use of cipher sql libs
                System.loadLibrary("sqlcipher")
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

class Converters {
    @TypeConverter
    fun toSyncState(value: String) = enumValueOf<SyncState>(value)

    @TypeConverter
    fun fromSyncState(value: SyncState) = value.name
}