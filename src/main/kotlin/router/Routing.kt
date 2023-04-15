package router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import model.LoginRequest
import model.Orphanage
import model.Parents
import service.AccountService
import utils.JsonSupporter
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

        post("/login") { handleLogin() }

        route("register") {
            post("/orphanage") { handleOrphanageRegister() }
            post("/parent") { handleParentRegister() }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.responseMainPage() {
    call.respondRedirect("index.html")
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleLogin() {
    try {
        val request = JsonSupporter.fromJson<LoginRequest>(call.receive())

        val msg2obj = AccountService.getAccount(request.email, request.password)

        if(msg2obj.second == null) {
            call.respond(HttpStatusCode.NotFound, msg2obj.first)
            return
        }

        call.respond(msg2obj.second!!)
    }
    catch (e: Exception) {
        call.respond(HttpStatusCode.BadRequest, e.toString())
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleOrphanageRegister() {
    try {
        val entity = JsonSupporter.fromJson<Orphanage>(call.receive())

        val msg = AccountService.addOrphanage(entity)

        if(msg.isNotEmpty()) {
            call.respond(HttpStatusCode.Created, msg)
            return
        }

        call.respond("Success")
    }
    catch (e: Exception) {
        call.respond(HttpStatusCode.BadRequest, e.toString())
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleParentRegister() {
    try {
        val entity = JsonSupporter.fromJson<Parents>(call.receive())

        val msg = AccountService.addParents(entity)

        if(msg.isNotEmpty()) {
            call.respond(HttpStatusCode.Created, msg)
            return
        }

        call.respond("Success")
    }
    catch (e: Exception) {
        call.respond(HttpStatusCode.BadRequest, e.toString())
    }
}