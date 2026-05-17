package dev.pennyrush.feature.insights

import kotlinx.serialization.Serializable

@Serializable
data class InsightCard(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val severity: String,
)
