package indigo.gameengine.scenegraph

import indigo.gameengine.assets.FontRegister
import indigo.gameengine.scenegraph.datatypes.{FontChar, FontInfo, FontKey, Rectangle}
import org.scalatest.{FunSpec, Matchers}

class TextSpec extends FunSpec with Matchers {

  describe("Text entities") {

    it("should be able to correctly calculate the bounds where all are equal") {

      val chars = List(
        FontChar("a", 0, 16, 16, 16),
        FontChar("b", 16, 16, 16, 16),
        FontChar("c", 32, 16, 16, 16)
      )

      val fontKey = FontKey("test")

      val fontInfo = FontInfo(fontKey, "font-sheet", 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

      FontRegister.register(fontInfo)

      val t = Text("abc", 10, 20, 1, fontKey)

      t.bounds shouldEqual Rectangle(0, 0, 16 * 3, 16)

    }

    it("should be able to correctly calculate the bounds with different sized chars") {

      val chars = List(
        FontChar("a", 0, 16, 10, 10),
        FontChar("b", 30, 16, 20, 20),
        FontChar("c", 60, 16, 30, 30)
      )

      val fontKey = FontKey("test")

      val fontInfo = FontInfo(fontKey, "font-sheet", 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

      FontRegister.register(fontInfo)

      val t = Text("abc", 10, 20, 1, fontKey)

      t.bounds shouldEqual Rectangle(0, 0, 10 + 20 + 30, 30)

    }

  }

}
