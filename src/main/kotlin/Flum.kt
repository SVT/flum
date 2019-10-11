package se.svt.oss.flum

import me.alexpanov.net.FreePortFinder
import mu.KotlinLogging
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import se.svt.oss.flum.assertions.RecordedRequestAssert
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue

const val INTERNAL_SERVER_ERROR = 500

class Flum(
    val port: Int = FreePortFinder.findFreeLocalPort(),
    val matchRequestOrder: Boolean = true
) {

    private val log = KotlinLogging.logger {}

    val server = MockWebServer()

    private val expectedRequests = ConcurrentHashMap<String, ExpectedRequest>()

    private val unmatchedExpectedRequests = ConcurrentSkipListMap<Int, ExpectedRequest>()

    private val unmatchedRequests = CopyOnWriteArrayList<RecordedRequest>()

    private val expectedOrder = LinkedBlockingQueue<String>()

    private val receivedRequests = CopyOnWriteArrayList<RecordedRequest>()

    init {
        setDispatcher()
    }

    fun start() {
        server.start(port)
    }

    fun shutdown() {
        server.shutdown()
    }

    private fun setDispatcher() {
        server.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                    this@Flum.dispatch(request)
        })
    }

    private fun nextExpectedRequest() =
            expectedOrder.poll()?.let {
                expectedRequests[it]
            }

    private fun nextMatchingRequest(request: RecordedRequest) =
            unmatchedExpectedRequests.keys.asSequence()
                    .filter { unmatchedExpectedRequests[it]?.requestMatcher?.match(request) == true }
                    .map { unmatchedExpectedRequests.remove(it) }
                    .filterNotNull()
                    .firstOrNull()

    fun dispatch(request: RecordedRequest): MockResponse {
        log.info { "Dispatching request $request" }
        return try {
            receivedRequests.add(request)
            val expectedRequest =
                    if (matchRequestOrder) nextExpectedRequest()
                    else nextMatchingRequest(request)

            val response =
                    if (expectedRequest == null || !expectedRequest.requestMatcher.match(request)) {
                        unmatchedRequests.add(request)
                        MockResponse().setResponseCode(404)
                    } else {
                        expectedRequest.matchedRequest = request
                        expectedRequest.responseBuilder.response(request)
                    }
            log.info { "Sending response $response" }
            response
        } catch (throwable: Throwable) {
            log.error("Error dispatching request", throwable)
            MockResponse().setResponseCode(INTERNAL_SERVER_ERROR)
                    .setBody(throwable.toString())
        }
    }

    fun expectRequest(id: String = randomUuid()): RequestMatcher {
        require(expectedRequests[id] == null) { "Duplicate requestId: $id" }
        val expectedRequest = ExpectedRequest(id)
        expectedRequests[id] = expectedRequest
        expectedOrder.add(id)
        unmatchedExpectedRequests[unmatchedExpectedRequests.size] = expectedRequest
        return expectedRequest.requestMatcher
    }

    fun expectGet(id: String = randomUuid()) =
            expectRequest(id)
                    .withMethod("GET")

    fun expectPost(id: String = randomUuid()) =
            expectRequest(id)
                    .withMethod("POST")

    fun expectPut(id: String = randomUuid()) =
            expectRequest(id)
                    .withMethod("PUT")

    fun expectPatch(id: String = randomUuid()) =
            expectRequest(id)
                    .withMethod("PATCH")

    fun expectDelete(id: String = randomUuid()) =
            expectRequest(id)
                    .withMethod("DELETE")

    fun verify() {
        val assertions = SoftAssertions()
        assertions.assertThat(receivedRequests.size)
                .`as`("Verifying received requests count")
                .isEqualTo(expectedRequests.size)

        expectedRequests.values.forEach {
            try {
                if (it.matchedRequest != null) it.requestVerifyer.verify(it.matchedRequest!!)
                else assertions.fail("Expected request '${it.id}' was not received")
            } catch (error: AssertionError) {
                assertions.fail(error.message)
            }
        }

        unmatchedRequests.forEach {
            assertions.fail("Unexpected request: $it")
        }

        assertions.assertAll()
    }

    fun assertThatRecordedRequest(requestId: String): RecordedRequestAssert {
        val request = expectedRequests[requestId]
        assertThat(request)
                .`as`("Request '$requestId'")
                .isNotNull
        assertThat(request!!.matchedRequest)
                .`as`("Checking matched request for '$requestId'")
                .isNotNull
        return RecordedRequestAssert(request.matchedRequest!!, requestId)
    }
}

private fun randomUuid() = UUID.randomUUID().toString()