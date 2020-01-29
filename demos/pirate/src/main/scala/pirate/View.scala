package pirate

import indigo._

object View {

  // STEP 2
  def drawBackground: SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Graphic(Rectangle(0, 0, 640, 360), 50, Assets.Static.backgroundRef))

  // STEP 3
  def sceneAudio: SceneUpdateFragment =
    SceneUpdateFragment.empty
      .withAudio(
        SceneAudio(
          SceneAudioSource(
            BindingKey(Assets.Sounds.shanty),
            PlaybackPattern.SingleTrackLoop(
              Track(Assets.Sounds.shanty)
            )
          )
        )
      )

  // STEP 4
  def drawWater(viewModel: ViewModel): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        viewModel.waterReflections.play(),
        viewModel.waterReflections.moveBy(150, 30).play(),
        viewModel.waterReflections.moveBy(-100, 60).play()
      )

  // STEP 7
  def drawForeground(viewModel: ViewModel, screenDimensions: Rectangle): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        Text("The Cursed Pirate\n@davidjamessmith", 0, 0, 5, Assets.Fonts.fontKey).alignRight
          .withAlpha(0.5d)
          .moveTo(screenDimensions.right - 5, screenDimensions.bottom - 30),
        viewModel.flag.play(),
        viewModel.helm.play(),
        Assets.Trees.tallTrunkGraphic.moveTo(420, 220),
        Assets.Trees.leftLeaningTrunkGraphic.moveTo(100, 270),
        Assets.Trees.rightLeaningTrunkGraphic.moveTo(25, 150),
        viewModel.backTallPalm.moveTo(420, 210).changeCycle(CycleLabel("P Back")).play(),
        viewModel.frontPalm.moveTo(397, 188).play(),
        viewModel.frontPalm.moveTo(77, 235).play(),
        viewModel.frontPalm.moveTo(37, 104).play(),
        Assets.Static.chestGraphic.moveTo(380, 271),
        Assets.Static.levelGraphic
      )

  // STEP 8
  def drawPirate(model: Model, captain: Sprite): SceneUpdateFragment = {
    val updatedCaptain = model.pirateState match {
      case PirateState.Idle =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Idle"))
          .play()

      case PirateState.MoveLeft =>
        captain
          .moveTo(model.position)
          .flipHorizontal(true)
          .moveBy(-20, 0)
          .changeCycle(CycleLabel("Run"))
          .play()

      case PirateState.MoveRight =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Run"))
          .play()

      case PirateState.Falling =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Fall"))
          .play()
    }

    SceneUpdateFragment.empty
      .addGameLayerNodes(updatedCaptain)
  }

  // STEP 9
  def drawPirateWithRespawn(gameTime: GameTime, model: Model, captain: Sprite): SceneUpdateFragment = {
    val updatedCaptain = model.pirateState match {
      case PirateState.Idle =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Idle"))
          .play()

      case PirateState.MoveLeft =>
        captain
          .moveTo(model.position)
          .flipHorizontal(true)
          .moveBy(-20, 0)
          .changeCycle(CycleLabel("Run"))
          .play()

      case PirateState.MoveRight =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Run"))
          .play()

      case PirateState.Falling =>
        captain
          .moveTo(model.position)
          .changeCycle(CycleLabel("Fall"))
          .play()
    }

    SceneUpdateFragment.empty
      .addGameLayerNodes(respawnEffect(gameTime, model, updatedCaptain))
  }

  // STEP 9
  def respawnEffect(gameTime: GameTime, model: Model, captain: Sprite): Sprite = {
    val flashActive: Signal[Boolean] =
      Signal(_ < model.lastRespawn + Millis(2000))

    val flashOnOff: Signal[Boolean] =
      Signal.Pulse(Millis(100))

    val combinedSignals: Signal[(Boolean, Boolean)] =
      flashActive |*| flashOnOff

    val captainWithAlpha: SignalFunction[(Boolean, Boolean), Sprite] =
      SignalFunction {
        case (false, _)    => captain
        case (true, true)  => captain.withAlpha(1)
        case (true, false) => captain.withAlpha(0)
      }

    val signal = combinedSignals |> captainWithAlpha

    signal.at(gameTime.running)
  }

}
