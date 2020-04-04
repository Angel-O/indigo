package indigojs.delegates

import indigo.shared.datatypes.Effects
import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Effects")
final class EffectsDelegate(_alpha: Double, _tint: RGBADelegate, _flip: FlipDelegate) {

  @JSExport
  val alpha = _alpha
  @JSExport
  val tint = _tint
  @JSExport
  val flip = _flip

  @JSExport
  def withAlpha(newAlpha: Double): EffectsDelegate =
    new EffectsDelegate(newAlpha, tint, flip)

  @JSExport
  def withTint(newTint: RGBADelegate): EffectsDelegate =
    new EffectsDelegate(alpha, newTint, flip)

  @JSExport
  def withFlip(newFlip: FlipDelegate): EffectsDelegate =
    new EffectsDelegate(alpha, tint, newFlip)

  def toInternal: Effects =
    Effects(alpha, tint.toInternal, flip.toInternal)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("EffectsHelper")
@JSExportAll
object EffectsDelegate {

  def None: EffectsDelegate =
    new EffectsDelegate(1, RGBADelegate.None, FlipDelegate.None)

}

object EffectsUtilities {
    implicit class EffectsConvert(val obj: Effects) {
        def toJsDelegate = new EffectsDelegate(
            obj.alpha,
            new TintDelegate(obj.tint.r, obj.tint.g, obj.tint.b, obj.tint.a),
            new FlipDelegate(obj.flip.horizontal, obj.flip.vertical)
        )
    }
}
