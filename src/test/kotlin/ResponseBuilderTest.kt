import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import se.svt.oss.flum.RequestAction
import se.svt.oss.flum.RequestVerifyer
import se.svt.oss.flum.ResponseBuilder

internal class ResponseBuilderTest {

    val requestVerifyer = mockk<RequestVerifyer>()

    val request = mockk<RecordedRequest>()

    val responseBuilder = ResponseBuilder(requestVerifyer)

    @Test
    fun `If no response defined, returns default response`() {
        val response = responseBuilder.response(request)

        assertThat(response.statusCode)
            .`as`("status code")
            .isEqualTo(200)
        assertThat(response.headers.size())
            .`as`("Should have one header only")
            .isEqualTo(1)
        assertThat(response.headers.get("Content-Length"))
            .`as`("Sould have content length header set to 0")
            .isEqualTo("0")
        assertThat(response.body)
            .`as`("Should have no body")
            .isNull()
    }

    @Test
    fun `Request actions are executed when response is requested`() {
        val requestAction = mockk<RequestAction>()
        every { requestAction.invoke(any()) } just Runs
        responseBuilder.actions.add(requestAction)

        responseBuilder.response(request)

        verify { requestAction.invoke(request) }
    }

    @Nested
    inner class WithStatus {
        @Test
        fun `Returns given status`() {
            val status = 302
            responseBuilder.withStatus(status)

            assertThat(responseBuilder.response(request).statusCode)
                .isEqualTo(status)
        }
    }

    @Nested
    inner class WithJsonBody {
        @Test
        fun `Sets content type header to json and returns the given body`() {
            val body = "BLABLA"
            responseBuilder.withJsonBody(body)

            val response = responseBuilder.response(request)

            assertThat(response.body.readUtf8())
                .isEqualTo(body)
            assertThat(response.headers.get("Content-Type"))
                .isEqualTo("application/json")
        }
    }

    @Nested
    inner class WithBody {
        @Test
        fun `Sets  the given body`() {
            val body = "BLABLA"
            responseBuilder.withBody(body)

            val response = responseBuilder.response(request)

            assertThat(response.body.readUtf8())
                .isEqualTo(body)
        }
    }

    @Nested
    inner class WithHeader {
        @Test
        fun `Sets the given header to the given value`() {
            val header = "x-some-header"
            val value = "thevalue"
            responseBuilder.withHeader(header, value)

            val response = responseBuilder.response(request)

            assertThat(response.headers.get(header))
                .isEqualTo(value)
        }
    }

    @Nested
    inner class AfterwardsVerifyRequest {
        @Test
        fun `returns request verifyer`() {
            assertThat(responseBuilder.afterwardsVerifyRequest())
                .isEqualTo(requestVerifyer)
        }
    }

    private val MockResponse.statusCode: Int
        get() = this.status.split(" ")[1].let {
            Integer.parseInt(it)
        }
}