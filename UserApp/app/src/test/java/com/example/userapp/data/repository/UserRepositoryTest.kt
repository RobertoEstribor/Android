package com.example.userapp.data.repository

import com.example.userapp.data.db.UserDao
import com.example.userapp.data.db.UserEntity
import com.example.userapp.data.model.User
import com.example.userapp.data.network.SoapService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*

class UserRepositoryTest {

    private lateinit var mockSoapService: SoapService
    private lateinit var mockUserDao: UserDao
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        mockSoapService = mock(SoapService::class.java)
        mockUserDao = mock(UserDao::class.java)
        userRepository = UserRepository(mockSoapService, mockUserDao)
    }

    // Tests for getUsersFromSoap

    @Test
    fun `getUsersFromSoap success returnsUserList`() = runBlocking {
        val validJson = """[{"login":"user1","nombre":"User One","pass":"pass1"},{"login":"user2","nombre":"User Two","pass":"pass2"}]"""
        `when`(mockSoapService.getUsers(anyString())).thenReturn(validJson)

        val result = userRepository.getUsersFromSoap("testCompany")

        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals("user1", result?.get(0)?.login)
        assertEquals("User One", result?.get(0)?.nombre)
        assertEquals("pass1", result?.get(0)?.pass)
        assertEquals("user2", result?.get(1)?.login)
    }

    @Test
    fun `getUsersFromSoap emptyJsonArray returnsEmptyList`() = runBlocking {
        val emptyJsonArray = "[]"
        `when`(mockSoapService.getUsers(anyString())).thenReturn(emptyJsonArray)

        val result = userRepository.getUsersFromSoap("testCompany")

        assertNotNull(result)
        assertTrue(result?.isEmpty() == true)
    }

    @Test
    fun `getUsersFromSoap nullResponse returnsNull`() = runBlocking {
        `when`(mockSoapService.getUsers(anyString())).thenReturn(null)

        val result = userRepository.getUsersFromSoap("testCompany")

        assertNull(result)
    }

    @Test
    fun `getUsersFromSoap malformedJsonResponse returnsNull`() = runBlocking {
        val malformedJson = """[{"login":"user1","nombre":"User One",pass:"pass1"}]""" // Missing quotes around pass key
        `when`(mockSoapService.getUsers(anyString())).thenReturn(malformedJson)

        val result = userRepository.getUsersFromSoap("testCompany")

        assertNull(result)
    }

    // Tests for fetchAndSaveUsers

    @Test
    fun `fetchAndSaveUsers success clearsAndInsertsData returnsTrue`() = runBlocking {
        val validJson = """[{"login":"user1","nombre":"User One","pass":"pass1"}]"""
        val expectedUserEntities = listOf(UserEntity(login = "user1", nombre = "User One", pass = "pass1"))

        `when`(mockSoapService.getUsers(anyString())).thenReturn(validJson)
        // Mock DAO methods to do nothing (default behavior for void methods)
        // `when`(mockUserDao.clearAllUsers()).thenReturn(Unit) // Not strictly necessary for void
        // `when`(mockUserDao.insertAll(anyList())).thenReturn(Unit) // Not strictly necessary for void

        val result = userRepository.fetchAndSaveUsers("testCompany")

        assertTrue(result)
        verify(mockUserDao).clearAllUsers()

        // Capture the argument passed to insertAll
        val listCaptor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<UserEntity>>
        verify(mockUserDao).insertAll(listCaptor.capture())
        
        val capturedList = listCaptor.value
        assertNotNull(capturedList)
        assertEquals(1, capturedList.size)
        assertEquals(expectedUserEntities[0].login, capturedList[0].login)
        assertEquals(expectedUserEntities[0].nombre, capturedList[0].nombre)
        assertEquals(expectedUserEntities[0].pass, capturedList[0].pass)
    }

    @Test
    fun `fetchAndSaveUsers soapReturnsNull doesNotCallDao returnsFalse`() = runBlocking {
        `when`(mockSoapService.getUsers(anyString())).thenReturn(null)

        val result = userRepository.fetchAndSaveUsers("testCompany")

        assertFalse(result)
        verify(mockUserDao, never()).clearAllUsers()
        verify(mockUserDao, never()).insertAll(anyList())
    }
    
    @Test
    fun `fetchAndSaveUsers soapReturnsEmptyList doesNotCallDaoReturnsFalse`() = runBlocking {
        `when`(mockSoapService.getUsers(anyString())).thenReturn("[]")

        val result = userRepository.fetchAndSaveUsers("testCompany")

        assertFalse(result) // Based on current implementation, empty list from SOAP also leads to false
        verify(mockUserDao, never()).clearAllUsers()
        verify(mockUserDao, never()).insertAll(anyList())
    }

    @Test
    fun `fetchAndSaveUsers daoThrowsExceptionOnClear returnsFalse`() = runBlocking {
        val validJson = """[{"login":"user1","nombre":"User One","pass":"pass1"}]"""
        `when`(mockSoapService.getUsers(anyString())).thenReturn(validJson)
        `when`(mockUserDao.clearAllUsers()).thenThrow(RuntimeException("DB error on clear"))

        val result = userRepository.fetchAndSaveUsers("testCompany")

        assertFalse(result)
        verify(mockUserDao).clearAllUsers() // clearAllUsers is called
        verify(mockUserDao, never()).insertAll(anyList()) // insertAll should not be called
    }

    @Test
    fun `fetchAndSaveUsers daoThrowsExceptionOnInsert returnsFalse`() = runBlocking {
        val validJson = """[{"login":"user1","nombre":"User One","pass":"pass1"}]"""
        `when`(mockSoapService.getUsers(anyString())).thenReturn(validJson)
        // clearAllUsers works fine
        `when`(mockUserDao.insertAll(anyList())).thenThrow(RuntimeException("DB error on insert"))

        val result = userRepository.fetchAndSaveUsers("testCompany")

        assertFalse(result)
        verify(mockUserDao).clearAllUsers()
        verify(mockUserDao).insertAll(anyList()) // insertAll is called
    }
}
