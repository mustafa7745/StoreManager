package com.fekraplatform.storemanger.repositories

import com.fekraplatform.storemanger.storage.BillingDao
import com.fekraplatform.storemanger.storage.BillingEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val billingDao: BillingDao
) {

    suspend fun addBilling(billing: BillingEntity) {
        billingDao.insert(billing)
    }

    suspend fun getAllBillings(): List<BillingEntity> {
        return billingDao.getAll()
    }

    suspend fun getBillingByToken(token: String): BillingEntity? {
        return billingDao.getByToken(token)
    }

    suspend fun removeBilling(billing: BillingEntity) {
        billingDao.delete(billing)
    }
}
