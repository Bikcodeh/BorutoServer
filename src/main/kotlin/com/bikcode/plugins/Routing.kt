package com.bikcode.plugins

import com.bikcode.routes.getAllHeroes
import com.bikcode.routes.root
import com.bikcode.routes.searchHeroes
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.request.*

fun Application.configureRouting() {

    routing {
        root()
        getAllHeroes()
        searchHeroes()
        static("/images") {
            resources("images")
        }
    }
}
