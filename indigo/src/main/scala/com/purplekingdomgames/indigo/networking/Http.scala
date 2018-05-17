package com.purplekingdomgames.indigo.networking

import com.purplekingdomgames.indigo.gameengine.events.{GlobalEventStream, HttpRequest}
import com.purplekingdomgames.indigo.gameengine.events.HttpReceiveEvent.{HttpError, HttpResponse}
import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest

object Http {

  def processRequest(request: HttpRequest): Unit =
    try {

      val xhr = new XMLHttpRequest

      xhr.open(request.method, request.fullUrl)

      request.headers.foreach { h =>
        xhr.setRequestHeader(h._1, h._2)
      }

      xhr.onload = (_: dom.Event) => {

        val parsedHeaders: Map[String, String] =
          xhr
            .getAllResponseHeaders()
            .split("\\r\\n")
            .map(
              p =>
                p.split(':').map(_.trim).toList match {
                  case Nil =>
                    None
                  case x :: y :: Nil =>
                    Option((x, y))

                  case x :: Nil =>
                    Option((x, ""))

                  case _ =>
                    None

              }
            )
            .collect { case Some(t) => t }
            .foldLeft(Map.empty[String, String])(_ + _)

        val body = ((str: String) => if (str.isEmpty) None else Option(str))(xhr.responseText)

        GlobalEventStream.pushGameEvent(
          HttpResponse(
            status = xhr.status,
            headers = parsedHeaders,
            body = body
          )
        )
      }

      xhr.onerror = (_: dom.Event) => GlobalEventStream.pushGameEvent(HttpError)

      request.body match {
        case Some(b) =>
          xhr.send(b)

        case None =>
          xhr.send()
      }

    } catch {
      case _: Throwable =>
        GlobalEventStream.pushGameEvent(HttpError)
    }

}

object HttpMethod {
  val GET: String    = "GET"
  val POST: String   = "POST"
  val PUT: String    = "PUT"
  val DELETE: String = "DELETE"
}
