package se.svt.oss.flum

import okhttp3.mockwebserver.RecordedRequest
import se.svt.oss.flum.assertions.RecordedRequestAssert

class RequestVerifyer(private val expectedRequestId: String) {

    private val verifiers = ArrayList<RequestVerification>()

    @Synchronized
    fun hasBody(body: String) =
        apply {
            verifiers.add { request ->
                RecordedRequestAssert.assertThat(request, expectedRequestId)
                    .hasBody(body)
            }
        }

    @Synchronized
    fun hasJsonBody(body: String) =
        apply {
            verifiers.add { request ->
                RecordedRequestAssert.assertThat(request, expectedRequestId)
                    .hasJsonBody(body)
            }
        }

    @Synchronized
    fun hasMethod(method: String) =
        apply {
            verifiers.add { request ->
                RecordedRequestAssert.assertThat(request, expectedRequestId)
                    .hasMethod(method)
            }
        }

    @Synchronized
    fun hasPath(path: String) =
        apply {
            verifiers.add { request ->
                RecordedRequestAssert.assertThat(request, expectedRequestId)
                    .hasPath(path)
            }
        }

    @Synchronized
    fun hasQueryParameter(name: String, vararg values: String) =
        apply {
            verifiers.add { request ->
                RecordedRequestAssert.assertThat(request, expectedRequestId)
                    .hasQueryParameter(name, *values)
            }
        }

    @Synchronized
    fun <T : Any> assertDeserializedBody(
        bodyDeserializer: BodyDeserializer<T>,
        assertFunction: (T) -> Unit
    ): RequestVerifyer =
        apply {
            verifiers.add { request ->
                RecordedRequestAssert.assertThat(request, expectedRequestId)
                    .assertDeserializedBody(bodyDeserializer, assertFunction)
            }
        }

    fun matches(requestVerification: RequestVerification): RequestVerifyer =
        apply {
            verifiers.add { request ->
                RecordedRequestAssert.assertThat(request, expectedRequestId).matches(requestVerification)
            }
        }

    @Synchronized
    internal fun verify(request: RecordedRequest) {
        verifiers.forEach { it.invoke(request) }
    }
}
