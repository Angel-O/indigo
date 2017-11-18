package com.purplekingdomgames.server

import com.purplekingdomgames.shared.{AssetList, GameConfig}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import io.circe.generic.auto._
import io.circe.syntax._

import java.io.File
import fs2.interop.cats._

object Service {
  val service = HttpService {

    case GET -> Root / "game" / "id" / "config" =>
      Ok(GameConfig.default.asJson)

    case GET -> Root / "game" / "id" / "assets" =>
      Ok(
        AssetList.empty
          .withImage("smallFontName", "assets/boxy_font.png")
          .withImage("light", "assets/light_texture.png")
          .withImage("base_charactor", "assets/base_charactor.png")
          .withImage("trafficlights", "assets/trafficlights.png")
          .withText("indigoJson", "assets/indigo.json")
          .withText("base_charactor-json", "assets/base_charactor.json")
          .asJson
      )

    case request @ GET -> Root / "game" / "id" / "assets" / path =>
      StaticFile.fromFile(new File("./server/assets/" + path), Some(request))
        .getOrElseF(NotFound())

  }
}
