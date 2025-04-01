package ru.yandex.praktikumchatapp.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen

class ChatRepository(
    private val api: ChatApi = ChatApi()
) {
    companion object {
        private const val INITIAL_DELAY = 1000L
        private const val DELAY_FACTOR = 2L
        private const val MAX_RETRIES = 3
    }

    fun getReplyMessage(): Flow<String> {
        return api.getReply()
            .retryWhen { cause, attempt ->
                if (attempt < MAX_RETRIES) {
                    val delayTime = INITIAL_DELAY * (DELAY_FACTOR).pow(attempt.toInt() - 1)
                    delay(delayTime)
                    true
                } else {
                    false
                }
            }
    }
//
    private fun Long.pow(exp: Int): Long {
        return when {
            exp < 0 -> 0L
            exp == 0 -> 1L
            else -> this * pow(exp - 1)
        }
    }
}