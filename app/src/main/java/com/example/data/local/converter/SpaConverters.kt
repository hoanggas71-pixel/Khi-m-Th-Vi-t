package com.example.data.local.converter

import androidx.room.TypeConverter
import com.example.QuyenHan
import com.example.SystemRole
import com.example.TrangThaiTaiKhoan
import com.example.LoaiTC
import org.json.JSONArray

/**
 * Room TypeConverters cho các cấu trúc dữ liệu phức tạp: Set<QuyenHan>, List<String>, và Enums.
 */
class SpaConverters {

    @TypeConverter
    fun fromQuyenHanSet(set: Set<QuyenHan>?): String {
        if (set.isNullOrEmpty()) return ""
        return set.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toQuyenHanSet(data: String?): Set<QuyenHan> {
        if (data.isNullOrBlank()) return emptySet()
        return data.split(",")
            .mapNotNull { name ->
                val trimmed = name.trim()
                if (trimmed.isEmpty()) null
                else runCatching { QuyenHan.valueOf(trimmed) }.getOrNull()
            }
            .toSet()
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(data)
            val result = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                result.add(arr.getString(i))
            }
            result
        } catch (_: Exception) {
            data.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
    }

    @TypeConverter
    fun fromSystemRole(role: SystemRole?): String {
        return (role ?: SystemRole.STAFF).name
    }

    @TypeConverter
    fun toSystemRole(data: String?): SystemRole {
        if (data.isNullOrBlank()) return SystemRole.STAFF
        return runCatching { SystemRole.valueOf(data.trim()) }.getOrDefault(SystemRole.STAFF)
    }

    @TypeConverter
    fun fromTrangThaiTaiKhoan(status: TrangThaiTaiKhoan?): String {
        return (status ?: TrangThaiTaiKhoan.ACTIVE).name
    }

    @TypeConverter
    fun toTrangThaiTaiKhoan(data: String?): TrangThaiTaiKhoan {
        if (data.isNullOrBlank()) return TrangThaiTaiKhoan.ACTIVE
        return runCatching { TrangThaiTaiKhoan.valueOf(data.trim()) }.getOrDefault(TrangThaiTaiKhoan.ACTIVE)
    }

    @TypeConverter
    fun fromLoaiTC(loai: LoaiTC?): String {
        return (loai ?: LoaiTC.THU).name
    }

    @TypeConverter
    fun toLoaiTC(data: String?): LoaiTC {
        if (data.isNullOrBlank()) return LoaiTC.THU
        return runCatching { LoaiTC.valueOf(data.trim()) }.getOrDefault(LoaiTC.THU)
    }
}
