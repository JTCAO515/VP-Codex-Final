package space.go2china.visepanda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import space.go2china.visepanda.data.datastore.UserPreferencesDataStore

/**
 * Scoped to the Activity (created once in [space.go2china.visepanda.navigation.VisePandaApp]
 * and read again from `MeScreen`'s language row) so the whole app shares one
 * language selection — see DESIGN.md ADR-118.
 */
@HiltViewModel
class AppLanguageViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    val languageCode: StateFlow<String> = userPreferencesDataStore.languageCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "en")

    fun setLanguage(code: String) {
        viewModelScope.launch { userPreferencesDataStore.setLanguageCode(code) }
    }
}
