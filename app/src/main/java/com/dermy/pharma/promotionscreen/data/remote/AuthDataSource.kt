package com.dermy.pharma.promotionscreen.data.remote

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AuthDataSource {
    fun getLastSignedInAccount(context: Context): GoogleSignInAccount?
    fun getSignInIntent(context: Context): Intent
    suspend fun getAccessToken(context: Context, account: GoogleSignInAccount): String
    fun signOut(context: Context)
}

class AuthDataSourceImpl : AuthDataSource {

    override fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    override fun getSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(SCOPE_DRIVE_READONLY_API))
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    override suspend fun getAccessToken(context: Context, account: GoogleSignInAccount): String {
        return withContext(Dispatchers.IO) {
            GoogleAuthUtil.getToken(context, account.account!!, SCOPE_DRIVE_READONLY)
        }
    }

    override fun signOut(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(context, gso).signOut()
    }

    companion object {
        private const val SCOPE_DRIVE_READONLY_API = "https://www.googleapis.com/auth/drive.readonly"
        private const val SCOPE_DRIVE_READONLY = "oauth2:$SCOPE_DRIVE_READONLY_API"
    }
}
