package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.DEFAULT_TENANT_ID
import com.example.DK
import com.example.DV
import com.example.Goi
import com.example.KH
import com.example.LoaiTC
import com.example.Luot
import com.example.NV
import com.example.QuyenHan
import com.example.SystemRole
import com.example.TC
import com.example.TaiKhoan
import com.example.TenantInfo

// =============================================================================
// ROOM ENTITIES CỐT LÕI HỖ TRỢ MULTI-TENANT & OFFLINE-FIRST
// =============================================================================

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["tenantId", "username"]),
        Index(value = ["tenantId", "role"])
    ]
)
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val tenantId: String = DEFAULT_TENANT_ID,
    val username: String = "",
    val matKhauHash: String = "",
    val role: SystemRole = SystemRole.STAFF,
    val nvId: String = "",
    val hoTen: String = "",
    val cacQuyenDuocCap: Set<QuyenHan> = emptySet(),
    val matKhauHienThi: String = "",
    val email: String = "",
    val deviceTokenFcm: String = "",
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "staff",
    indices = [
        Index(value = ["tenantId", "sdt"]),
        Index(value = ["tenantId", "ten"])
    ]
)
data class StaffEntity(
    @PrimaryKey
    val id: String,
    val tenantId: String = DEFAULT_TENANT_ID,
    val ten: String,
    val gt: String,
    val sdt: String,
    val gc: String = "",
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "services",
    indices = [
        Index(value = ["tenantId", "ten"])
    ]
)
data class ServiceEntity(
    @PrimaryKey
    val id: String,
    val tenantId: String = DEFAULT_TENANT_ID,
    val ten: String,
    val phut: Int,
    val gia: Long,
    val moTa: String = "",
    val chietKhauVnd: Long,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "packages",
    indices = [
        Index(value = ["tenantId", "ten"])
    ]
)
data class PackageEntity(
    @PrimaryKey
    val id: String,
    val tenantId: String = DEFAULT_TENANT_ID,
    val ten: String,
    val dvIds: List<String> = emptyList(),
    val soBuoi: Int,
    val gia: Long,
    val chiTietLuot: String = "",
    val dvTangKem: String = "",
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["tenantId", "sdt"]),
        Index(value = ["tenantId", "ten"])
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val tenantId: String = DEFAULT_TENANT_ID,
    val ten: String,
    val sdt: String,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "package_subscriptions",
    indices = [
        Index(value = ["tenantId", "khId"]),
        Index(value = ["tenantId", "daDung"])
    ]
)
data class PackageSubEntity(
    @PrimaryKey
    val id: String,
    val tenantId: String = DEFAULT_TENANT_ID,
    val khId: String,
    val goiId: String,
    val ngay: String,
    val daDung: Boolean = false,
    val luotDaDung: Int = 0,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tickets",
    indices = [
        Index(value = ["tenantId", "ngay"]),
        Index(value = ["tenantId", "daXacNhan"]),
        Index(value = ["tenantId", "nvId"])
    ]
)
data class TicketEntity(
    @PrimaryKey
    val id: String,
    val tenantId: String = DEFAULT_TENANT_ID,
    val khId: String,
    val nvId: String,
    val dvIds: List<String> = emptyList(),
    val dkId: String = "",
    val tgbd: String,
    val tgkt: String = "",
    val thu: Long = 0L,
    val daXacNhan: Boolean = false,
    val ngay: String,
    val dvNgoai: String = "",
    val hoaHongChot: Long? = null,
    val phieuChiHoaHongId: String = "",
    val phieuThuVeId: String = "" ,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cashbook",
    indices = [
        Index(value = ["tenantId", "ngay"]),
        Index(value = ["tenantId", "loai"])
    ]
)
data class CashbookEntity(
    @PrimaryKey
    val id: String,
    val tenantId: String = DEFAULT_TENANT_ID,
    val loai: LoaiTC,
    val tien: Long,
    val lyDo: String,
    val ngay: String,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tenants"
)
data class TenantEntity(
    @PrimaryKey
    val tenantId: String = DEFAULT_TENANT_ID,
    val shopName: String = "",
    val status: String = "ACTIVE",
    val isLocked: Boolean = false,
    val lockedReason: String = "",
    val subscriptionExpiresAt: Long = 0L,
    val planId: String = "PLAN_FREE",
    val ownerPhone: String = "",
    val ownerEmail: String = "",
    val qrPaymentUrl: String = "",
    val accountsCount: Int = 0,
    val ticketsCount: Int = 0,
    val customersCount: Int = 0,
    val createdAt: String = "",
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

// =============================================================================
// DOMAIN <-> ENTITY MAPPERS 2 CHIỀU
// =============================================================================

fun TaiKhoan.toEntity(tenantIdOverride: String? = null): AccountEntity {
    return AccountEntity(
        id = this.id,
        tenantId = tenantIdOverride ?: this.tenantId.ifBlank { DEFAULT_TENANT_ID },
        username = this.username,
        matKhauHash = this.matKhauHash,
        role = this.role,
        nvId = this.nvId,
        hoTen = this.hoTen,
        cacQuyenDuocCap = this.cacQuyenDuocCap,
        matKhauHienThi = this.matKhauHienThi,
        email = this.email,
        deviceTokenFcm = this.deviceTokenFcm,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun AccountEntity.toDomain(): TaiKhoan {
    return TaiKhoan(
        id = this.id,
        username = this.username,
        matKhauHash = this.matKhauHash,
        nvId = this.nvId,
        hoTen = this.hoTen,
        cacQuyenDuocCap = this.cacQuyenDuocCap,
        matKhauHienThi = this.matKhauHienThi,
        email = this.email,
        tenantId = this.tenantId,
        role = this.role,
        deviceTokenFcm = this.deviceTokenFcm
    )
}

fun NV.toEntity(tenantId: String = DEFAULT_TENANT_ID): StaffEntity {
    return StaffEntity(
        id = this.id,
        tenantId = tenantId,
        ten = this.ten,
        gt = this.gt,
        sdt = this.sdt,
        gc = this.gc,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun StaffEntity.toDomain(): NV {
    return NV(
        id = this.id,
        ten = this.ten,
        gt = this.gt,
        sdt = this.sdt,
        gc = this.gc
    )
}

fun DV.toEntity(tenantId: String = DEFAULT_TENANT_ID): ServiceEntity {
    return ServiceEntity(
        id = this.id,
        tenantId = tenantId,
        ten = this.ten,
        phut = this.phut,
        gia = this.gia,
        moTa = this.moTa,
        chietKhauVnd = this.chietKhauVnd,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun ServiceEntity.toDomain(): DV {
    return DV(
        id = this.id,
        ten = this.ten,
        phut = this.phut,
        gia = this.gia,
        moTa = this.moTa,
        chietKhauVnd = this.chietKhauVnd
    )
}

fun Goi.toEntity(tenantId: String = DEFAULT_TENANT_ID): PackageEntity {
    return PackageEntity(
        id = this.id,
        tenantId = tenantId,
        ten = this.ten,
        dvIds = this.dvIds,
        soBuoi = this.soBuoi,
        gia = this.gia,
        chiTietLuot = this.chiTietLuot,
        dvTangKem = this.dvTangKem,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun PackageEntity.toDomain(): Goi {
    return Goi(
        id = this.id,
        ten = this.ten,
        dvIds = this.dvIds,
        soBuoi = this.soBuoi,
        gia = this.gia,
        chiTietLuot = this.chiTietLuot,
        dvTangKem = this.dvTangKem
    )
}

fun KH.toEntity(tenantId: String = DEFAULT_TENANT_ID): CustomerEntity {
    return CustomerEntity(
        id = this.id,
        tenantId = tenantId,
        ten = this.ten,
        sdt = this.sdt,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun CustomerEntity.toDomain(): KH {
    return KH(
        id = this.id,
        ten = this.ten,
        sdt = this.sdt
    )
}

fun DK.toEntity(tenantId: String = DEFAULT_TENANT_ID): PackageSubEntity {
    return PackageSubEntity(
        id = this.id,
        tenantId = tenantId,
        khId = this.khId,
        goiId = this.goiId,
        ngay = this.ngay,
        daDung = this.daDung,
        luotDaDung = this.luotDaDung,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun PackageSubEntity.toDomain(): DK {
    return DK(
        id = this.id,
        khId = this.khId,
        goiId = this.goiId,
        ngay = this.ngay,
        daDung = this.daDung,
        luotDaDung = this.luotDaDung
    )
}

fun Luot.toEntity(tenantId: String = DEFAULT_TENANT_ID): TicketEntity {
    return TicketEntity(
        id = this.id,
        tenantId = tenantId,
        khId = this.khId,
        nvId = this.nvId,
        dvIds = this.dvIds,
        dkId = this.dkId,
        tgbd = this.tgbd,
        tgkt = this.tgkt,
        thu = this.thu,
        daXacNhan = this.daXacNhan,
        ngay = this.ngay,
        dvNgoai = this.dvNgoai,
        hoaHongChot = this.hoaHongChot,
        phieuChiHoaHongId = this.phieuChiHoaHongId,
        phieuThuVeId = this.phieuThuVeId,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun TicketEntity.toDomain(): Luot {
    return Luot(
        id = this.id,
        khId = this.khId,
        nvId = this.nvId,
        dvIds = this.dvIds,
        dkId = this.dkId,
        tgbd = this.tgbd,
        tgkt = this.tgkt,
        thu = this.thu,
        daXacNhan = this.daXacNhan,
        ngay = this.ngay,
        dvNgoai = this.dvNgoai,
        hoaHongChot = this.hoaHongChot,
        phieuChiHoaHongId = this.phieuChiHoaHongId,
        phieuThuVeId = this.phieuThuVeId
    )
}

fun TC.toEntity(tenantId: String = DEFAULT_TENANT_ID): CashbookEntity {
    return CashbookEntity(
        id = this.id,
        tenantId = tenantId,
        loai = this.loai,
        tien = this.tien,
        lyDo = this.lyDo,
        ngay = this.ngay,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun CashbookEntity.toDomain(): TC {
    return TC(
        id = this.id,
        loai = this.loai,
        tien = this.tien,
        lyDo = this.lyDo,
        ngay = this.ngay
    )
}

fun TenantInfo.toEntity(): TenantEntity {
    return TenantEntity(
        tenantId = this.tenantId,
        shopName = this.shopName,
        status = this.status,
        isLocked = this.isLocked,
        lockedReason = this.lockedReason,
        subscriptionExpiresAt = this.subscriptionExpiresAt,
        planId = this.planId,
        ownerPhone = this.ownerPhone,
        ownerEmail = this.ownerEmail,
        qrPaymentUrl = this.qrPaymentUrl,
        accountsCount = this.accountsCount,
        ticketsCount = this.ticketsCount,
        customersCount = this.customersCount,
        createdAt = this.createdAt,
        isSynced = false,
        updatedAt = System.currentTimeMillis()
    )
}

fun TenantEntity.toDomain(): TenantInfo {
    return TenantInfo(
        tenantId = this.tenantId,
        shopName = this.shopName,
        status = this.status,
        isLocked = this.isLocked,
        lockedReason = this.lockedReason,
        subscriptionExpiresAt = this.subscriptionExpiresAt,
        planId = this.planId,
        ownerPhone = this.ownerPhone,
        ownerEmail = this.ownerEmail,
        qrPaymentUrl = this.qrPaymentUrl,
        accountsCount = this.accountsCount,
        ticketsCount = this.ticketsCount,
        customersCount = this.customersCount,
        createdAt = this.createdAt
    )
}
