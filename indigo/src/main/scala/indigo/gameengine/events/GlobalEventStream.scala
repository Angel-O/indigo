package indigo.gameengine.events

import indigo.gameengine.audio.AudioPlayer
import indigo.networking._

import scala.collection.mutable

trait GlobalEventStream {
  def pushGlobalEvent(e: GlobalEvent): Unit
  def collect: List[GlobalEvent]
}

object GlobalEventStream {

  def default(audioPlayer: AudioPlayer): GlobalEventStream =
    new GlobalEventStream {
      private val eventQueue: mutable.Queue[GlobalEvent] =
        new mutable.Queue[GlobalEvent]()

      def pushGlobalEvent(e: GlobalEvent): Unit =
        NetworkEventProcessor
          .filter(this)(e)
          .flatMap { AudioEventProcessor.filter(audioPlayer) }
          .foreach(e => eventQueue += e)

      def collect: List[GlobalEvent] =
        eventQueue.dequeueAll(_ => true).toList
    }

  object NetworkEventProcessor {

    def filter(implicit globalEventStream: GlobalEventStream): GlobalEvent => Option[GlobalEvent] = {
      case httpRequest: HttpRequest =>
        Http.processRequest(httpRequest)
        None

      case webSocketEvent: WebSocketEvent with NetworkSendEvent =>
        WebSockets.processSendEvent(webSocketEvent)
        None

      case e =>
        Option(e)
    }

  }

  object AudioEventProcessor {

    def filter: AudioPlayer => GlobalEvent => Option[GlobalEvent] = audioPlayer => {
      case PlaySound(assetRef, volume) =>
        audioPlayer.playSound(assetRef, volume)
        None

      case e =>
        Option(e)
    }

  }

}
