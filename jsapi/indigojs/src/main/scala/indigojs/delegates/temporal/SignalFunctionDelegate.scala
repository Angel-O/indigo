package indigojs.delegates.temporal

import scala.scalajs.js.annotation._
import indigo.shared.temporal.SignalFunction
import indigo.shared.temporal.Signal
import indigo.shared.time.Millis

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SignalFunction")
final class SignalFunctionDelegate(val run: SignalDelegate => SignalDelegate) {

  @JSExport
  def andThen(other: SignalFunctionDelegate): SignalFunctionDelegate = {
    val sf: SignalFunction[Any, Any] = SignalFunction.andThen(this.toInternal, other.toInternal)

    new SignalFunctionDelegate((sd: SignalDelegate) => {
      new SignalDelegate(d => sf.run(sd.toInternal).at(Millis(d.toLong)))
    })
  }

  @JSExport
  def and(other: SignalFunctionDelegate): SignalFunctionDelegate = {
    val sf: SignalFunction[Any, (Any, Any)] = SignalFunction.parallel(this.toInternal, other.toInternal)

    new SignalFunctionDelegate((sd: SignalDelegate) => {
      new SignalDelegate(d => sf.run(sd.toInternal).at(Millis(d.toLong)))
    })
  }

  def toInternal: SignalFunction[Any, Any] = {
    def convert(s: Signal[Any]): SignalDelegate = new SignalDelegate(d => s.at(Millis(d.toLong)))
    new SignalFunction(sa => run(convert(sa)).toInternal)
  }

}
