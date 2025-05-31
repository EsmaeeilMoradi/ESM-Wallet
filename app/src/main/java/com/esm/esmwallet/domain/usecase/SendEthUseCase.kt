package com.esm.esmwallet.domain.usecase


import com.esm.esmwallet.data.repository.WalletRepository
import java.math.BigInteger

class SendEthUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(
        privateKey: String,
        toAddress: String,
        amountWei: BigInteger
    ): String {
        return walletRepository.sendEth(privateKey, toAddress, amountWei)
    }
}
