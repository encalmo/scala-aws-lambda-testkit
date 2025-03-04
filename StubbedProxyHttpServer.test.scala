package org.encalmo.lambda

import org.encalmo.utils.JsonUtils.{getString, readAsJson}

import sttp.client4.*
import sttp.model.{MediaType, Uri}
import java.net.URI

class StubbedProxyHttpServerSpec extends munit.FunSuite {

  val quickRequest = basicRequest.response(asStringAlways)

  test("Test stubbed proxy server api.ipify.org".flaky) {
    val server =
      new StubbedProxyHttpServer(
        URI("https://api.ipify.org"),
        debug = true
      )
    try {
      val response =
        quickRequest.get(Uri(server.uri)).send(quick.backend)
      assertEquals(response.code.code, 200)
      server
        .stubResponseOnce(
          requestMethod = "GET",
          requestPath = "/test",
          responseStatus = 201,
          responseHeaders = Map.empty,
          responseBody = "Hello!"
        )
      server
        .stubResponseOnce(
          requestMethod = "GET",
          requestPath = "/test",
          responseStatus = 202,
          responseHeaders = Map.empty,
          responseBody = "Bingo!"
        )
      server
        .stubResponseAlways(
          requestMethod = "POST",
          requestPath = "/test/me",
          responseStatus = 203,
          responseHeaders = Map.empty,
          responseBody = "Me!",
          requestQuery = "format=json"
        )
      val response2 =
        quickRequest.get(Uri(server.uri).addPath("test")).send(quick.backend)
      assertEquals(response2.code.code, 201)
      assertEquals(response2.body, "Hello!")
      val response3 =
        quickRequest.get(Uri(server.uri).addPath("test")).send(quick.backend)
      assertEquals(response3.code.code, 202)
      assertEquals(response3.body, "Bingo!")
      val response4 =
        quickRequest.get(Uri(server.uri).addPath("test")).send(quick.backend)
      assertEquals(response4.code.code, 404)
      (1 to 5).foreach { _ =>
        val response5 =
          quickRequest
            .post(
              Uri(server.uri)
                .addPath("test", "me")
                .addParam("format", "json")
                .addParam("foo", "bar")
            )
            .send(quick.backend)
        assertEquals(response5.code.code, 203)
        assertEquals(response5.body, "Me!")
      }
    } finally {
      server.close()
    }
  }

  test("Test stubbed foo proxy server with regex".flaky) {
    val server =
      new StubbedProxyHttpServer(
        URI(
          "https://ukr7depy1b.execute-api.eu-central-1.amazonaws.com/live"
        ),
        debug = true
      )
    try {
      val response =
        quickRequest
          .get(Uri(server.uri).addPath("health"))
          .contentType(MediaType.ApplicationJson)
          .send(quick.backend)
      assertEquals(response.code.code, 200)
      server
        .stubResponseOnce(
          requestMethod = "GET",
          requestPath = "/(.+)",
          responseStatus = 201,
          responseHeaders = Map("content-type" -> "application/json; charset=utf-8"),
          responseBody = "{\"foo\":\"{{1}}\"}",
          regex = true
        )
      val response2 =
        quickRequest
          .get(Uri(server.uri).addPath("health"))
          .contentType(MediaType.ApplicationJson)
          .send(quick.backend)
      assertEquals(response2.code.code, 201)
      assertEquals(response2.body.readAsJson.getString("foo"), "health")
    } finally {
      server.close()
    }
  }

  test("Test stubbed foo proxy server".flaky) {
    val server =
      new StubbedProxyHttpServer(
        URI(
          "https://ukr7depy1b.execute-api.eu-central-1.amazonaws.com/live"
        ),
        debug = true
      )
    try {
      val response =
        quickRequest
          .get(Uri(server.uri).addPath("health"))
          .contentType(MediaType.ApplicationJson)
          .send(quick.backend)
      assertEquals(response.code.code, 200)
      server
        .stubResponseOnce(
          requestMethod = "GET",
          requestPath = "/health",
          responseStatus = 201,
          responseHeaders = Map("content-type" -> "application/json; charset=utf-8"),
          responseBody = "{\"foo\":\"bar\"}"
        )
      val response2 =
        quickRequest
          .get(Uri(server.uri).addPath("health"))
          .contentType(MediaType.ApplicationJson)
          .send(quick.backend)
      assertEquals(response2.code.code, 201)
      assertEquals(response2.body.readAsJson.getString("foo"), "bar")
    } finally {
      server.close()
    }
  }
}
