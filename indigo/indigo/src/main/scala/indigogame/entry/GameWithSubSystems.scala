package indigogame.entry

import indigo.GameTime
import indigo.Dice
import indigo.GlobalEvent
import indigo.Outcome
import indigo.InputState
import indigo.SceneUpdateFragment
import indigoexts.subsystems.SubSystemsRegister
import indigo.shared.abstractions.syntax._
import indigo.shared.BoundaryLocator

final class GameWithSubSystems[Model](val model: Model, val subSystemsRegister: SubSystemsRegister)
object GameWithSubSystems {

  def update[Model](
      modelUpdate: (GameTime, Model, InputState, Dice) => GlobalEvent => Outcome[Model]
  )(gameTime: GameTime, model: GameWithSubSystems[Model], inputState: InputState, dice: Dice): GlobalEvent => Outcome[GameWithSubSystems[Model]] =
    e =>
      (modelUpdate(gameTime, model.model, inputState, dice)(e), model.subSystemsRegister.update(gameTime, inputState, dice)(e))
        .map2((m, s) => new GameWithSubSystems(m, s))

  def updateViewModel[Model, ViewModel](
      viewModelUpdate: (GameTime, Model, ViewModel, InputState, Dice, BoundaryLocator) => Outcome[ViewModel]
  )(gameTime: GameTime, model: GameWithSubSystems[Model], viewModel: ViewModel, inputState: InputState, dice: Dice, boundaryLocator: BoundaryLocator): Outcome[ViewModel] =
    viewModelUpdate(gameTime, model.model, viewModel, inputState, dice, boundaryLocator)

  def present[Model, ViewModel](
      viewPresent: (GameTime, Model, ViewModel, InputState, BoundaryLocator) => SceneUpdateFragment
  )(gameTime: GameTime, model: GameWithSubSystems[Model], viewModel: ViewModel, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment =
    viewPresent(gameTime, model.model, viewModel, inputState, boundaryLocator) |+| model.subSystemsRegister.render(gameTime)

}
