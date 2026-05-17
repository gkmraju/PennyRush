package dev.pennyrush.feature.statements

import dev.pennyrush.core.common.ImportCandidate

interface StatementParser {
    suspend fun parse(bytes: ByteArray, fileName: String, mimeType: String?): List<ImportCandidate>
}
