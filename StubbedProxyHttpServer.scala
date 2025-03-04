package org.encalmo.lambda

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable.Buffer
import scala.io.AnsiColor.*
import scala.jdk.CollectionConverters.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.io.InputStream
import java.net.http.HttpResponse
import java.{util => ju}
import javax.net.ssl.SSLSession
import java.net.http.HttpHeaders
import java.net.http.HttpClient.Version
import java.util.Optional
import java.util.regex.Pattern

/** Proxy http server for testing with stubbed http responses. */
class StubbedProxyHttpServer(proxyTargetUri: URI, debug: Boolean = false) {

  import StubbedProxyHttpServer.*

  val proxyTargetUriPath: Array[String] = proxyTargetUri.getPath().split("/")
  val proxyTargetUriPathSize: Int = proxyTargetUriPath.size

  private val restrictedHeaders =
    Set(
      "connection",
      "host",
      "upgrade",
      "content-length",
      "http2-settings",
      "accept-encoding",
      "content-encoding",
      "content-type",
      "user-agent"
    )

  private val socket = new InetSocketAddress(0)
  private val server = HttpServer.create(socket, 0)

  server.start()

  val uri = new URI(s"http://localhost:${server.getAddress().getPort()}")

  println(
    s"${CYAN}Started stubbed http server at ${YELLOW}${uri.toString}${CYAN} proxying request to ${YELLOW}${proxyTargetUri.toString}${CYAN} ${RESET}"
  )

  private val stubs: AtomicReference[Buffer[Stub]] =
    AtomicReference(Buffer.empty)

  private lazy val httpClient = HttpClient
    .newBuilder()
    .connectTimeout(java.time.Duration.ofSeconds(60))
    .build()

  server.createContext(
    "/",
    (exchange: HttpExchange) =>
      try {
        val uri = exchange.getRequestURI()
        if (debug) then println(s"base URI is $proxyTargetUri")
        if (debug) then println(s"request URI is $uri")
        val targetUriPath =
          proxyTargetUriPath ++ uri.getPath().split("/")
        val targetUriQuery =
          Option(uri.getRawQuery()).map(q => s"?$q").getOrElse("")
        val targetUri: URI =
          proxyTargetUri.resolve(targetUriPath.mkString("/") + targetUriQuery)
        if (debug) then println(s"target URI is $targetUri")

        val request: HttpRequest = {
          val r = HttpRequest
            .newBuilder(targetUri)
            .method(exchange.getRequestMethod(), BodyPublishers.ofInputStream(() => exchange.getRequestBody()))
          val r2 = exchange
            .getRequestHeaders()
            .entrySet()
            .asScala
            .foldLeft(r)((r, e) =>
              e.getValue().asScala.foldLeft(r) { (r, v) =>
                if (restrictedHeaders.contains(e.getKey().toLowerCase()))
                then r
                else r.setHeader(e.getKey(), v)
              }
            )
          r2.build()
        }

        val path = uri.getPath()
        val query = uri.getQuery()

        val response: HttpResponse[Array[Byte]] = stubs
          .get()
          .foldLeft[Option[HttpResponse[Array[Byte]]]](None)((response, stub) =>
            response.orElse(
              stub
                .canHandle(path, query, request, debug)
                .map { response =>
                  println(s"${CYAN}>>> Serving stubbed HTTP response ${stub.id} <<<${RESET}")
                  response
                }
            )
          )
          .getOrElse(
            try {
              httpClient.send(request, BodyHandlers.ofByteArray())
            } catch {
              case s: java.io.IOException => httpResponse404(request)
            }
          )

        if (debug) then println(s"$CYAN$response$RESET")

        exchange.getResponseHeaders().putAll(response.headers.map())
        val body = response.body
        exchange.sendResponseHeaders(
          response.statusCode(),
          if body.length > 0 then body.length else -1
        )
        if (body.length > 0) {
          val os = exchange.getResponseBody()
          os.write(body)
          os.close()
        }
      } catch {
        case e =>
          println(s"${RED_B}${WHITE}$e${RESET}")
          if (debug) then e.printStackTrace()
          exchange.sendResponseHeaders(500, -1)
      }
  )

  def close() = {
    httpClient.close()
    server.stop(0)
    println(
      s"${CYAN}Closing stubbed http server at ${YELLOW}${uri.toString}${RESET}"
    )
  }

  def clearStubs(): Unit =
    stubs.set(Buffer.empty[Stub])

