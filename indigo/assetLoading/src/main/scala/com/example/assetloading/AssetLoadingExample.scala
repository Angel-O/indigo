package com.example.assetloading

import indigo._
import indigoexts.entrypoint._
import indigoexts.ui._
import indigoexts.subsystems.assetbundleloader._

object AssetLoadingExample extends IndigoGameBasic[Unit, MyGameModel, MyViewModel] {

  val config: GameConfig =
    defaultGameConfig.withMagnification(2)

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] =
    Set(AssetBundleLoader.subSystem)

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    assetCollection.findTextDataByName(AssetName("text")) match {
      case Some(value) =>
        println("Loaded text! " + value)
        Startup.Success(())
      case None =>
        Startup.Success(())
    }

  def initialModel(startupData: Unit): MyGameModel =
    MyGameModel(loaded = false)

  def update(gameTime: GameTime, model: MyGameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[MyGameModel] = {
    case AssetBundleLoaderEvent.Started(key) =>
      println("Load started! " + key.toString())
      Outcome(model)

    case AssetBundleLoaderEvent.LoadProgress(key, percent, completed, total) =>
      println(s"In progress...: ${key.toString()} - ${percent.toString()}%, ${completed.toString()} of ${total.toString()}")
      Outcome(model)

    case AssetBundleLoaderEvent.Success(key) =>
      println("Got it! " + key.toString())
      Outcome(model.copy(loaded = true)).addGlobalEvents(PlaySound(AssetName("sfx"), Volume.Max))

    case AssetBundleLoaderEvent.Failure(key) =>
      println("Lost it... " + key.toString())
      Outcome(model)

    case _ =>
      Outcome(model)
  }

  def initialViewModel(startupData: Unit): MyGameModel => MyViewModel =
    _ =>
      MyViewModel(
        button = Button(
          buttonAssets = Assets.buttonAssets,
          bounds = Rectangle(10, 10, 16, 16),
          depth = Depth(2)
        ).withUpAction {
          println("Start loading assets...")
          List(AssetBundleLoaderEvent.Load(BindingKey("Junction box assets"), Assets.junctionboxImageAssets ++ Assets.otherAssetsToLoad))
        }
      )

  def updateViewModel(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, inputState: InputState, dice: Dice): Outcome[MyViewModel] =
    viewModel.button.update(inputState.mouse).map { btn =>
      viewModel.copy(button = btn)
    }

  def present(gameTime: GameTime, model: MyGameModel, viewModel: MyViewModel, inputState: InputState): SceneUpdateFragment = {
    val box = if (model.loaded) {
      List(
        Graphic(Rectangle(0, 0, 64, 64), 1, Assets.junctionBoxMaterial)
          .moveTo(30, 30)
      )
    } else Nil

    viewModel.button.draw //(inputState)
      .addGameLayerNodes(box)
  }
}

final case class MyGameModel(loaded: Boolean)
final case class MyViewModel(button: Button)

object Assets {

  val junctionBoxAlbedo: AssetName   = AssetName("junctionbox_albedo")
  val junctionBoxEmission: AssetName = AssetName("junctionbox_emission")
  val junctionBoxNormal: AssetName   = AssetName("junctionbox_normal")
  val junctionBoxSpecular: AssetName = AssetName("junctionbox_specular")

  def junctionboxImageAssets: Set[AssetType] =
    Set(
      AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo.value + ".png")),
      AssetType.Image(junctionBoxEmission, AssetPath("assets/" + junctionBoxEmission.value + ".png")),
      AssetType.Image(junctionBoxNormal, AssetPath("assets/" + junctionBoxNormal.value + ".png")),
      AssetType.Image(junctionBoxSpecular, AssetPath("assets/" + junctionBoxSpecular.value + ".png"))
    )

  def otherAssetsToLoad: Set[AssetType] =
    Set(
      AssetType.Text(AssetName("text"), AssetPath("assets/test.txt")),
      AssetType.Audio(AssetName("sfx"), AssetPath("assets/RetroGameJump.mp3"))
    )

  val junctionBoxMaterial: Material.Lit =
    Material.Lit(
      junctionBoxAlbedo,
      junctionBoxEmission,
      junctionBoxNormal,
      junctionBoxSpecular
    )

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png"))
    )

  val buttonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 16, 16, 16),
      down = Graphic(0, 0, 16, 16, 2, Material.Textured(AssetName("graphics"))).withCrop(32, 32, 16, 16)
    )

}
