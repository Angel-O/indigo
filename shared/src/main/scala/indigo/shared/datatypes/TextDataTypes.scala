package indigo.shared.datatypes

import indigo.shared.EqualTo

import indigo.shared.EqualTo._
import indigo.shared.QuickCache

final class FontInfo(val fontKey: FontKey, val fontSpriteSheet: FontSpriteSheet, val unknownChar: FontChar, val fontChars: List[FontChar], val caseSensitive: Boolean) {
  import FontInfo._

  private val nonEmptyChars: List[FontChar] = unknownChar +: fontChars

  def addChar(fontChar: FontChar): FontInfo =
    FontInfo(fontKey, fontSpriteSheet, fontChar, nonEmptyChars, caseSensitive)

  def addChars(chars: List[FontChar]): FontInfo =
    FontInfo(fontKey, fontSpriteSheet, unknownChar, fontChars ++ chars, caseSensitive)

  def addChars(chars: FontChar*): FontInfo =
    addChars(chars.toList)

  def findByCharacter(character: String): FontChar =
    QuickCache("char-" + character + "-" + fontKey.key) {
      nonEmptyChars
        .find { p =>
          if (caseSensitive) p.character === character else p.character.toLowerCase === character.toLowerCase
        }
        .getOrElse(unknownChar)
    }
  def findByCharacter(character: Char): FontChar =
    findByCharacter(character.toString)

  def makeCaseSensitive(sensitive: Boolean): FontInfo =
    FontInfo(fontKey, fontSpriteSheet, unknownChar, fontChars, sensitive)

  def isCaseSensitive: FontInfo =
    makeCaseSensitive(true)
  def isCaseInSensitive: FontInfo =
    makeCaseSensitive(false)
}

object FontInfo {

  implicit val fontCharCache: QuickCache[FontChar] = QuickCache.empty

  def apply(fontKey: FontKey, fontSpriteSheet: FontSpriteSheet, unknownChar: FontChar, fontChars: List[FontChar], caseSensitive: Boolean): FontInfo =
    new FontInfo(fontKey, fontSpriteSheet, unknownChar, fontChars, caseSensitive)

  def apply(fontKey: FontKey, imageAssetRef: String, sheetWidth: Int, sheetHeight: Int, unknownChar: FontChar, chars: FontChar*): FontInfo =
    FontInfo(
      fontKey = fontKey,
      fontSpriteSheet = FontSpriteSheet(imageAssetRef, Point(sheetWidth, sheetHeight)),
      unknownChar = unknownChar,
      fontChars = chars.toList,
      caseSensitive = false
    )
}

final class FontKey(val key: String) extends AnyVal
object FontKey {

  implicit def eq(implicit eqS: EqualTo[String]): EqualTo[FontKey] =
    EqualTo.create { (a, b) =>
      eqS.equal(a.key, b.key)
    }

  def apply(key: String): FontKey =
    new FontKey(key)

}

final class FontSpriteSheet(val imageAssetRef: String, val size: Point)
object FontSpriteSheet {
  def apply(imageAssetRef: String, size: Point): FontSpriteSheet =
    new FontSpriteSheet(imageAssetRef, size)
}

final class FontChar(val character: String, val bounds: Rectangle)
object FontChar {

  def apply(character: String, bounds: Rectangle): FontChar =
    new FontChar(character, bounds)

  def apply(character: String, x: Int, y: Int, width: Int, height: Int): FontChar =
    FontChar(character, Rectangle(x, y, width, height))
}

sealed trait TextAlignment
object TextAlignment {
  case object Left   extends TextAlignment
  case object Center extends TextAlignment
  case object Right  extends TextAlignment
}
