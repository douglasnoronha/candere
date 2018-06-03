package xyz.bullington.candere

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface DeviceDao {
    @Query("SELECT * from devices")
    fun fetch(): List<Device>

    @Insert(onConflict = REPLACE)
    fun add(device: Device)

    @Delete
    fun remove(device: Device)

    @Update
    fun update(device: Device)
}