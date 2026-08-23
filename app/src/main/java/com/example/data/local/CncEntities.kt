package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machine_profiles")
data class MachineProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hostIp: String,
    val port: Int = 8000,
    val architecture: String = "ETHERCAT_DELTA",
    val coordinateSystem: String = "G54",
    val isDefault: Boolean = false,
    val lastConnectedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "mdi_macros")
data class MdiMacroEntity(
    @PrimaryKey
    val id: String,
    val label: String,
    val command: String,
    val description: String,
    val category: String = "SETUP"
)
