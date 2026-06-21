package com.example.client

import net.fabricmc.api.ClientModInitializer

object ExampleModClient : ClientModInitializer {
    override fun onInitializeClient() {
        // this entrypoint is suitable for setting up client-specific logic, such as rendering
    }
}