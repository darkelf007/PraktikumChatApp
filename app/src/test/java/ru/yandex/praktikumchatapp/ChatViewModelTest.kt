import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.yandex.praktikumchatapp.presentation.ChatViewModel
import ru.yandex.praktikumchatapp.presentation.Message
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ChatViewModelTest {

    private var testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChatViewModel(isWithReplies = false)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `send message should update messages with MyMessage`() = runTest {
        val testMessage = "TestMessage"

        viewModel.messages.test {
            assertEquals(emptyList(), awaitItem())
            viewModel.sendMyMessage(testMessage)
            val updatedMessages = awaitItem()
            assertEquals(1, updatedMessages.size)
            val message = updatedMessages.first()
            assertEquals(Message.MyMessage(testMessage), message)
        }
    }


    @Test
    fun testReceiveMessage_concurrentMessages() = runTest {
        val messagesToSend = (1..100).map { Message.MyMessage("Message $it") }
        val collectJob = launch {
            viewModel.messages.test {
                awaitItem()
                repeat(100) {
                    awaitItem()
                }
            }
        }
        messagesToSend.forEach { message ->
            launch {
                viewModel.sendMyMessage(message.text)
            }
        }
        advanceUntilIdle()
        assertEquals(100, viewModel.messages.value.size)
        collectJob.cancel()
    }
}