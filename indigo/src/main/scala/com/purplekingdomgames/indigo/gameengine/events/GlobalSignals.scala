package com.purplekingdomgames.indigo.gameengine.events

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

object GlobalSignals {

  def MousePosition: Point = GlobalSignalsManager.MousePosition

}

private[indigo] object GlobalSignalsManager {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var signals: Signals = Signals.default

  def update(events: List[GameEvent]): Signals = {
    signals = events.foldLeft(signals) { (sigs, e) =>
      e match {
        case mp: MouseEvent.Move =>
          sigs.copy(mousePosition = mp.position)

        case _ =>
          sigs
      }
    }

    signals
  }

  def MousePosition: Point = signals.mousePosition

}

private[indigo] case class Signals(mousePosition: Point)
private[indigo] object Signals {
  val default: Signals = Signals(
    mousePosition = Point.zero
  )
}
