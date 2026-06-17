package space.jtcao.visepanda.data.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatSseClientTest {

    @Test
    fun `message event with token payload should become token event`() {
        val client = ChatSseClient()
        val event = client.parseForTest(
            eventType = "message",
            data = """{"token":"Hello China"}"""
        )

        assertTrue(event is ChatSseEvent.Token)
        assertEquals("Hello China", (event as ChatSseEvent.Token).text)
    }
}
