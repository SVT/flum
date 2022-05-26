// SPDX-FileCopyrightText: 2022 Sveriges Television AB
//
// SPDX-License-Identifier: Apache-2.0

package se.svt.oss.flum

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.UUID

typealias RequestPredicate = (RecordedRequest) -> Boolean
typealias ResponseProducer = (RecordedRequest) -> MockResponse
typealias ResponseModifier = (MockResponse) -> MockResponse
typealias RequestVerification = (RecordedRequest) -> Unit
typealias RequestAction = (RecordedRequest) -> Unit
typealias BodyDeserializer<T> = (String) -> T

const val APPLICATION_JSON = "application/json"
const val APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8"

const val CONTENT_TYPE = "Content-Type"

internal class ExpectedRequest(val id: String = UUID.randomUUID().toString()) {

    val requestVerifyer = RequestVerifyer(id)

    val responseBuilder = ResponseBuilder(requestVerifyer)

    val requestMatcher = RequestMatcher(responseBuilder)

    var matchedRequest: RecordedRequest? = null
}
