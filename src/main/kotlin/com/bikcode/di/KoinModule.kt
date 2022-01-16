package com.bikcode.di

import com.bikcode.repository.HeroRepository
import com.bikcode.repository.HeroRepositoryImpl
import org.koin.dsl.module

val koinModule = module {
    single<HeroRepository> {
        HeroRepositoryImpl()
    }
}