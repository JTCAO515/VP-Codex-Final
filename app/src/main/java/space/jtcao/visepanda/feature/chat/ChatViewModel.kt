package space.jtcao.visepanda.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.jtcao.visepanda.data.chat.ChatRemoteDataSourceImpl
import space.jtcao.visepanda.data.chat.ChatRepositoryImpl
import space.jtcao.visepanda.data.chat.ChatSseEvent
import space.jtcao.visepanda.data.repository.TripAutoSave
import space.jtcao.visepanda.data.repository.TripRepository
import space.jtcao.visepanda.data.trip.TripAssetRepositoryImpl
import space.jtcao.visepanda.data.trip.toTripAsset
import space.jtcao.visepanda.domain.model.ChatMessageItem
import space.jtcao.visepanda.domain.model.TripAsset
import space.jtcao.visepanda.domain.usecase.SaveTripAssetUseCase
import space.jtcao.visepanda.domain.usecase.StreamChatUseCase

class ChatViewModel(
    private val streamChat: StreamChatUseCase,
    private val saveTripAsset: suspend (TripAsset) -> Unit = {},
    private val createTripAssetCandidate: (String?, String) -> TripAsset? = { _, _ -> null }
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun updateInput(value: String) {
        _uiState.value = _uiState.value.copy(input = value)
    }

    fun setCityContext(cityId: String?) {
        if (cityId == _uiState.value.cityId) return
        _uiState.value = _uiState.value.copy(cityId = cityId)
    }

    fun send(
        prompt: String = _uiState.value.input,
        cityId: String? = _uiState.value.cityId
    ) {
        if (prompt.isBlank() || _uiState.value.isStreaming) return

        val trimmedPrompt = prompt.trim()
        val userMessage = ChatMessageItem(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = trimmedPrompt,
            cityId = cityId
        )

        _uiState.value = _uiState.value.copy(
            cityId = cityId,
            input = "",
            error = null,
            isStreaming = true,
            messages = _uiState.value.messages + userMessage,
            streamingText = "",
            streamingImages = emptyList(),
            streamingFaqs = emptyList()
        )

        val history = _uiState.value.messages

        viewModelScope.launch {
            streamChat(cityId, history, trimmedPrompt).collect { event ->
                when (event) {
                    is ChatSseEvent.Token -> {
                        _uiState.value = _uiState.value.copy(
                            streamingText = _uiState.value.streamingText + event.text
                        )
                    }

                    ChatSseEvent.Split -> flushAssistantDraft(cityId, keepStreaming = true)

                    is ChatSseEvent.Image -> {
                        _uiState.value = _uiState.value.copy(
                            streamingImages = _uiState.value.streamingImages + event
                        )
                    }

                    is ChatSseEvent.Faq -> {
                        _uiState.value = _uiState.value.copy(
                            streamingFaqs = _uiState.value.streamingFaqs + event
                        )
                    }

                    ChatSseEvent.Done -> flushAssistantDraft(
                        cityId = cityId,
                        keepStreaming = false,
                        shouldSaveTrip = true
                    )

                    is ChatSseEvent.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isStreaming = false,
                            error = event.message
                        )
                    }
                }
            }
        }
    }

    private fun flushAssistantDraft(
        cityId: String?,
        keepStreaming: Boolean,
        shouldSaveTrip: Boolean = false
    ) {
        val currentState = _uiState.value
        val assistantContent = buildAssistantContent(currentState)

        _uiState.value = if (assistantContent.isBlank()) {
            currentState.copy(
                isStreaming = keepStreaming,
                streamingText = "",
                streamingImages = emptyList(),
                streamingFaqs = emptyList()
            )
        } else {
            if (shouldSaveTrip) {
                maybeSaveTrip(cityId, assistantContent)
            }
            currentState.copy(
                isStreaming = keepStreaming,
                messages = currentState.messages + ChatMessageItem(
                    id = UUID.randomUUID().toString(),
                    role = "assistant",
                    content = assistantContent,
                    cityId = cityId
                ),
                streamingText = "",
                streamingImages = emptyList(),
                streamingFaqs = emptyList()
            )
        }
    }

    private fun maybeSaveTrip(cityId: String?, content: String) {
        val candidate = createTripAssetCandidate(cityId, content) ?: return
        viewModelScope.launch {
            runCatching {
                saveTripAsset(candidate)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    error = throwable.message ?: "Failed to save trip"
                )
            }
        }
    }

    private fun buildAssistantContent(state: ChatUiState): String {
        val sections = mutableListOf<String>()

        if (state.streamingText.isNotBlank()) {
            sections += state.streamingText.trim()
        }
        if (state.streamingImages.isNotEmpty()) {
            sections += buildString {
                append("Images:")
                state.streamingImages.forEach { image ->
                    append("\n- ")
                    append(image.label.ifBlank { image.url })
                }
            }
        }
        if (state.streamingFaqs.isNotEmpty()) {
            sections += buildString {
                append("FAQs:")
                state.streamingFaqs.forEach { faq ->
                    append("\n- ")
                    append(faq.icon.takeIf { it.isNotBlank() }?.plus(" ") ?: "")
                    append(faq.title)
                }
            }
        }

        return sections.joinToString(separator = "\n\n").trim()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                )
                val tripAssetRepository = TripAssetRepositoryImpl(TripRepository(application))
                ChatViewModel(
                    streamChat = StreamChatUseCase(
                        repository = ChatRepositoryImpl(
                            remote = ChatRemoteDataSourceImpl()
                        )
                    ),
                    saveTripAsset = SaveTripAssetUseCase(tripAssetRepository)::invoke,
                    createTripAssetCandidate = { cityId, content ->
                        TripAutoSave.createTripOrNull(cityId, content)?.toTripAsset()
                    }
                )
            }
        }
    }
}
