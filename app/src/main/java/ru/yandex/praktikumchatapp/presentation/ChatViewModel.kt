package ru.yandex.praktikumchatapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.yandex.praktikumchatapp.data.ChatRepository

class ChatViewModel(
    val isWithReplies: Boolean = true
) : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            while (isWithReplies) {
                try {
                    repository.getReplyMessage().collect { response ->
                        _messages.update { currentMessages ->
                            currentMessages + Message.OtherMessage(response)
                        }
                    }
                } catch (e: Exception) {
                    _messages.update { currentMessages ->
                        currentMessages + Message.OtherMessage("Ошибка соединения")
                    }
                }
            }
        }
    }

    fun sendMyMessage(messageText: String) {
        _messages.update { currentMessages ->
            currentMessages + Message.MyMessage(messageText)
        }
    }
}