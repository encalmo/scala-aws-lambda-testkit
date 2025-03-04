package org.encalmo.lambda

import sttp.client4.*
import sttp.model.Uri

class StubbedHttpServerSpec extends munit.FunSuite {

  val quickRequest = basicRequest.response(asStringAlways)

  test("Stub http response once") {
    val server = new StubbedHttpServer()
    server.stubResponseOnce(
      "GET",
      "/test",
      201,
      Map("Content-Type" -> "text/plain"),
      "Hello!"
    )
    val response =
      quickRequest.get(Uri(server.uri).addPath("test")).send(quick.backend)
    assertEquals(response.code.code, 201)
    assertEquals(response.body, "Hello!")
    assertEquals(response.header("Content-Type"), Some("text/plain"))

    val response2 =
      quickRequest.get(Uri(server.uri).addPath("test")).send(quick.backend)
    assertEquals(response2.code.code, 404)

    val response3 =
      quickRequest.post(Uri(server.uri).addPath("test")).send(quick.backend)
    assertEquals(response3.code.code, 404)

    val response4 =
      quickRequest.post(Uri(server.uri).addPath("dummy")).send(quick.backend)
    assertEquals(response4.code.code, 404)

    server.close()
  }

  test("Stub http response always") {
    val server = new StubbedHttpServer()
    server.stubResponseAlways(
      "GET",
      "/test",
      201,
      Map("Content-Type" -> "text/plain"),
      "Hello!"
    )
    val response =
      quickRequest.get(Uri(server.uri).addPath("test")).send(quick.backend)
    assertEquals(response.code.code, 201)
    assertEquals(response.body, "Hello!")
    assertEquals(response.header("Content-Type"), Some("text/plain"))

    val response2 =
      quickRequest.get(Uri(server.uri).addPath("test")).send(quick.backend)
    assertEquals(response2.code.code, 201)
    assertEquals(response2.body, "Hello!")

    val response3 =
      quickRequest.post(Uri(server.uri).addPath("test")).send(quick.backend)
    assertEquals(response3.code.code, 404)

    val response4 =
      quickRequest.post(Uri(server.uri).addPath("dummy")).send(quick.backend)
    assertEquals(response4.code.code, 404)

    server.close()
  }
}
