package com.esm.esmwallet

import android.app.Application
class ESMWalletApp : Application() {
    lateinit var dependencyGraph: DependencyGraph

    override fun onCreate() {
        super.onCreate()
        dependencyGraph = DependencyGraph(this)

    }
}
