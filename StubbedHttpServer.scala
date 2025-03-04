package org.encalmo.lambda

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

import java.net.InetSocketAddress
import scala.io.AnsiColor.*
import java.net.URI

/** Simple http server for testing with stubbed http responses. */
class StubbedHttpServer {

  private val socket = new InetSocketAddress(0)
  private val server = HttpServer.create(socket, 0)
  server.start()
  val uri = new URI(s"http://localhost:${server.getAddress().getPort()}")
  println(
    s"${CYAN}[StubbedHttpServer] Started server at ${YELLOW}${uri.toString}${RESET}"
  )

  def close() = {
    server.stop(0)
    println(
      s"${CYAN}[StubbedHttpServer] Closing server at ${YELLOW}${uri.toString}${RESET}"
    )
  }

  def stubResponseOnce(
      requestMethod: String,
      requestPath: String,
      responseStatus: Int,
      responseHeaders: Map[String, String],
      responseBody: String
  ) = {
    val bytes = responseBody.getBytes()
    server.createContext(
      requestPath,
      (exchange: HttpExchange) =>
        try {
          println(
            s"${CYAN}[StubbedHttpServer] Handling HTTP request ${YELLOW}${exchange.getRequestURI()}${RESET}"
          )
          if (exchange.getRequestMethod() == requestMethod) then {
            responseHeaders.foreach((k, v) => exchange.getResponseHeaders().add(k, v))
            exchange.sendResponseHeaders(responseStatus, bytes.length)
            val os = exchange.getResponseBody()
            os.write(bytes)
            os.close()
            exchange.close()
          } else {
            exchange.sendResponseHeaders(404, -1)
          }
        } catch {
          case e =>
            exchange.sendResponseHeaders(500, -1)
        }
        server.removeContext(requestPath)
    )
  }

  def stubResponseAlways(
      requestMethod: String,
      requestPath: String,
      responseStatus: Int,
      responseHeaders: Map[String, String],
      responseBody: String
  ) = {
    server.createContext(
      requestPath,
      (exchange: HttpExchange) =>
        try {
          println(
            s"${CYAN}[StubbedHttpServer] Handling HTTP request ${YELLOW}${exchange.getRequestURI()}${RESET}"
          )
          if (exchange.getRequestMethod() == requestMethod) then {
            responseHeaders.foreach((k, v) => exchange.getResponseHeaders().add(k, v))
            exchange.sendResponseHeaders(responseStatus, responseBody.length())
            val os = exchange.getResponseBody()
            os.write(responseBody.getBytes())
            os.close()
          } else {
            exchange.sendResponseHeaders(404, -1)
          }
        } catch {
          case e =>
            exchange.sendResponseHeaders(500, -1)
        }
    )
  }

}
