package com.esm.esmwallet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.esm.esmwallet.data.model.CustomToken

@Dao
interface CustomTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomToken(token: CustomToken)

    @Query("SELECT * FROM custom_tokens")
    fun getAllCustomTokens(): Flow<List<CustomToken>>

    @Query("DELETE FROM custom_tokens WHERE contractAddress = :contractAddress")
    suspend fun deleteCustomToken(contractAddress: String)

    @Query("SELECT * FROM custom_tokens WHERE contractAddress = :contractAddress LIMIT 1")
    suspend fun getCustomTokenByAddress(contractAddress: String): CustomToken?
}