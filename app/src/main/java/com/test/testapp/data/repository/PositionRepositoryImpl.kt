package com.test.testapp.data.repository

import com.test.testapp.data.local.PositionLocalDataSource
import com.test.testapp.domain.models.IconPosition
import com.test.testapp.domain.repository.PositionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PositionRepositoryImpl @Inject constructor(
    private val local: PositionLocalDataSource
) : PositionRepository {

    override val positions: Flow<IconPosition> =
        local.positions.map { (x, y) -> IconPosition(x, y) }

    override suspend fun save(xRatio: Float, yRatio: Float) {
        local.save(xRatio, yRatio)
    }
}
