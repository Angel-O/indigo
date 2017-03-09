package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine.{AssetType, ImageAsset, TextAsset}

object MyAssets {

//  val spriteSheetName1: String = "blob1"
//  val spriteSheetName2: String = "blob2"
//  val spriteSheetName3: String = "f"
//  val trafficLightsName: String = "trafficlights"
//  val fontName: String = "fontName"
  val smallFontName: String = "smallFontName"
  val dudeName: String = "base_charactor"
//  val light: String = "light"
//  val sludge: String = "sludge"

//  private val spriteAsset1 = ImageAsset(spriteSheetName1, "Sprite-0001.png")
//  private val spriteAsset2 = ImageAsset(spriteSheetName2, "Sprite-0002.png")
//  private val spriteAsset3 = ImageAsset(spriteSheetName3, "f-texture.png")
//  private val trafficLightsAsset = ImageAsset(trafficLightsName, "trafficlights.png")

  def assets: Set[AssetType] =
    Set(
//      spriteAsset1,
//      spriteAsset2,
//      spriteAsset3,
//      trafficLightsAsset,
//      ImageAsset(fontName, "boxy_bold_font_5.png"),
      ImageAsset(smallFontName, "boxy_font.png"),
//      ImageAsset(light, "light_texture.png"),
//      TextAsset(trafficLightsName + "-json", trafficLightsName + ".json"),
      TextAsset(dudeName + "-json", dudeName + ".json"),
      ImageAsset(dudeName, dudeName + ".png")//,
//      ImageAsset(sludge, "sludge.png")
    )

}
