package space.go2china.visepanda.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import space.go2china.visepanda.data.model.TranslateRequest
import space.go2china.visepanda.data.model.TranslateResponse
import space.go2china.visepanda.data.model.OcrRequest
import space.go2china.visepanda.data.model.OcrResponse
import space.go2china.visepanda.data.model.SttRequest
import space.go2china.visepanda.data.model.SttResponse
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

        override suspend fun translateOcr(request: OcrRequest): OcrResponse {
            if (success) {
                return OcrResponse(ok = true, text = "Extracted OCR text")
            } else {
                throw Exception("Network failure")
            }
        }

        override suspend fun translateStt(request: SttRequest): SttResponse {
            if (success) {
                return SttResponse(ok = true, text = "Extracted STT text")
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
    fun translateOcr_success_returnsText() {
        val mockApi = MockTranslateApiService(success = true)
        val repository = LiveTranslateRepository(mockApi)
        
        val result = runBlocking {
            repository.translateOcr("dummy_base64")
        }
        
        assertTrue(result.isSuccess)
        assertEquals("Extracted OCR text", result.getOrNull())
    }

    @Test
    fun translateStt_success_returnsText() {
        val mockApi = MockTranslateApiService(success = true)
        val repository = LiveTranslateRepository(mockApi)
        
        val result = runBlocking {
            repository.translateStt("dummy_base64")
        }
        
        assertTrue(result.isSuccess)
        assertEquals("Extracted STT text", result.getOrNull())
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
