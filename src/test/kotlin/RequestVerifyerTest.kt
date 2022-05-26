// SPDX-FileCopyrightText: 2022 Sveriges Television AB
//
// SPDX-License-Identifier: Apache-2.0

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import se.svt.oss.flum.APPLICATION_JSON
import se.svt.oss.flum.APPLICATION_JSON_UTF8
import se.svt.oss.flum.BodyDeserializer
import se.svt.oss.flum.CONTENT_TYPE
import se.svt.oss.flum.RequestVerification
import se.svt.oss.flum.RequestVerifyer
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.test.fail

internal class RequestVerifyerTest {

    val requestId = UUID.randomUUID().toString()
    val requestVerifyer = RequestVerifyer(requestId)
    val request = mockk<RecordedRequest>()

    @Nested
    inner class HasBody {
        val expectedBody = "TheBody"

        @Test
        fun `Throws assertion error if body does not match`() {
            mockRequestBody("NotTheBody")
            requestVerifyer.hasBody(expectedBody)

            expectAssertionError()
        }

        @Test
        fun `Does not throw assertion error if body matches`() {
            mockRequestBody(expectedBody)
            requestVerifyer.hasBody(expectedBody)

            expectNoAssertionError()
        }
    }

    @Nested
    inner class HasJsonBody {
        val expectedBody = "abcd"

        @Test
        fun `Throws assertion error if body does not match`() {
            mockRequestBody("")
            requestVerifyer.hasJsonBody(expectedBody)

            expectAssertionError()
        }

        @Test
        fun `Throws assertion error if content type is not correct`() {
            mockRequestBody(expectedBody)
            mockRequestHeader(CONTENT_TYPE, "")
            requestVerifyer.hasJsonBody(expectedBody)

            expectAssertionError()
        }

        @Test
        fun `Throws no error if body is correct and content type is application-json`() {
            mockRequestBody(expectedBody)
            mockRequestHeader(CONTENT_TYPE, APPLICATION_JSON)

            requestVerifyer.hasJsonBody(expectedBody)

            expectNoAssertionError()
        }

        @Test
        fun `Throws no error if body is correct and content type is application-json-utf8`() {
            mockRequestBody(expectedBody)
            mockRequestHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8)

            requestVerifyer.hasJsonBody(expectedBody)
        }
    }

    @Nested
    inner class HasMethod {
        @Test
        fun `Throws assertion error if method does not match`() {
            mockRequestMethod("POST")

            requestVerifyer.hasMethod("GET")

            expectAssertionError()
        }

        @Test
        fun `Does not throw assertion error if method matches`() {
            mockRequestMethod("GET")

            requestVerifyer.hasMethod("GET")

            expectNoAssertionError()
        }
    }

    @Nested
    inner class HasPath {
        @Test
        fun `Throws assertion error if path does not match`() {
            mockRequestPath("/a/b/c")

            requestVerifyer.hasPath("/d/e/f")

            expectAssertionError()
        }

        @Test
        fun `Does not throw assertion error if path matches`() {
            mockRequestPath("/a/b/c")

            requestVerifyer.hasPath("/a/b/c")

            expectNoAssertionError()
        }
    }

    @Nested
    inner class HasQueryParameter {
        @Test
        fun `Throws assertion error if query parameter does not match`() {
            val name = "parameter-name"
            val value1 = "parameter-value1"
            val value2 = "parameter-value2"
            mockRequestQueryParameter(name, value1, value2)

            requestVerifyer.hasQueryParameter(name, "Other-value")

            expectAssertionError()
        }

        @Test
        fun `Does not throw assertion error if query parameter matches`() {
            val name = "parameter-name"
            val value1 = "parameter-value1"
            val value2 = "parameter-value2"
            mockRequestQueryParameter(name, value1, value2)

            requestVerifyer.hasQueryParameter(name, value1, value2)

            expectNoAssertionError()
        }

        private fun mockRequestQueryParameter(name: String, vararg values: String) {
            every { request.requestUrl.queryParameter(name) } returns values[0]
            every { request.requestUrl.queryParameterValues(name) } returns values.toMutableList()
        }
    }

    @Nested
    inner class AssertJsonBody {
        @Test
        fun `Deserializes json body and calls supplied verification function`() {
            val mockBody = "bleble"
            mockRequestBody(mockBody)
            val jsonDeserializer = mockk<BodyDeserializer<String>>()
            val deserializedBody = "blabla"
            every { jsonDeserializer.invoke(any()) } returns deserializedBody
            val mockVerification = mockk<(String) -> Unit>()
            every { mockVerification.invoke(any()) } just Runs

            requestVerifyer.assertDeserializedBody(jsonDeserializer, mockVerification)

            requestVerifyer.verify(request)

            verify { jsonDeserializer.invoke(mockBody) }
            verify { mockVerification.invoke(deserializedBody) }
        }
    }

    @Nested
    inner class Matches {
        @Test
        fun `Supplied verification is called`() {
            val verification = mockk<RequestVerification>()
            every { verification.invoke(any()) } just Runs
            requestVerifyer.matches(verification)

            requestVerifyer.verify(request)

            verify { verification.invoke(request) }
        }
    }

    private fun expectAssertionError() {
        assertThatThrownBy { requestVerifyer.verify(request) }
            .isInstanceOf(AssertionError::class.java)
    }

    private fun expectNoAssertionError() {
        try {
            requestVerifyer.verify(request)
        } catch (assertionError: AssertionError) {
            fail("Expected no assertion error but got:\n${assertionError.message}")
        }
    }

    private fun mockRequestBody(content: String) {
        every { request.body } returns buffer(content)
    }

    private fun mockRequestHeader(name: String, value: String) {
        every { request.headers.get(name) } returns value
    }

    private fun mockRequestMethod(method: String) {
        every { request.method } returns method
    }

    private fun mockRequestPath(path: String) {
        every { request.path } returns path
    }

    private fun buffer(content: String) =
        Buffer().apply {
            writeString(content, StandardCharsets.UTF_8)
        }
}
