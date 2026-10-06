package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {

    private val credentialManager: CredentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "FirebaseApp not initialized: ${e.message}")
            null
        }
    }

    val currentFirebaseUser: FirebaseUser?
        get() = try { firebaseAuth?.currentUser } catch (e: Exception) { null }

    suspend fun signInWithGoogle(activity: Activity): Result<GoogleUserResult> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("692979032431-client-id.apps.googleusercontent.com")
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")

                // If Firebase Auth is available, sign in with Firebase credential
                try {
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth?.signInWithCredential(authCredential)?.await()
                } catch (e: Exception) {
                    Log.w("AuthManager", "Firebase signInWithCredential skipped or failed: ${e.message}")
                }

                Result.success(
                    GoogleUserResult(
                        email = email,
                        displayName = displayName,
                        idToken = idToken
                    )
                )
            } else {
                Result.failure(Exception("نوع الاعتماد غير مدعوم"))
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign-In via Credential Manager failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w("AuthManager", "Sign out failed: ${e.message}")
        }
    }
}

data class GoogleUserResult(
    val email: String,
    val displayName: String,
    val idToken: String
)
