package dev.pennyrush.core.security

data class AppLockPolicy(
    val enabled: Boolean = false,
    val timeoutSeconds: Int = 60,
)
