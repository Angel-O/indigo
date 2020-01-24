package indigo.shared.events

import indigo.shared.constants.Key
import indigo.shared.datatypes.{Point, Rectangle}

final class FrameInputEvents(val globalEvents: List[GlobalEvent], val signals: InputSignals) extends FrameMouseEvents with FrameKeyboardEvents

object FrameInputEvents {
  val empty: FrameInputEvents =
    FrameInputEvents(Nil, InputSignals.default)

  def apply(globalEvents: List[GlobalEvent], signals: InputSignals): FrameInputEvents =
    new FrameInputEvents(globalEvents, signals)
}

trait FrameMouseEvents {

  val globalEvents: List[GlobalEvent]

  def mouseEvents: List[MouseEvent] = globalEvents.collect { case e: MouseEvent => e }

  def mouseClickAt: Option[Point]    = mouseEvents.collectFirst { case m: MouseEvent.Click     => m.position }
  def mouseUpAt: Option[Point]       = mouseEvents.collectFirst { case m: MouseEvent.MouseUp   => m.position }
  def mouseDownAt: Option[Point]     = mouseEvents.collectFirst { case m: MouseEvent.MouseDown => m.position }
  def mousePositionAt: Option[Point] = mouseEvents.collectFirst { case m: MouseEvent.Move      => m.position }

  // At
  private def wasMouseAt(position: Point, maybePosition: Option[Point]): Boolean =
    maybePosition match {
      case Some(pt) => position === pt
      case None     => false
    }

  def wasMouseClickedAt(position: Point): Boolean = wasMouseAt(position, mouseClickAt)
  def wasMouseClickedAt(x: Int, y: Int): Boolean  = wasMouseClickedAt(Point(x, y))

  def wasMouseUpAt(position: Point): Boolean = wasMouseAt(position, mouseUpAt)
  def wasMouseUpAt(x: Int, y: Int): Boolean  = wasMouseUpAt(Point(x, y))

  def wasMouseDownAt(position: Point): Boolean = wasMouseAt(position, mouseDownAt)
  def wasMouseDownAt(x: Int, y: Int): Boolean  = wasMouseDownAt(Point(x, y))

  def wasMousePositionAt(position: Point): Boolean = wasMouseAt(position, mousePositionAt)
  def wasMousePositionAt(x: Int, y: Int): Boolean  = wasMousePositionAt(Point(x, y))

  //Within
  private def wasMouseWithin(bounds: Rectangle, maybePosition: Option[Point]): Boolean =
    maybePosition match {
      case Some(pt) => bounds.isPointWithin(pt)
      case None     => false
    }

  def wasMouseClickedWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mouseClickAt)
  def wasMouseClickedWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasMouseClickedWithin(Rectangle(x, y, width, height))

  def wasMouseUpWithin(bounds: Rectangle): Boolean                       = wasMouseWithin(bounds, mouseUpAt)
  def wasMouseUpWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseUpWithin(Rectangle(x, y, width, height))

  def wasMouseDownWithin(bounds: Rectangle): Boolean                       = wasMouseWithin(bounds, mouseDownAt)
  def wasMouseDownWithin(x: Int, y: Int, width: Int, height: Int): Boolean = wasMouseDownWithin(Rectangle(x, y, width, height))

  def wasMousePositionWithin(bounds: Rectangle): Boolean = wasMouseWithin(bounds, mousePositionAt)
  def wasMousePositionWithin(x: Int, y: Int, width: Int, height: Int): Boolean =
    wasMousePositionWithin(Rectangle(x, y, width, height))

}

trait FrameKeyboardEvents {

  val globalEvents: List[GlobalEvent]

  def keyboardEvents: List[KeyboardEvent] = globalEvents.collect { case e: KeyboardEvent => e }

  def keysUp: List[Key]   = keyboardEvents.collect { case k: KeyboardEvent.KeyUp   => k.keyCode }
  def keysDown: List[Key] = keyboardEvents.collect { case k: KeyboardEvent.KeyDown => k.keyCode }

  def keysAreDown(keys: Key*): Boolean = keys.forall(keyCode => keysDown.contains(keyCode))
  def keysAreUp(keys: Key*): Boolean   = keys.forall(keyCode => keysUp.contains(keyCode))

}
