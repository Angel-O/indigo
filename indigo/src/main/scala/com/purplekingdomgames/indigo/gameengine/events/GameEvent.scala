package com.purplekingdomgames.indigo.gameengine.events

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point
import com.purplekingdomgames.indigo.networking.HttpMethod

sealed trait GameEvent

case object FrameTick extends GameEvent

sealed trait MouseEvent extends GameEvent {
  val x: Int
  val y: Int
  def position: Point = Point(x, y)
}
object MouseEvent {
  case class Click(x: Int, y: Int) extends MouseEvent
  case class MouseUp(x: Int, y: Int) extends MouseEvent
  case class MouseDown(x: Int, y: Int) extends MouseEvent
  case class Move(x: Int, y: Int) extends MouseEvent
}

sealed trait KeyboardEvent extends GameEvent {
  val keyCode: Int
}
object KeyboardEvent {
  case class KeyUp(keyCode: Int) extends KeyboardEvent
  case class KeyDown(keyCode: Int) extends KeyboardEvent
  case class KeyPress(keyCode: Int) extends KeyboardEvent
}

trait ViewEvent extends GameEvent

sealed trait NetworkSendEvent extends ViewEvent
sealed trait NetworkReceiveEvent extends GameEvent

case class WebSocketSend() extends NetworkSendEvent
case class WebSocketReceive() extends NetworkReceiveEvent
case class WebSocketError() extends NetworkReceiveEvent
case class WebSocketClose() extends NetworkReceiveEvent

sealed trait HttpReceiveEvent extends NetworkReceiveEvent
object HttpReceiveEvent {
  case object HttpError extends HttpReceiveEvent
  case class HttpResponse(status: Int, headers: Map[String, String], body: Option[String]) extends HttpReceiveEvent
}

sealed trait HttpRequest extends NetworkSendEvent {
  val params: Map[String, String]
  val url: String
  val headers: Map[String, String]
  val body: Option[String]
  val method: String

  val fullUrl: String = if(params.isEmpty) url else url + "?" + params.toList.map(p => p._1 + "=" + p._2).mkString("&")
}
object HttpRequest {
  case class GET(url: String, params: Map[String, String], headers: Map[String, String]) extends HttpRequest {
    val body: Option[String] = None
    val method: String = HttpMethod.GET
  }
  case class POST(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String]) extends HttpRequest {
    val method: String = HttpMethod.POST
  }
  case class PUT(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String]) extends HttpRequest {
    val method: String = HttpMethod.PUT
  }
  case class DELETE(url: String, params: Map[String, String], headers: Map[String, String], body: Option[String]) extends HttpRequest {
    val method: String = HttpMethod.DELETE
  }

  object GET {
    def apply(url: String): GET =
      GET(url, Map(), Map())
  }

  object POST {
    def apply(url: String, body: String): POST =
      POST(url, Map(), Map(), Option(body))
  }

  object PUT {
    def apply(url: String, body: String): PUT =
      PUT(url, Map(), Map(), Option(body))
  }

  object DELETE {
    def apply(url: String, body: Option[String]): DELETE =
      DELETE(url, Map(), Map(), body)
  }
}