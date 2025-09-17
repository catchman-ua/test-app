package com.test.testapp.di

import android.content.Context
import com.test.testapp.data.local.PositionLocalDataSource
import com.test.testapp.data.repository.PositionRepositoryImpl
import com.test.testapp.domain.repository.PositionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProvidersModule {

    @Provides
    @Singleton
    fun providePositionLocalDataSource(
        @ApplicationContext context: Context
    ): PositionLocalDataSource = PositionLocalDataSource(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindsModule {

    @Binds
    @Singleton
    abstract fun bindPositionRepository(
        impl: PositionRepositoryImpl
    ): PositionRepository
}
