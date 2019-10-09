
import me.alexpanov.net.FreePortFinder
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import se.svt.oss.flum.Flum

class FlumTest {

    private val okHttpClient = OkHttpClient()

    val port = FreePortFinder.findFreeLocalPort()

    lateinit var flum: Flum

    @AfterEach
    fun tearDown() {
        try {
            flum.verify()
        } finally {
            flum.shutdown()
        }
    }

    @Nested
    inner class ExpectRequestsInOrder {

        @BeforeEach
        fun before() {
            flum = Flum(port).apply {
                start()
            }
        }

        @Test
        fun requestsWithVerification() {
            flum.expectRequest("test request 1")
                    .toPath("/myService")
                    .withMethod("GET")
                    .thenRespond()
                    .withStatus(200)
                    .withBody("SUCCESS")
                    .afterwardsVerifyRequest()
                    .hasQueryParameter("myParam", "myValue")

            flum.expectRequest("test request 2")
                    .toPath("/myService")
                    .withMethod("POST")
                    .thenRespond()
                    .withStatus(202)
                    .withBody("ALSO SUCCESS")
                    .afterwardsVerifyRequest()
                    .hasBody("Hello World!")

            callSomeCodeThatIsSuppoedToExecuteTheExpectedRequests()
        }

        @Test
        fun requestsWithVerificationsLast() {
            flum.expectRequest("test request 1")
                    .toPath("/myService")
                    .withMethod("GET")
                    .thenRespond()
                    .withStatus(200)
                    .withBody("SUCCESS")

            flum.expectRequest("test request 2")
                    .toPath("/myService")
                    .withMethod("POST")
                    .thenRespond()
                    .withStatus(202)
                    .withBody("ALSO SUCCESS")

            callSomeCodeThatIsSuppoedToExecuteTheExpectedRequests()

            flum.assertThatRecordedRequest("test request 1")
                    .hasQueryParameter("myParam", "myValue")

            flum.assertThatRecordedRequest("test request 2")
                    .hasBody("Hello World!")
        }
    }

    @Nested
    inner class ExpectRequestsIgnoreOrder {

        @BeforeEach
        fun before() {
            flum = Flum(port, false).apply {
                start()
            }
        }

        @Test
        fun requestsWithVerification() {

            flum.expectRequest("test request 2")
                    .toPath("/myService")
                    .withMethod("POST")
                    .thenRespond()
                    .withStatus(202)
                    .withBody("ALSO SUCCESS")
                    .afterwardsVerifyRequest()
                    .hasBody("Hello World!")

            flum.expectRequest("test request 1")
                    .toPath("/myService")
                    .withMethod("GET")
                    .thenRespond()
                    .withStatus(200)
                    .withBody("SUCCESS")
                    .afterwardsVerifyRequest()
                    .hasQueryParameter("myParam", "myValue")

            callSomeCodeThatIsSuppoedToExecuteTheExpectedRequests()
        }

        @Test
        fun requestsWithVerificationsLast() {

            flum.expectRequest("test request 2")
                    .toPath("/myService")
                    .withMethod("POST")
                    .thenRespond()
                    .withStatus(202)
                    .withBody("ALSO SUCCESS")

            flum.expectRequest("test request 1")
                    .toPath("/myService")
                    .withMethod("GET")
                    .thenRespond()
                    .withStatus(200)
                    .withBody("SUCCESS")

            callSomeCodeThatIsSuppoedToExecuteTheExpectedRequests()

            flum.assertThatRecordedRequest("test request 1")
                    .hasQueryParameter("myParam", "myValue")

            flum.assertThatRecordedRequest("test request 2")
                    .hasBody("Hello World!")
        }
    }

    private fun callSomeCodeThatIsSuppoedToExecuteTheExpectedRequests() {
        val response1 = doRequest("http://localhost:$port/myService?myParam=myValue")

        assertThat(response1.code())
            .isEqualTo(200)
        assertThat(response1.body()?.string())
            .isEqualTo("SUCCESS")

        val response2 = doRequest(
            "http://localhost:$port/myService", "POST",
            RequestBody.create(MediaType.parse("plain/text"), "Hello World!")
        )

        assertThat(response2.code())
            .isEqualTo(202)
        assertThat(response2.body()?.string())
            .isEqualTo("ALSO SUCCESS")
    }

    fun doRequest(url: String, method: String = "GET", body: RequestBody? = null) =
            okHttpClient.newCall(
                    Request.Builder()
                            .method(method, body)
                            .url(url)
                            .build()
            )
                    .execute()
}