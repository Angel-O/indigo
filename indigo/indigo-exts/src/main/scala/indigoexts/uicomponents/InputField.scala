package indigoexts.uicomponents

import indigo.shared.time.GameTime
import indigo.shared.constants.Keys
import indigo.shared.events.InputState
import indigo.shared.datatypes._
import indigo.shared.scenegraph.{Graphic, SceneUpdateFragment, Text}

import indigo.shared.EqualTo._
import indigo.shared.temporal.Signal
import indigo.shared.BoundaryLocator
import scala.collection.immutable.Nil
import scala.annotation.tailrec
import indigo.shared.time.Seconds
import indigo.shared.constants.Key
import indigo.shared.time.Millis

final case class InputField(
    text: String,
    characterLimit: Int,
    multiLine: Boolean,
    assets: InputFieldAssets,
    cursorPulseRate: Option[Seconds],
    position: Point,
    depth: Depth,
    hasFocus: Boolean,
    cursorPosition: Int
) {

  def giveFocus: InputField =
    this.copy(
      hasFocus = true,
      cursorPosition = this.text.length
    )

  def loseFocus: InputField =
    this.copy(
      hasFocus = false
    )

  def withCharacterLimit(limit: Int): InputField =
    this.copy(characterLimit = limit)

  def makeMultiLine: InputField =
    this.copy(multiLine = true)

  def makeSingleLine: InputField =
    this.copy(multiLine = false)

  def cursorLeft: InputField =
    this.copy(cursorPosition = if (cursorPosition - 1 >= 0) cursorPosition - 1 else cursorPosition)

  def cursorRight: InputField =
    this.copy(cursorPosition = if (cursorPosition + 1 <= text.length) cursorPosition + 1 else text.length)

  def cursorHome: InputField =
    this.copy(cursorPosition = 0)

  def cursorEnd: InputField =
    this.copy(cursorPosition = text.length)

  def delete: InputField =
    if (cursorPosition === text.length()) this
    else {
      val splitString = text.splitAt(cursorPosition)
      copy(text = splitString._1 + splitString._2.substring(1))
    }

  def backspace: InputField = {
    val splitString = text.splitAt(cursorPosition)

    this.copy(
      text = splitString._1.take(splitString._1.length - 1) + splitString._2,
      cursorPosition = if (cursorPosition > 0) cursorPosition - 1 else cursorPosition
    )
  }

  def addCharacter(char: Char): InputField =
    addCharacterText(char.toString())

  def addCharacterText(textToInsert: String): InputField = {
    @tailrec
    def rec(remaining: List[Char], textHead: String, textTail: String, position: Int): InputField =
      remaining match {
        case Nil =>
          this.copy(
            text = textHead + textTail,
            cursorPosition = position
          )

        case _ if (textHead + textTail).length >= characterLimit =>
          rec(Nil, textHead, textTail, position)

        case c :: cs if (c !== '\n') || multiLine =>
          rec(cs, textHead + c.toString(), textTail, position + 1)

        case _ :: cs =>
          rec(cs, textHead, textTail, position)
      }

    val splitString = text.splitAt(cursorPosition)

    rec(textToInsert.toCharArray().toList, splitString._1, splitString._2, cursorPosition)
  }

  def update(inputState: InputState, boundaryLocator: BoundaryLocator): InputField = {
    @tailrec
    def rec(keysReleased: List[Key], acc: InputField): InputField =
      keysReleased match {
        case Nil =>
          acc

        case Keys.BACKSPACE :: ks =>
          rec(ks, acc.backspace)

        case Keys.DELETE :: ks =>
          rec(ks, acc.delete)

        case Keys.LEFT_ARROW :: ks =>
          rec(ks, acc.cursorLeft)

        case Keys.RIGHT_ARROW :: ks =>
          rec(ks, acc.cursorRight)

        case Keys.HOME :: ks =>
          rec(ks, acc.cursorHome)

        case Keys.END :: ks =>
          rec(ks, acc.cursorEnd)

        case Keys.ENTER :: ks =>
          rec(ks, acc.addCharacterText(Keys.ENTER.key))

        case key :: ks if hasFocus && key.isPrintable =>
          rec(ks, acc.addCharacterText(key.key))

        case _ :: ks =>
          rec(ks, acc)
      }

    val updated = rec(inputState.keyboard.keysReleased, this)

    if (inputState.mouse.mouseReleased)
      if (inputState.mouse.wasMouseUpWithin(assets.text.bounds(boundaryLocator)))
        updated.giveFocus
      else updated.loseFocus
    else updated
  }

  def draw(
      gameTime: GameTime,
      boundaryLocator: BoundaryLocator
  ): SceneUpdateFragment = {
    val field =
      assets.text
        .withText(this.text)
        .moveTo(position)
        .withDepth(depth)

    val sceneUpdateFragment =
      SceneUpdateFragment.empty
        .addUiLayerNodes(field)

    if (hasFocus) {
      val cursorPositionPoint =
        boundaryLocator
          .textAsLinesWithBounds(field.text.substring(0, cursorPosition), field.fontKey)
          .reverse
          .headOption
          .map(_.lineBounds.topRight + position)
          .getOrElse(position)

      cursorPulseRate match {
        case None =>
          sceneUpdateFragment
            .addUiLayerNodes(
              assets.cursor
                .moveTo(cursorPositionPoint)
                .withDepth(Depth(-(depth.zIndex + 100000)))
            )

        case Some(seconds) =>
          Signal
            .Pulse(seconds)
            .map {
              case false =>
                sceneUpdateFragment

              case true =>
                sceneUpdateFragment
                  .addUiLayerNodes(
                    assets.cursor
                      .moveTo(cursorPositionPoint)
                      .withDepth(Depth(-(depth.zIndex + 100000)))
                  )
            }
            .at(gameTime.running)
      }

    } else sceneUpdateFragment
  }

}

object InputField {

  def apply(text: String, assets: InputFieldAssets): InputField =
    InputField(text, 255, false, assets, Some(Millis(400).toSeconds), Point.zero, Depth(1), false, text.length())

  def apply(text: String, characterLimit: Int, multiLine: Boolean, assets: InputFieldAssets): InputField =
    InputField(text, characterLimit, multiLine, assets, Some(Millis(400).toSeconds), Point.zero, Depth(1), false, text.length())

}

final case class InputFieldAssets(text: Text, cursor: Graphic)
