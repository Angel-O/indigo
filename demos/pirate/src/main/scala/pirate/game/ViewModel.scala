package pirate.game

import indigo._

import pirate.init.StartupData

final case class ViewModel(waterReflections: Sprite, flag: Sprite, captain: Sprite, helm: Sprite, palm: Sprite) {
  val backTallPalm: Sprite = {
    palm
      .withBindingKey(BindingKey("Back Tall Palm"))
      .withAnimationKey(AnimationKey("P Back Tall"))
      .withDepth(Depth(10))
  }
}

object ViewModel {

  def initialViewModel(startupData: StartupData, screenDimensions: Rectangle): ViewModel =
    ViewModel(
      startupData.waterReflections
        .withRef(85, 0)
        .moveTo(screenDimensions.horizontalCenter, screenDimensions.verticalCenter + 5),
      startupData.flag.withRef(22, 105).moveTo(200, 270),
      startupData.captain.withRef(37, 63).moveTo(300, 271),
      startupData.helm.moveTo(605, 137).withRef(31, 49),
      startupData.palm
    )

}
