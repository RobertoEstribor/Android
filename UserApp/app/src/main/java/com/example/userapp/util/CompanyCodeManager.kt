package com.example.userapp.util // Create new subpackage util

import android.content.Context
import android.content.SharedPreferences

object CompanyCodeManager {
    private const val PREFS_NAME = "UserAppPrefs"
    private const val KEY_COMPANY_CODE = "company_code"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveCompanyCode(context: Context, code: String) {
        getPreferences(context).edit().putString(KEY_COMPANY_CODE, code).apply()
    }

    fun getCompanyCode(context: Context): String? {
        return getPreferences(context).getString(KEY_COMPANY_CODE, null)
    }

    fun clearCompanyCode(context: Context) {
        getPreferences(context).edit().remove(KEY_COMPANY_CODE).apply()
    }
}
