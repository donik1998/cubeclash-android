package com.donik1998.cubeclash.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.donik1998.cubeclash.core.domain.repository.TokenStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore("cubeclash_tokens")

/**
 * The JWT pair.
 *
 * **Known gap, stated rather than hidden:** these are stored in plain Preferences DataStore.
 * On a non-rooted device that is app-private, but it is not the same as being encrypted at
 * rest — wrapping the file in a Keystore-backed cipher is a tracked follow-up, not something
 * this scaffold pretends to have done.
 */
@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : TokenStore {

    override val isSignedIn: Flow<Boolean> =
        context.tokenDataStore.data.map { !it[Keys.ACCESS].isNullOrBlank() }

    override suspend fun accessToken(): String? = context.tokenDataStore.data.first()[Keys.ACCESS]

    override suspend fun refreshToken(): String? = context.tokenDataStore.data.first()[Keys.REFRESH]

    override suspend fun save(access: String, refresh: String) {
        context.tokenDataStore.edit {
            it[Keys.ACCESS] = access
            it[Keys.REFRESH] = refresh
        }
    }

    override suspend fun clear() {
        context.tokenDataStore.edit { it.clear() }
    }

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
    }
}
