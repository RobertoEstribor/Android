package com.example.yourappname.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import com.google.common.truth.Truth.assertThat // Popular librería de aserciones

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var userDao: UserDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Permite consultas en el hilo principal solo para pruebas
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() = runBlocking {
        val user = UserEntity("123", "pass123", "Test User")
        userDao.insertAll(listOf(user))
        val byEntrada = userDao.getUserByEntrada("123")
        assertThat(byEntrada).isEqualTo(user)
    }

    @Test
    @Throws(Exception::class)
    fun getAllUsers() = runBlocking {
        val user1 = UserEntity("001", "pass1", "User One")
        val user2 = UserEntity("002", "pass2", "User Two")
        userDao.insertAll(listOf(user1, user2))
        val allUsers = userDao.getAllUsers()
        assertThat(allUsers).containsExactly(user1, user2)
    }

    @Test
    @Throws(Exception::class)
    fun deleteAllUsers() = runBlocking {
        val user1 = UserEntity("001", "pass1", "User One")
        userDao.insertAll(listOf(user1))
        userDao.deleteAllUsers()
        val allUsers = userDao.getAllUsers()
        assertThat(allUsers).isEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun getUserByNonExistentEntrada() = runBlocking {
        val user = userDao.getUserByEntrada("nonexistent")
        assertThat(user).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun insertDuplicateReplace() = runBlocking {
        val user1 = UserEntity("123", "pass1", "Original User")
        userDao.insertAll(listOf(user1))
        val user2Updated = UserEntity("123", "newPass", "Updated User")
        userDao.insertAll(listOf(user2Updated)) // Debería reemplazar debido a OnConflictStrategy.REPLACE

        val byEntrada = userDao.getUserByEntrada("123")
        assertThat(byEntrada).isEqualTo(user2Updated)
        assertThat(byEntrada?.nombre).isEqualTo("Updated User")

        val allUsers = userDao.getAllUsers()
        assertThat(allUsers.size).isEqualTo(1)
    }
}
