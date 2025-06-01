package com.esm.esmwallet.domain.usecase

import com.esm.esmwallet.data.repository.WalletRepository
import java.math.BigInteger

class SendErc20TokenUseCase(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(
        privateKey: String,
        tokenContractAddress: String,
        toAddress: String,
        amount: BigInteger
    ): String {
        return walletRepository.sendErc20Token(
            privateKey,
            tokenContractAddress,
            toAddress,
            amount
        )
    }
}