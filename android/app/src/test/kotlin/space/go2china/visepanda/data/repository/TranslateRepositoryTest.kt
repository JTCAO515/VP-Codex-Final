package space.go2china.visepanda.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import space.go2china.visepanda.data.model.TranslateRequest
import space.go2china.visepanda.data.model.TranslateResponse
import space.go2china.visepanda.data.remote.TranslateApiService

class TranslateRepositoryTest {

    private class MockTranslateApiService(
        private val success: Boolean,
        private val mockResponse: TranslateResponse = TranslateResponse(ok = true, translation = "Hello", pinyin = "")
    ) : TranslateApiService {
        override suspend fun translateText(request: TranslateRequest): TranslateResponse {
            if (success) {
                return mockResponse
            } else {
                throw Exception("Network failure")
            }
        }
    }

    @Test
    fun translateText_success_returnsTranslateResult() {
        val mockApi = MockTranslateApiService(
            success = true,
            mockResponse = TranslateResponse(ok = true, translation = "你好", pinyin = "Nǐ hǎo")
        )
        val repository = LiveTranslateRepository(mockApi)
        
        val result = runBlocking {
            repository.translateText("Hello", "en", "zh")
        }
        
        assertTrue(result.isSuccess)
        val data = result.getOrNull()
        assertEquals("你好", data?.translation)
        assertEquals("Nǐ hǎo", data?.pinyin)
    }

    @Test
    fun translateText_failure_returnsFailureResult() {
        val mockApi = MockTranslateApiService(success = false)
        val repository = LiveTranslateRepository(mockApi)
        
        val result = runBlocking {
            repository.translateText("Hello", "en", "zh")
        }
        
        assertTrue(result.isFailure)
        assertEquals("Network failure", result.exceptionOrNull()?.message)
    }

    @Test
    fun getPhrases_returnsStaticPhrases() {
        val mockApi = MockTranslateApiService(success = true)
        val repository = LiveTranslateRepository(mockApi)
        
        val phrases = runBlocking {
            repository.getPhrases()
        }
        
        assertTrue(phrases.isNotEmpty())
        assertEquals("Greeting", phrases.first().category)
    }
}