  def stubResponseOnce(
      requestMethod: String,
      requestPath: String,
      responseStatus: Int,
      responseHeaders: Map[String, String],
      responseBody: String,
      requestQuery: String = null,
      regex: Boolean = false
  ) = {
    stubs.get.append(
      Stub(
        requestMethod,
        requestPath,
        requestQuery,
        responseStatus,
        responseHeaders,
        responseBody,
        once = true,
        regex = regex
      )
    )
  }

  def stubResponseAlways(
      requestMethod: String,
      requestPath: String,
      responseStatus: Int,
      responseHeaders: Map[String, String],
      responseBody: String,
      requestQuery: String = null,
      regex: Boolean = false
  ) = {
    stubs.get.append(
      Stub(
        requestMethod,
        requestPath,
        requestQuery,
        responseStatus,
        responseHeaders,
        responseBody,
        once = false,
        regex = regex
      )
    )
  }

}

object StubbedProxyHttpServer {

  trait Stub {

    def id: String

    def canHandle(
        path: String,
        query: String,
        request: HttpRequest,
        debug: Boolean
    ): Option[HttpResponse[Array[Byte]]]
  }

  object Stub {
    def apply(
        requestMethod: String,
        requestPath: String,
        requestQuery: String,
        responseStatus: Int,
        responseHeaders: Map[String, String],
        responseBody: String,
        once: Boolean,
        regex: Boolean = false
    ): Stub =
      new Stub() {

        val id =
          s"${if (once) then "once" else "always"} $requestMethod $requestPath${Option(requestQuery).map(q => s"?$q").getOrElse("")}"

        @volatile
        var available: Boolean = true

        override def canHandle(
            path: String,
            query: String,
            httpRequest: HttpRequest,
            debug: Boolean
        ): Option[HttpResponse[Array[Byte]]] =
          if (debug) then
            println(
              s"available=$available matching path=$path query=$query against path=$requestPath query=$requestQuery"
            )
            println((regex && path.matches(requestPath)))
          if (
            available
            && httpRequest.method.toUpperCase() == requestMethod
              .toUpperCase()
            && (path == requestPath || (regex && path.matches(requestPath)))
            && Option(requestQuery)
              .forall(q => query != null && query.contains(q))
          )
          then {
            if (once) then available = false
            val responseHttpHeaders = responseHeaders.view.mapValues(java.util.List.of(_)).toMap.asJava
            if (debug) then {
              println("match found")
              if (regex) then {
                val pattern = Pattern.compile(requestPath)
                val matcher = pattern.matcher(path)
                if (matcher.matches()) {
                  println(s"regex matches: ${matcher.groupCount()}")
                  (1 to matcher.groupCount()).foreach(i => println(s"$i: ${matcher.group(i)}"))
                } else {
                  println(s"no regex matches for $pattern")
                }
              }
            }
            Some(
              new HttpResponse[Array[Byte]] {
                override def version(): Version = Version.HTTP_1_1

                override def headers(): HttpHeaders =
                  HttpHeaders.of(responseHttpHeaders, (a, b) => true)

                override def uri(): URI = request.uri()

                override def sslSession(): ju.Optional[SSLSession] = Optional.empty()

                override def body(): Array[Byte] =
                  (if (regex)
                   then {
                     val matcher = Pattern.compile(requestPath).matcher(path)
                     if (matcher.matches()) then
                       (1 to matcher.groupCount()).foldLeft(responseBody)((a, i) =>
                         responseBody.replace(s"{{$i}}", matcher.group(i))
                       )
                     else responseBody
                   } else responseBody)
                  .getBytes(StandardCharsets.UTF_8)

                override def request(): HttpRequest = httpRequest

                override def statusCode(): Int = responseStatus

                override def previousResponse(): ju.Optional[HttpResponse[Array[Byte]]] = Optional.empty()

                override def toString(): String =
                  s"Stubbed ${if (once) "once" else "always"} response $responseStatus for $requestMethod $requestPath"
              }
            )
          } else
            if (debug) then println("match not found")
            None

      }
  }

  def httpResponse404(httpRequest: HttpRequest) =
    new HttpResponse[Array[Byte]] {
      override def version(): Version = Version.HTTP_1_1

      override def headers(): HttpHeaders = HttpHeaders.of(ju.Map.of(), (a, b) => true)

      override def uri(): URI = request.uri()

      override def sslSession(): ju.Optional[SSLSession] = Optional.empty()

      override def body(): Array[Byte] = Array.emptyByteArray

      override def request(): HttpRequest = httpRequest

      override def statusCode(): Int = 404

      override def previousResponse(): ju.Optional[HttpResponse[Array[Byte]]] = Optional.empty()

      override def toString(): String = s"Stubbed HTTP 404 NOT_FOUND response"
    }

}
