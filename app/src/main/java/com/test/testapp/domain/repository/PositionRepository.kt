package com.test.testapp.domain.repository

import com.test.testapp.domain.models.IconPosition
import kotlinx.coroutines.flow.Flow

interface PositionRepository {
    val positions: Flow<IconPosition>
    suspend fun save(xRatio: Float, yRatio: Float)
}