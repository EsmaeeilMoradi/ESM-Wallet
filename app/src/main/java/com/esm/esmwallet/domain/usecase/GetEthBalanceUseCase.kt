package com.esm.esmwallet.domain.usecase


import com.esm.esmwallet.data.repository.WalletRepository
import java.math.BigInteger

class GetEthBalanceUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(walletAddress: String): BigInteger {
        return repository.getEthBalance(walletAddress)
    }
}