// SPDX-FileCopyrightText: 2022 Sveriges Television AB
//
// SPDX-License-Identifier: Apache-2.0

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import se.svt.oss.flum.RequestAction
import se.svt.oss.flum.RequestMatcher
import se.svt.oss.flum.ResponseBuilder
import se.svt.oss.flum.ResponseProducer

internal class RequestMatcherTest {

    val REQUEST_PATH = "/a/b/c"

    val REQUEST_METHOD = "GET"

    val responseBuilder = mockk<ResponseBuilder>()

    lateinit var requestMatcher: RequestMatcher

    val request = mockk<RecordedRequest>()

    @BeforeEach
    fun setUp() {
        requestMatcher = RequestMatcher(responseBuilder)
        every { request.requestUrl.encodedPath() } returns REQUEST_PATH
        every { request.method } returns REQUEST_METHOD
    }

    @Test
    fun `If no criteria defined, matches all requests`() {
        assertRequestIsMatched()
    }

    @Test
    fun `Matches if all criteria match`() {
        requestMatcher
            .withMethod(REQUEST_METHOD)
            .toPath(REQUEST_PATH)

        assertRequestIsMatched()
    }

    @Test
    fun `does not match if one criteria fails`() {
        requestMatcher
            .withMethod(REQUEST_METHOD)
            .toPath(REQUEST_PATH)
            .matching { false }

        assertRequestIsNotMatched()
    }

    @Nested
    inner class ToPath {
        @Test
        fun `matches given path`() {
            requestMatcher.toPath(REQUEST_PATH)

            assertRequestIsMatched()
        }

        @Test
        fun `does not match request with other path`() {
            requestMatcher.toPath("/apa/bepa")

            assertRequestIsNotMatched()
        }
    }

    @Nested
    inner class WithMethod {
        @Test
        fun `matches request with given method`() {
            requestMatcher.withMethod(REQUEST_METHOD)

            assertRequestIsMatched()
        }

        @Test
        fun `does not match request with other method`() {
            requestMatcher.withMethod("POSY")

            assertRequestIsNotMatched()
        }
    }

    @Nested
    inner class WithQueryParameter {
        @Test
        fun `matches request if query parameter matches`() {
            val name = "query-param"
            val value1 = "apa"
            val value2 = "bepa"
            every { request.requestUrl.queryParameterValues(name) } returns listOf(value1, value2)
            requestMatcher.withQueryParameter(name, value1, value2)

            assertRequestIsMatched()
        }

        @Test
        fun `does not match request if query parameter matches`() {
            val name = "query-param"
            every { request.requestUrl.queryParameterValues(name) } returns listOf("apa")
            requestMatcher.withQueryParameter(name, "bepa")

            assertRequestIsNotMatched()
        }
    }

    @Nested
    inner class Matching {
        @Test
        fun `matching lambda is called and result returned`() {
            requestMatcher.matching { false }

            assertRequestIsNotMatched()
        }
    }

    @Nested
    inner class ThenRespond {
        @Test
        fun `then respond without argument returns ResponseBuilder`() {
            assertThat(requestMatcher.thenRespond())
                .isEqualTo(responseBuilder)
        }

        @Test
        fun `then respond with argument sets response producer and returns ResponseBuilder`() {
            val responseProducer = mockk<ResponseProducer>()
            every { responseBuilder.responseProducer = any() } just Runs
            requestMatcher.thenRespond(responseProducer)

            verify { responseBuilder.responseProducer = responseProducer }
        }
    }

    @Nested
    inner class ThenDo {
        @Test
        fun `Adds action to responseBuilder`() {
            val requestAction = mockk<RequestAction>()
            every { responseBuilder.actions.add(any()) } returns true

            requestMatcher.thenDo(requestAction)

            verify { responseBuilder.actions.add(requestAction) }
        }
    }

    private fun assertRequestIsMatched() {
        assertThat(requestMatcher.match(request))
            .isTrue()
    }

    private fun assertRequestIsNotMatched() {
        assertThat(requestMatcher.match(request))
            .isFalse()
    }
}
