package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CashbookEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.PackageEntity
import com.example.data.local.entity.PackageSubEntity
import com.example.data.local.entity.ServiceEntity
import com.example.data.local.entity.StaffEntity
import com.example.data.local.entity.TenantEntity
import com.example.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

// =============================================================================
// ROOM DAOS CHO TOÀN BỘ CÁC BẢNG (CÁCH LY DỮ LIỆU MULTI-TENANT)
// =============================================================================

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE tenantId = :tenantId ORDER BY id ASC")
    fun getAllByTenant(tenantId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE tenantId = :tenantId ORDER BY id ASC")
    suspend fun getAllByTenantSync(tenantId: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id AND tenantId = :tenantId LIMIT 1")
    suspend fun getById(id: String, tenantId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE username = :username AND tenantId = :tenantId LIMIT 1")
    suspend fun getByUsername(username: String, tenantId: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AccountEntity>)

    @Update
    suspend fun update(entity: AccountEntity)

    @Delete
    suspend fun delete(entity: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id AND tenantId = :tenantId")
    suspend fun deleteById(id: String, tenantId: String)

    @Query("DELETE FROM accounts WHERE tenantId = :tenantId")
    suspend fun deleteAllByTenant(tenantId: String)
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE tenantId = :tenantId ORDER BY ten ASC")
    fun getAllByTenant(tenantId: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE tenantId = :tenantId ORDER BY ten ASC")
    suspend fun getAllByTenantSync(tenantId: String): List<StaffEntity>

    @Query("SELECT * FROM staff WHERE id = :id AND tenantId = :tenantId LIMIT 1")
    suspend fun getById(id: String, tenantId: String): StaffEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StaffEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<StaffEntity>)

    @Update
    suspend fun update(entity: StaffEntity)

    @Delete
    suspend fun delete(entity: StaffEntity)

    @Query("DELETE FROM staff WHERE id = :id AND tenantId = :tenantId")
    suspend fun deleteById(id: String, tenantId: String)

    @Query("DELETE FROM staff WHERE tenantId = :tenantId")
    suspend fun deleteAllByTenant(tenantId: String)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE tenantId = :tenantId ORDER BY phut ASC")
    fun getAllByTenant(tenantId: String): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE tenantId = :tenantId ORDER BY phut ASC")
    suspend fun getAllByTenantSync(tenantId: String): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE id = :id AND tenantId = :tenantId LIMIT 1")
    suspend fun getById(id: String, tenantId: String): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ServiceEntity>)

    @Update
    suspend fun update(entity: ServiceEntity)

    @Delete
    suspend fun delete(entity: ServiceEntity)

    @Query("DELETE FROM services WHERE id = :id AND tenantId = :tenantId")
    suspend fun deleteById(id: String, tenantId: String)

    @Query("DELETE FROM services WHERE tenantId = :tenantId")
    suspend fun deleteAllByTenant(tenantId: String)
}

@Dao
interface PackageDao {
    @Query("SELECT * FROM packages WHERE tenantId = :tenantId ORDER BY soBuoi ASC")
    fun getAllByTenant(tenantId: String): Flow<List<PackageEntity>>

    @Query("SELECT * FROM packages WHERE tenantId = :tenantId ORDER BY soBuoi ASC")
    suspend fun getAllByTenantSync(tenantId: String): List<PackageEntity>

    @Query("SELECT * FROM packages WHERE id = :id AND tenantId = :tenantId LIMIT 1")
    suspend fun getById(id: String, tenantId: String): PackageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PackageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PackageEntity>)

    @Update
    suspend fun update(entity: PackageEntity)

    @Delete
    suspend fun delete(entity: PackageEntity)

    @Query("DELETE FROM packages WHERE id = :id AND tenantId = :tenantId")
    suspend fun deleteById(id: String, tenantId: String)

    @Query("DELETE FROM packages WHERE tenantId = :tenantId")
    suspend fun deleteAllByTenant(tenantId: String)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE tenantId = :tenantId ORDER BY ten ASC")
    fun getAllByTenant(tenantId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE tenantId = :tenantId ORDER BY ten ASC")
    suspend fun getAllByTenantSync(tenantId: String): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id AND tenantId = :tenantId LIMIT 1")
    suspend fun getById(id: String, tenantId: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE sdt = :sdt AND tenantId = :tenantId LIMIT 1")
    suspend fun getByPhone(sdt: String, tenantId: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CustomerEntity>)

    @Update
    suspend fun update(entity: CustomerEntity)

    @Delete
    suspend fun delete(entity: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id AND tenantId = :tenantId")
    suspend fun deleteById(id: String, tenantId: String)

    @Query("DELETE FROM customers WHERE tenantId = :tenantId")
    suspend fun deleteAllByTenant(tenantId: String)
}

@Dao
interface PackageSubDao {
    @Query("SELECT * FROM package_subscriptions WHERE tenantId = :tenantId ORDER BY ngay DESC")
    fun getAllByTenant(tenantId: String): Flow<List<PackageSubEntity>>

    @Query("SELECT * FROM package_subscriptions WHERE tenantId = :tenantId ORDER BY ngay DESC")
    suspend fun getAllByTenantSync(tenantId: String): List<PackageSubEntity>

    @Query("SELECT * FROM package_subscriptions WHERE khId = :khId AND tenantId = :tenantId")
    suspend fun getByCustomer(khId: String, tenantId: String): List<PackageSubEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PackageSubEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PackageSubEntity>)

    @Update
    suspend fun update(entity: PackageSubEntity)

    @Delete
    suspend fun delete(entity: PackageSubEntity)

    @Query("DELETE FROM package_subscriptions WHERE id = :id AND tenantId = :tenantId")
    suspend fun deleteById(id: String, tenantId: String)

    @Query("DELETE FROM package_subscriptions WHERE tenantId = :tenantId")
    suspend fun deleteAllByTenant(tenantId: String)
}

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE tenantId = :tenantId AND ngay = :date ORDER BY tgbd DESC")
    fun getByTenantAndDate(tenantId: String, date: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE tenantId = :tenantId ORDER BY ngay DESC, tgbd DESC")
    fun getAllByTenant(tenantId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE tenantId = :tenantId ORDER BY ngay DESC, tgbd DESC")
    suspend fun getAllByTenantSync(tenantId: String): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE tenantId = :tenantId AND isSynced = 0")
    suspend fun getUnsynced(tenantId: String): List<TicketEntity>

    @Query("SELECT * FROM tickets WHERE id = :id AND tenantId = :tenantId LIMIT 1")
    suspend fun getById(id: String, tenantId: String): TicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TicketEntity>)

    @Update
    suspend fun update(entity: TicketEntity)

    @Delete
    suspend fun delete(entity: TicketEntity)

    @Query("DELETE FROM tickets WHERE id = :id AND tenantId = :tenantId")
    suspend fun deleteById(id: String, tenantId: String)

    @Query("DELETE FROM tickets WHERE tenantId = :tenantId")
    suspend fun deleteAllByTenant(tenantId: String)
}

@Dao
interface CashbookDao {
    @Query("SELECT * FROM cashbook WHERE tenantId = :tenantId AND ngay = :date ORDER BY id DESC")
    fun getByTenantAndDate(tenantId: String, date: String): Flow<List<CashbookEntity>>

    @Query("SELECT * FROM cashbook WHERE tenantId = :tenantId ORDER BY ngay DESC")
    fun getAllByTenant(tenantId: String): Flow<List<CashbookEntity>>

    @Query("SELECT * FROM cashbook WHERE tenantId = :tenantId ORDER BY ngay DESC")
    suspend fun getAllByTenantSync(tenantId: String): List<CashbookEntity>

    @Query("SELECT * FROM cashbook WHERE tenantId = :tenantId AND isSynced = 0")
    suspend fun getUnsynced(tenantId: String): List<CashbookEntity>

    @Query("SELECT * FROM cashbook WHERE id = :id AND tenantId = :tenantId LIMIT 1")
    suspend fun getById(id: String, tenantId: String): CashbookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CashbookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CashbookEntity>)

    @Update
    suspend fun update(entity: CashbookEntity)

    @Delete
    suspend fun delete(entity: CashbookEntity)

    @Query("DELETE FROM cashbook WHERE id = :id AND tenantId = :tenantId")
    suspend fun deleteById(id: String, tenantId: String)

    @Query("DELETE FROM cashbook WHERE tenantId = :tenantId")
    suspend fun deleteAllByTenant(tenantId: String)
}

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE tenantId = :tenantId LIMIT 1")
    fun getTenantById(tenantId: String): Flow<TenantEntity?>

    @Query("SELECT * FROM tenants WHERE tenantId = :tenantId LIMIT 1")
    suspend fun getTenantByIdSync(tenantId: String): TenantEntity?

    @Query("SELECT * FROM tenants ORDER BY shopName ASC")
    fun getAllTenants(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants ORDER BY shopName ASC")
    suspend fun getAllTenantsSync(): List<TenantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tenant: TenantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tenants: List<TenantEntity>)

    @Update
    suspend fun update(tenant: TenantEntity)

    @Delete
    suspend fun delete(tenant: TenantEntity)

    @Query("DELETE FROM tenants WHERE tenantId = :tenantId")
    suspend fun deleteById(tenantId: String)
}
