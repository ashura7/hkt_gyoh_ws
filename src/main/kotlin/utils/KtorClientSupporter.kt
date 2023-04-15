//package utils
//
//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.request.*
//import io.ktor.client.request.forms.*
//import io.ktor.client.statement.*
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.*
//import org.apache.logging.log4j.LogManager
//
//object KtorClientSupporter {
//    private val logger = LogManager.getLogger()
//
//    private val client = HttpClient(CIO) {
//        install(HttpTimeout)
//        install(ContentNegotiation) {
//            json()
//        }
//    }
//
//    suspend fun get(url: String, params: Map<String, String>): HttpResponse {
//        return client.get(url) {
//            url {
//                params.forEach { (k, v) ->
//                    parameters.append(k, v)
//                }
//            }
//        }
//    }
//
//    suspend fun post(url: String, body: Any): HttpResponse {
//        return client.post(url) {
//            contentType(ContentType.Application.Json)
//            setBody(body)
//        }
//    }
//
//    suspend fun form(url: String, key2value: Map<String, String>): HttpResponse {
//        val params = Parameters.build {
//            key2value.forEach {(k, v) ->
//                append(k, v)
//            }
//        }
//
//        return client.post(url) {
//            header("Accept", "*/*")
//            timeout {
//                requestTimeoutMillis = 60000
//            }
//            setBody(FormDataContent(params))
//        }
//    }
//}