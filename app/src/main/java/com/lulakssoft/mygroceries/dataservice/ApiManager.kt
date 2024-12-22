package com.lulakssoft.mygroceries.dataservice

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.json.Json

class ApiManager {
    var jsonHttpClient =
        HttpClient {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }

            /*
            + needs some more work regarding the query
             */
            defaultRequest {
                url.host = "world.openfoodfacts.org"
                url.protocol = URLProtocol.HTTPS
                url.encodedPath = "/api/v3/" + url.encodedPath
                contentType(ContentType.Application.Json)
            }

            HttpResponseValidator {
                getCustomResponseValidator(this)
            }
        }

    var imageHttpClient =
        HttpClient {
            HttpResponseValidator {
                getCustomResponseValidator(this)
            }
        }

    private fun getCustomResponseValidator(responseValidator: HttpCallValidator.Config): HttpCallValidator.Config {
        responseValidator.handleResponseExceptionWithRequest { exception, _ ->
            var exceptionResponseText = exception.message ?: "Unknown Error occurred. Please contact your administrator"

            if (exception is ClientRequestException) {
                // 400 Errors

                val exceptionResponse = exception.response
                exceptionResponseText = exceptionResponse.bodyAsText() + " - " + exceptionResponse.status
            } else if (exception is ServerResponseException) {
                // 500 Errors

                val exceptionResponse = exception.response
                exceptionResponseText = exceptionResponse.bodyAsText()
            }

            throw CancellationException(exceptionResponseText)
        }

        return responseValidator
    }
}
