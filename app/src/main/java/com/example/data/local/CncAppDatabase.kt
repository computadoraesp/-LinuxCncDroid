package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineProfileDao {
    @Query("SELECT * FROM machine_profiles ORDER BY isDefault DESC, lastConnectedTime DESC")
    fun getAllProfiles(): Flow<List<MachineProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: MachineProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: MachineProfileEntity)

    @Query("DELETE FROM machine_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Long)
}

@Dao
interface MdiMacroDao {
    @Query("SELECT * FROM mdi_macros")
    fun getAllMacros(): Flow<List<MdiMacroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacros(macros: List<MdiMacroEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MdiMacroEntity)

    @Query("DELETE FROM mdi_macros WHERE id = :id")
    suspend fun deleteMacro(id: String)
}

@Database(
    entities = [MachineProfileEntity::class, MdiMacroEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CncAppDatabase : RoomDatabase() {
    abstract fun profileDao(): MachineProfileDao
    abstract fun macroDao(): MdiMacroDao
}
