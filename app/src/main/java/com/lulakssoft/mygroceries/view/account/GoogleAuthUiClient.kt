package com.lulakssoft.mygroceries.view.account

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lulakssoft.mygroceries.R
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(
    private val context: Context,
) {
    private val auth = Firebase.auth

    suspend fun signIn(): Result<FirebaseUser> {
        val credentialManager = CredentialManager.create(context)

        println("Web client ID: ${context.getString(R.string.your_web_client_id)}")

        // Use GoogleIdTokenCredentialOption instead of GoogleIdOption
        val googleIdOption =
            GetGoogleIdOption
                .Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.your_web_client_id))
                .build()

        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(googleIdOption)
                .build()

        return try {
            val credentialResponse =
                credentialManager.getCredential(
                    request = request,
                    context = context,
                )

            when (val credential = credentialResponse.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential
                                .createFrom(credential.data)

                        println("Google ID token: ${googleIdTokenCredential.idToken}")

                        val firebaseCredential =
                            GoogleAuthProvider.getCredential(
                                googleIdTokenCredential.idToken,
                                null,
                            )

                        println("Firebase credential: $firebaseCredential")

                        val authResult = auth.signInWithCredential(firebaseCredential).await()
                        Result.success(authResult.user!!)
                    } else {
                        Result.failure(Exception("Unexpected credential type"))
                    }
                }
                else -> Result.failure(Exception("Unexpected credential type"))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSignedInUser(): FirebaseUser? = auth.currentUser

    suspend fun signOut() {
        auth.signOut()
    }
}
