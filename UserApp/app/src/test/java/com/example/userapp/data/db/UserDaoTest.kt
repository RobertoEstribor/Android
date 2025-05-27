package com.example.userapp.data.db

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class UserDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        // Using a mocked Context for Room in-memory database.
        // If this causes issues, it would indicate a need for Robolectric or Instrumented tests.
        val mockContext = mock(Context::class.java)
        database = Room.inMemoryDatabaseBuilder(
            mockContext,
            AppDatabase::class.java
        ).allowMainThreadQueries() // Essential for test simplicity, avoids needing to manage dispatchers.
         .build()
        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertAll_and_getAllUsers_retrievesCorrectData`() = runBlocking {
        val userEntities = listOf(
            UserEntity("user1", "User One", "pass1"),
            UserEntity("user2", "User Two", "pass2")
        )
        userDao.insertAll(userEntities)

        val retrievedUsers = userDao.getAllUsers()
        assertNotNull(retrievedUsers)
        assertEquals(userEntities.size, retrievedUsers.size)
        // Basic check, could be more thorough by checking individual fields or using containsAll
        assertTrue(retrievedUsers.any { it.login == "user1" && it.nombre == "User One" })
        assertTrue(retrievedUsers.any { it.login == "user2" && it.nombre == "User Two" })
    }

    @Test
    fun `insertAll_onConflictReplace_updatesExistingUser`() = runBlocking {
        val initialUser = UserEntity("user1", "Old Name", "pass1")
        userDao.insertAll(listOf(initialUser))

        val updatedUser = UserEntity("user1", "New Name", "pass1") // Same login, different name
        userDao.insertAll(listOf(updatedUser)) // This should replace due to OnConflictStrategy.REPLACE

        val allUsers = userDao.getAllUsers()
        assertEquals("There should only be one user with login 'user1'.", 1, allUsers.count { it.login == "user1" })

        val retrievedUser = userDao.getUserByUsername("user1")
        assertNotNull(retrievedUser)
        assertEquals("New Name", retrievedUser?.nombre)
        assertEquals("pass1", retrievedUser?.pass) // Password should remain the same
    }

    @Test
    fun `getUserByUsername_returnsCorrectUser`() = runBlocking {
        val userEntities = listOf(
            UserEntity("user1", "User One", "pass1"),
            UserEntity("user2", "User Two", "pass2"),
            UserEntity("user3", "User Three", "pass3")
        )
        userDao.insertAll(userEntities)

        val retrievedUser = userDao.getUserByUsername("user2")
        assertNotNull(retrievedUser)
        assertEquals("user2", retrievedUser?.login)
        assertEquals("User Two", retrievedUser?.nombre)
        assertEquals("pass2", retrievedUser?.pass)
    }

    @Test
    fun `getUserByUsername_unknownUsername_returnsNull`() = runBlocking {
        val userEntities = listOf(
            UserEntity("user1", "User One", "pass1")
        )
        userDao.insertAll(userEntities)

        val retrievedUser = userDao.getUserByUsername("unknownUser")
        assertNull(retrievedUser)
    }

    @Test
    fun `clearAllUsers_emptiesTheTable`() = runBlocking {
        val userEntities = listOf(
            UserEntity("user1", "User One", "pass1"),
            UserEntity("user2", "User Two", "pass2")
        )
        userDao.insertAll(userEntities)

        var count = userDao.getAllUsers().size
        assertTrue("Table should not be empty before clear", count > 0)

        userDao.clearAllUsers()

        val usersAfterClear = userDao.getAllUsers()
        assertTrue(usersAfterClear.isEmpty())
    }
}
