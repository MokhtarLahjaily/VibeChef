package com.lahjaily.vibechef.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.lahjaily.vibechef.data.AuthRepository
import com.lahjaily.vibechef.data.FirestoreRepository
import com.lahjaily.vibechef.data.GeminiRepository
import com.lahjaily.vibechef.data.local.AppDatabase
import com.lahjaily.vibechef.data.local.RecipeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("vibechef_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vibechef_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(database: AppDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @Singleton
    fun provideFirestoreRepository(): FirestoreRepository {
        return FirestoreRepository()
    }

    @Provides
    @Singleton
    fun provideGeminiRepository(): GeminiRepository {
        return GeminiRepository()
    }
}
