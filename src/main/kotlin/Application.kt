package moteo_back

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureSerialization()  // Toujours avant configureRouting pour que la sérialisation soit active pour les routes
    configureSecurity()       // Configure ta sécurité (authentification etc.) si tu as ce module
    configureRouting()        // Ensuite tes routes
}
