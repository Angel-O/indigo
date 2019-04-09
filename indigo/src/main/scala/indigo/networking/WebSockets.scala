package indigo.networking

import indigo.gameengine.events._
import indigo.gameengine.scenegraph.datatypes.BindingKey
import indigo.networking.WebSocketReadyState.{CLOSED, CLOSING}
import indigo.runtime.IndigoLogger
import org.scalajs.dom

import indigo.shared.EqualTo._

import scala.collection.mutable

object WebSockets {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val connections: mutable.HashMap[WebSocketId, dom.WebSocket] = mutable.HashMap()
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val configs: mutable.HashMap[WebSocketId, WebSocketConfig] = mutable.HashMap()

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def processSendEvent(event: WebSocketEvent with NetworkSendEvent)(implicit globalEventStream: GlobalEventStream): Unit =
    try event match {
      case WebSocketEvent.ConnectOnly(config) =>
        reEstablishConnection(insertUpdateConfig(config), None)
        ()

      case WebSocketEvent.Open(message, config) =>
        reEstablishConnection(insertUpdateConfig(config), Option(message))
        ()

      case WebSocketEvent.Send(message, config) =>
        reEstablishConnection(insertUpdateConfig(config), None).foreach { socket =>
          socket.send(message)
        }

      case _ =>
        ()
    } catch {
      case e: Throwable =>
        globalEventStream.pushGlobalEvent(WebSocketEvent.Error(event.giveId.getOrElse(WebSocketId("<not found>")), e.getMessage))
    }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private def insertUpdateConfig(config: WebSocketConfig): WebSocketConfig = {
    val maybeConfig = configs.get(config.id)

    maybeConfig
      .flatMap { c =>
        if (c === config)
          Option(c)
        else {
          configs.remove(config.id)
          configs.put(config.id, config)
        }
      }
      .getOrElse(config)
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private def reEstablishConnection(config: WebSocketConfig, onOpenSendMessage: Option[String])(implicit globalEventStream: GlobalEventStream): Option[dom.WebSocket] =
    connections
      .get(config.id)
      .flatMap { conn =>
        WebSocketReadyState.fromInt(conn.readyState) match {
          case CLOSING | CLOSED =>
            newConnection(config, onOpenSendMessage).flatMap { newConn =>
              connections.remove(config.id)
              connections.put(config.id, newConn)
            }

          case _ =>
            Option(conn)
        }
      }
      .orElse {
        newConnection(config, onOpenSendMessage).flatMap { newConn =>
          connections.remove(config.id)
          connections.put(config.id, newConn)
        }
      }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  private def newConnection(config: WebSocketConfig, onOpenSendMessage: Option[String])(implicit globalEventStream: GlobalEventStream): Option[dom.WebSocket] =
    try {
      val socket = new dom.WebSocket(config.address)

      socket.onmessage = (e: dom.MessageEvent) => globalEventStream.pushGlobalEvent(WebSocketEvent.Receive(config.id, e.data.toString))

      socket.onopen = (_: dom.Event) => onOpenSendMessage.foreach(msg => socket.send(msg))

      socket.onerror = (_: dom.Event) => globalEventStream.pushGlobalEvent(WebSocketEvent.Error(config.id, "Web socket connection error"))

      socket.onclose = (_: dom.CloseEvent) => globalEventStream.pushGlobalEvent(WebSocketEvent.Close(config.id))

      Option(socket)
    } catch {
      case e: Throwable =>
        IndigoLogger.info("Error trying to set up a websocket: " + e.getMessage)
        None
    }

}

final case class WebSocketId(id: String) {
  def ===(other: WebSocketId): Boolean =
    WebSocketId.equality(this, other)
}
object WebSocketId {
  def generate: WebSocketId =
    WebSocketId(BindingKey.generate.value)

  def equality(a: WebSocketId, b: WebSocketId): Boolean =
    a.id === b.id
}

final case class WebSocketConfig(id: WebSocketId, address: String) {
  def ===(other: WebSocketConfig): Boolean =
    WebSocketConfig.equality(this, other)
}
object WebSocketConfig {
  def equality(a: WebSocketConfig, b: WebSocketConfig): Boolean =
    a.id === b.id && a.address === b.address
}

sealed trait WebSocketReadyState {
  val value: Int
  val isConnecting: Boolean
  val isOpen: Boolean
  val isClosing: Boolean
  val isClosed: Boolean
}
object WebSocketReadyState {

  case object CONNECTING extends WebSocketReadyState {
    val value: Int            = 0
    val isConnecting: Boolean = true
    val isOpen: Boolean       = false
    val isClosing: Boolean    = false
    val isClosed: Boolean     = false
  }

  case object OPEN extends WebSocketReadyState {
    val value: Int            = 1
    val isConnecting: Boolean = false
    val isOpen: Boolean       = true
    val isClosing: Boolean    = false
    val isClosed: Boolean     = false
  }

  case object CLOSING extends WebSocketReadyState {
    val value: Int            = 2
    val isConnecting: Boolean = false
    val isOpen: Boolean       = false
    val isClosing: Boolean    = true
    val isClosed: Boolean     = false
  }

  case object CLOSED extends WebSocketReadyState {
    val value: Int            = 3
    val isConnecting: Boolean = false
    val isOpen: Boolean       = false
    val isClosing: Boolean    = false
    val isClosed: Boolean     = true
  }

  def fromInt(i: Int): WebSocketReadyState =
    i match {
      case 0 => CONNECTING
      case 1 => OPEN
      case 2 => CLOSING
      case 3 => CLOSED
      case _ => CLOSED
    }

}

sealed trait WebSocketEvent {
  def giveId: Option[WebSocketId] =
    this match {
      case WebSocketEvent.ConnectOnly(config) =>
        Option(config.id)

      case WebSocketEvent.Open(_, config) =>
        Option(config.id)

      case WebSocketEvent.Send(_, config) =>
        Option(config.id)

      case WebSocketEvent.Receive(id, _) =>
        Option(id)

      case WebSocketEvent.Error(id, _) =>
        Option(id)

      case WebSocketEvent.Close(id) =>
        Option(id)

      case _ =>
        None
    }
}
object WebSocketEvent {
  // Send
  final case class ConnectOnly(webSocketConfig: WebSocketConfig)           extends WebSocketEvent with NetworkSendEvent
  final case class Open(message: String, webSocketConfig: WebSocketConfig) extends WebSocketEvent with NetworkSendEvent
  final case class Send(message: String, webSocketConfig: WebSocketConfig) extends WebSocketEvent with NetworkSendEvent

  // Receive
  final case class Receive(webSocketId: WebSocketId, message: String) extends WebSocketEvent with NetworkReceiveEvent
  final case class Error(webSocketId: WebSocketId, error: String)     extends WebSocketEvent with NetworkReceiveEvent
  final case class Close(webSocketId: WebSocketId)                    extends WebSocketEvent with NetworkReceiveEvent
}
