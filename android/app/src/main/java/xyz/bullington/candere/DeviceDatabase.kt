package xyz.bullington.candere

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [(Device::class)], version = 1)
abstract class DeviceDatabase: RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
}