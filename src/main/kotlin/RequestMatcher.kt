package se.svt.oss.flum

import okhttp3.mockwebserver.RecordedRequest

class RequestMatcher(private val responseBuilder: ResponseBuilder) {

    private val matchers = ArrayList<RequestPredicate>()

    @Synchronized
    fun toPath(expectedPath: String) =
        apply {
            matchers.add { request ->
                request.requestUrl.encodedPath() == expectedPath
            }
        }

    @Synchronized
    fun withMethod(method: String) =
        apply {
            matchers.add { request -> request.method.equals(method, true) }
        }

    @Synchronized
    fun withQueryParameter(name: String, vararg values: String) =
        apply {
            matchers.add {
                    request ->
                request.requestUrl.queryParameterValues(name) == values.toList()
            }
        }

    @Synchronized
    fun matching(predicate: RequestPredicate) =
        apply { matchers.add(predicate) }

    @Synchronized
    fun thenRespond(responseProducer: ResponseProducer) =
        responseBuilder.apply {
            this.responseProducer = responseProducer
        }

    @Synchronized
    fun thenRespond(): ResponseBuilder =
        responseBuilder

    @Synchronized
    fun thenDo(action: RequestAction): RequestMatcher {
        responseBuilder.actions.add(action)
        return this
    }

    @Synchronized
    internal fun match(request: RecordedRequest) =
        matchers.all { it.invoke(request) }
}
