package space.jtcao.visepanda.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import space.jtcao.visepanda.data.model.ChatEvent

class SseClientTest {

    private val client = SseClient()

    @Test
    fun `message event with token payload should become token chat event`() {
        val event = parseEvent("message", """{"token":"Hello China"}""")

        assertTrue(event is ChatEvent.Token)
        assertEquals("Hello China", (event as ChatEvent.Token).text)
    }

    @Test
    fun `message event with split payload should become split chat event`() {
        val event = parseEvent("message", """{"split":true}""")

        assertTrue(event is ChatEvent.Split)
    }

    @Test
    fun `message event with image payload should become image chat event`() {
        val event = parseEvent(
            "message",
            """{"image":{"key":"beijing","url":"/static/img/city-beijing.jpg","label":"Beijing"}}"""
        )

        assertTrue(event is ChatEvent.Image)
        event as ChatEvent.Image
        assertEquals("beijing", event.key)
        assertEquals("/static/img/city-beijing.jpg", event.url)
        assertEquals("Beijing", event.label)
    }

    @Test
    fun `message event with faq payload should become faq chat event`() {
        val event = parseEvent(
            "message",
            """{"faq":{"id":"visa","title":"Visa Guide","icon":"🛂"}}"""
        )

        assertTrue(event is ChatEvent.Faq)
        event as ChatEvent.Faq
        assertEquals("visa", event.id)
        assertEquals("Visa Guide", event.title)
        assertEquals("🛂", event.icon)
    }

    @Test
    fun `message event with done payload should become done chat event`() {
        val event = parseEvent("message", """{"done":true}""")

        assertTrue(event is ChatEvent.Done)
    }

    @Test
    fun `message event with unknown payload should return null`() {
        val event = parseEvent("message", """{"noop":true}""")

        assertNull(event)
    }

    private fun parseEvent(eventType: String, data: String): ChatEvent? {
        val method = client.javaClass.getDeclaredMethod("parseEvent", String::class.java, String::class.java)
        method.isAccessible = true
        return method.invoke(client, eventType, data) as ChatEvent?
    }
}
