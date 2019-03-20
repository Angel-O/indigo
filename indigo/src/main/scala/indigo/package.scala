import indigo.gameengine.events.EventTypeAliases
import indigo.gameengine.scenegraph.SceneGraphTypeAliases
import indigo.gameengine.scenegraph.datatypes.DataTypeAliases
import indigo.networking.NetworkingTypeAliases
import indigo.runtime.IndigoLogger
import indigo.shared.SharedTypeAliases

package object indigo extends DataTypeAliases with SceneGraphTypeAliases with NetworkingTypeAliases with SharedTypeAliases with EventTypeAliases {

  val logger: IndigoLogger.type = IndigoLogger

  type AsString[A] = runtime.AsString[A]
  val AsString: runtime.AsString.type = runtime.AsString

  implicit class WithAsString[T](val t: T) extends AnyVal {
    def show(implicit showMe: AsString[T]): String = showMe.show(t)
  }

  type Startup[ErrorType, SuccessType] = gameengine.Startup[ErrorType, SuccessType]
  val Startup: gameengine.Startup.type = gameengine.Startup

  type GameTime = gameengine.GameTime
  val GameTime: gameengine.GameTime.type = gameengine.GameTime

  type SubSystem = gameengine.subsystems.SubSystem

  type AssetCollection = gameengine.assets.AssetCollection
  val AssetCollection: gameengine.assets.AssetCollection.type = gameengine.assets.AssetCollection

  type ToReportable[T] = gameengine.ToReportable[T]
  val ToReportable: gameengine.ToReportable.type = gameengine.ToReportable

  type StartupErrors = gameengine.StartupErrors
  val StartupErrors: gameengine.StartupErrors.type = gameengine.StartupErrors

  type Outcome[T] = gameengine.Outcome[T]
  val Outcome: gameengine.Outcome.type = gameengine.Outcome

  // Constants

  val Keys: gameengine.constants.Keys.type = gameengine.constants.Keys

  type KeyCode = gameengine.constants.KeyCode
  val KeyCode: gameengine.constants.KeyCode.type = gameengine.constants.KeyCode

  type PowerOfTwo = gameengine.PowerOfTwo
  val PowerOfTwo: gameengine.PowerOfTwo.type = gameengine.PowerOfTwo

}
