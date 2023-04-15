package router

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.io.File

fun Application.configureRouting() {
    routing {
        static("/") {
            resources("files")
        }

        static("/s") {
            staticRootFolder = File("static")
            files(".")
        }

        get("/") { responseMainPage() }



//        route("schedulers") {
//        }
//
//        route("admin") {
//        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.responseMainPage() {
    call.respondRedirect("index.html")
}