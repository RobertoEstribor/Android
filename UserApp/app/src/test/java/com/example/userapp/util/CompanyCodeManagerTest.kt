package com.example.userapp.util

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class CompanyCodeManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    // Constants from CompanyCodeManager (assuming they are private, so redefine for test or make them internal/public)
    // For this test, we'll assume PREF_NAME and KEY_COMPANY_CODE are accessible or we use anyString()
    // If they are private, CompanyCodeManager would need to be refactored for better testability
    // or use reflection (not recommended for unit tests).
    // Let's assume CompanyCodeManager.PREF_NAME and CompanyCodeManager.KEY_COMPANY_CODE are accessible for now
    // If not, the test will rely on anyString() for these, which is less precise.
    // For the purpose of this test, we will use constants that match the implementation.
    private val PREF_NAME = "UserAppPrefs"
    private val KEY_COMPANY_CODE = "CompanyCode"


    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        // Common mock setup
        `when`(mockContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        // Make editor methods chainable by returning the editor itself
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
    }

    @Test
    fun saveCompanyCode_retrievesSameCode() {
        val testCode = "testCode123"

        // Configure editor.putString to capture or simulate the save
        // We don't need to capture, just ensure it's called.
        // Configure sharedPreferences.getString to return the "saved" code
        `when`(mockSharedPreferences.getString(KEY_COMPANY_CODE, null)).thenReturn(testCode)

        CompanyCodeManager.saveCompanyCode(mockContext, testCode)

        // Verify that putString and apply were called on the editor
        verify(mockEditor).putString(KEY_COMPANY_CODE, testCode)
        verify(mockEditor).apply()

        // Call getCompanyCode and assert that it returns "testCode123"
        val retrievedCode = CompanyCodeManager.getCompanyCode(mockContext)
        assertEquals(testCode, retrievedCode)
    }

    @Test
    fun getCompanyCode_returnsNull_whenNotSet() {
        // Configure sharedPreferences.getString to return null
        `when`(mockSharedPreferences.getString(KEY_COMPANY_CODE, null)).thenReturn(null)

        val retrievedCode = CompanyCodeManager.getCompanyCode(mockContext)
        assertNull(retrievedCode)
    }

    @Test
    fun clearCompanyCode_removesCode() {
        // Simulate a code being present initially
        val initialCode = "testCodeToClear"
        `when`(mockSharedPreferences.getString(KEY_COMPANY_CODE, null))
            .thenReturn(initialCode) // Before clear
            .thenReturn(null)      // After clear

        // Call save to ensure a value "could" be there (optional, as getString is mocked)
        // CompanyCodeManager.saveCompanyCode(mockContext, initialCode)

        CompanyCodeManager.clearCompanyCode(mockContext)

        // Verify that remove and apply were called on the editor
        verify(mockEditor).remove(KEY_COMPANY_CODE)
        verify(mockEditor).apply()

        // Call getCompanyCode and assert that it returns null
        val retrievedCode = CompanyCodeManager.getCompanyCode(mockContext)
        assertNull(retrievedCode)
    }
}
