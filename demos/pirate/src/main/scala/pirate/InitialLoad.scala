package pirate

import indigo._
import indigo.json.Json
import indigoexts.formats._

object InitialLoad {

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData] = {

    val loadedReflections: Option[SpriteAndAnimations] = for {
      json                <- assetCollection.findTextDataByName(AssetName(Assets.Water.reflectionJsonRef))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(20), Assets.Water.reflectionRef)
    } yield spriteAndAnimations

    val loadedFlag: Option[SpriteAndAnimations] = for {
      json                <- assetCollection.findTextDataByName(AssetName(Assets.Flag.jsonRef))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- AsepriteConverter.toSpriteAndAnimations(aseprite, Depth(10), Assets.Flag.ref)
    } yield spriteAndAnimations

    (loadedReflections, loadedFlag) match {
      case (Some(reflections), Some(flag)) =>
        makeStartupData(reflections, flag)

      case (None, _) =>
        Startup.Failure(StartupErrors("Failed to load the water reflections"))

      case (_, None) =>
        Startup.Failure(StartupErrors("Failed to load the flag"))

    }
  }

  def makeStartupData(waterReflections: SpriteAndAnimations, flag: SpriteAndAnimations): Startup.Success[StartupData] =
    Startup
      .Success(
        StartupData(waterReflections.sprite, flag.sprite.withDepth(Depth(10)))
      )
      .addAnimations(waterReflections.animations, flag.animations)

}

final case class StartupData(waterReflections: Sprite, flag: Sprite)
