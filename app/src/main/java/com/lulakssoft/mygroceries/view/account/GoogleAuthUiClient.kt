package com.lulakssoft.mygroceries.view.account

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.lulakssoft.mygroceries.R
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient = Identity.getSignInClient(context),
    private val credentialManager: CredentialManager = CredentialManager.create(context),
) {
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "GoogleAuthUiClient"

    suspend fun signIn(): SignInResult {
        return try {
            Log.d(TAG, "Starting sign-in process")

            val googleIdOption =
                GetGoogleIdOption
                    .Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()

            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

            Log.d(TAG, "Requesting credential from CredentialManager")
            val result =
                credentialManager.getCredential(
                    request = request,
                    context = context,
                )

            val credential = result.credential
            Log.d(TAG, "Received credential - Class: ${credential::class.java.simpleName}, Type: ${credential.type}")

            when {
                credential.type == "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL" -> {
                    Log.d(TAG, "Processing Google ID token credential")
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)?.idToken

                    if (googleIdToken != null) {
                        Log.d(TAG, "Successfully extracted Google ID token (first 10 chars): ${googleIdToken.take(10)}...")

                        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
                        Log.d(TAG, "Created Firebase credentials, signing in...")

                        try {
                            val authResult = auth.signInWithCredential(googleCredentials).await()
                            val user = authResult.user

                            if (user != null) {
                                Log.d(TAG, "Sign in successful for user ID: ${user.uid}")
                                return SignInResult(
                                    data = user.toUserData(),
                                    errorMessage = null,
                                )
                            } else {
                                Log.e(TAG, "Auth result returned null user")
                                return SignInResult(
                                    data = null,
                                    errorMessage = "Google sign in failed, no user returned",
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Firebase authentication failed", e)
                            return SignInResult(
                                data = null,
                                errorMessage = "Firebase authentication error: ${e.message}",
                            )
                        }
                    } else {
                        Log.e(TAG, "Failed to extract Google ID token from credential")
                        return SignInResult(
                            data = null,
                            errorMessage = "Could not extract Google ID token from credential",
                        )
                    }
                }
                else -> {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    return SignInResult(
                        data = null,
                        errorMessage = "No Google ID token found in response (type: ${credential.type})",
                    )
                }
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential manager exception", e)
            SignInResult(
                data = null,
                errorMessage = "Sign in failed: ${e.message}",
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception during sign in", e)
            SignInResult(
                data = null,
                errorMessage = "Unexpected error: ${e.message}",
            )
        }
    }

    fun getSignedInUser(): FirebaseUser? {
        val currentUser = auth.currentUser
        return currentUser
    }

    suspend fun signOut() {
        try {
            Log.d(TAG, "Signing out user")
            oneTapClient.signOut().await()
            auth.signOut()
            Log.d(TAG, "Sign out completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
            throw e
        }
    }

    fun getContext(): Context = context
}
