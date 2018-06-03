package xyz.bullington.candere

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "devices")
class Device(
        @PrimaryKey private val id: Int,
        @ColumnInfo() private val nickname: String,
        @ColumnInfo() private val address: String,
        @ColumnInfo() private val username: String,
        @ColumnInfo() private val password: String,
        @ColumnInfo() private val manufacturer: String,
        @ColumnInfo() private val snapshotUrl: String,
        @ColumnInfo() private val rtspUrl: String) {

    fun getId(): Int {
        return id
    }

    fun getNickname(): String {
        return nickname
    }

    fun getAddress(): String {
        return address
    }

    fun getUsername(): String {
        return username
    }

    fun getPassword(): String {
        return password
    }

    fun getManufacturer(): String {
        return manufacturer
    }

    fun getSnapshotUrl(): String {
        return snapshotUrl
    }

    fun getRtspUrl(): String {
        return rtspUrl
    }
}