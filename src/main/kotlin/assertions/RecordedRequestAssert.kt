// SPDX-FileCopyrightText: 2022 Sveriges Television AB
//
// SPDX-License-Identifier: Apache-2.0

package se.svt.oss.flum.assertions

import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions
import se.svt.oss.flum.APPLICATION_JSON
import se.svt.oss.flum.APPLICATION_JSON_UTF8
import se.svt.oss.flum.BodyDeserializer
import se.svt.oss.flum.CONTENT_TYPE
import se.svt.oss.flum.RequestVerification

class RecordedRequestAssert(actual: RecordedRequest, val requestId: String) :
    AbstractObjectAssert<RecordedRequestAssert, RecordedRequest>(actual, RecordedRequestAssert::class.java) {

    companion object {
        fun assertThat(actual: RecordedRequest, requestId: String) =
            RecordedRequestAssert(actual, requestId)
    }

    fun hasBody(body: String) =
        apply {
            Assertions.assertThat(actual.body)
                .`as`(describeAssert("body"))
                .isNotNull()
            Assertions.assertThat(actual.body.readUtf8())
                .`as`(describeAssert("body"))
                .isEqualTo(body)
        }

    fun hasJsonBody(body: String) =
        apply {
            Assertions.assertThat(actual.body)
                .`as`(describeAssert("body"))
                .isNotNull()
            Assertions.assertThat(actual.body.readUtf8())
                .`as`(describeAssert("body"))
                .isEqualTo(body)
            Assertions.assertThat(actual.headers[CONTENT_TYPE])
                .`as`(describeAssert("header 'Content-Type'"))
                .isIn(APPLICATION_JSON, APPLICATION_JSON_UTF8)
        }

    fun hasMethod(method: String) =
        apply {
            Assertions.assertThat(actual.method)
                .`as`(describeAssert("http method"))
                .isEqualTo(method)
        }

    fun hasPath(path: String) =
        apply {
            Assertions.assertThat(actual.path)
                .`as`(describeAssert("path"))
                .isEqualTo(path)
        }

    fun hasQueryParameter(name: String, vararg values: String) =
        apply {
            Assertions.assertThat(actual.requestUrl.queryParameter(name))
                .`as`(describeAssert("query parameter '$name'"))
                .isNotNull()
            Assertions.assertThat(actual.requestUrl.queryParameterValues(name))
                .`as`(describeAssert("query parameter '$name'"))
                .isEqualTo(values.toList())
        }

    fun <T : Any> assertDeserializedBody(
        bodyDeserializer: BodyDeserializer<T>,
        assertFunction: (T) -> Unit
    ) =
        apply {
            assertFunction.invoke(bodyDeserializer.invoke(actual.body.readUtf8()))
        }

    fun matches(requestVerification: RequestVerification) =
        apply {
            requestVerification.invoke(actual)
        }

    fun describeAssert(assertTarget: String) =
        "Verifying $assertTarget of request '$requestId'"
}
