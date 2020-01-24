package indigo.platform.events

import indigo.shared.platform.GlobalEventStream
import indigo.shared.events.{GlobalEvent, NetworkSendEvent, PlaySound}
import indigo.shared.networking.{HttpRequest, WebSocketEvent}
import indigo.shared.platform.AudioPlayer

import indigo.platform.networking.{Http, WebSockets}

import scala.collection.mutable
import indigo.shared.events.StorageEvent
import indigo.shared.platform.Storage

object GlobalEventStreamImpl {

  def default(audioPlayer: AudioPlayer, storage: Storage): GlobalEventStream =
    new GlobalEventStream {
      val audioFilter   = AudioEventProcessor.filter(audioPlayer)
      val storageFilter = StorageEventProcessor.filter(storage)

      private val eventQueue: mutable.Queue[GlobalEvent] =
        new mutable.Queue[GlobalEvent]()

      def pushGlobalEvent(e: GlobalEvent): Unit =
        NetworkEventProcessor
          .filter(this)(e)
          .flatMap { audioFilter }
          .flatMap { storageFilter }
          .foreach(e => eventQueue += e)

      def collect: List[GlobalEvent] =
        eventQueue.dequeueAll(_ => true).toList
    }

  object NetworkEventProcessor {

    def filter(globalEventStream: GlobalEventStream): GlobalEvent => Option[GlobalEvent] = {
      case httpRequest: HttpRequest =>
        Http.processRequest(httpRequest, globalEventStream)
        None

      case webSocketEvent: WebSocketEvent with NetworkSendEvent =>
        WebSockets.processSendEvent(webSocketEvent, globalEventStream)
        None

      case e =>
        Some(e)
    }

  }

  object AudioEventProcessor {

    def filter: AudioPlayer => GlobalEvent => Option[GlobalEvent] = audioPlayer => {
      case PlaySound(assetRef, volume) =>
        audioPlayer.playSound(assetRef, volume)
        None

      case e =>
        Some(e)
    }

  }

  object StorageEventProcessor {

    def filter: Storage => GlobalEvent => Option[GlobalEvent] = storage => {
      case StorageEvent.Save(key, data) =>
        storage.save(key, data)
        None

      case StorageEvent.Load(key) =>
        storage.load(key).map(data => StorageEvent.Loaded(data))

      case StorageEvent.Delete(key) =>
        storage.delete(key)
        None

      case StorageEvent.DeleteAll =>
        storage.deleteAll()
        None

      case e @ StorageEvent.Loaded(_) =>
        Some(e)

      case e =>
        Some(e)
    }

  }

}
