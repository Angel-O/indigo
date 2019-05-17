package indigoexts.geometry

import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.datatypes.Point
import indigo.shared.AsString
import indigo.shared.display.Vector2

final class LineSegment(val start: Point, val end: Point) {
  val center: Point =
    Point(
      ((end.x - start.x) / 2) + start.x,
      ((end.y - start.y) / 2) + start.y
    )

  def left: Int   = Math.min(start.x, end.x)
  def right: Int  = Math.max(start.x, end.x)
  def top: Int    = Math.min(start.y, end.y)
  def bottom: Int = Math.max(start.y, end.y)

  def normal: Vector2 =
    LineSegment.calculateNormal(start, end)

  def lineProperties: LineProperties =
    LineSegment.calculateLineComponents(start, end)

  def intersectWith(other: LineSegment): IntersectionResult =
    LineSegment.intersection(this, other)

  def intersectWithLine(other: LineSegment): Boolean =
    LineSegment.intersection(this, other) match {
      case IntersectionResult.NoIntersection =>
        false

      case r @ IntersectionResult.IntersectionPoint(_, _) =>
        val pt = r.toPoint
        containsPoint(pt) && other.containsPoint(pt)

    }

  def containsPoint(point: Point): Boolean =
    LineSegment.lineContainsPoint(this, point, 0.5f)

  def isFacingPoint(point: Point): Boolean =
    LineSegment.isFacingPoint(this, point)

  def asString: String =
    implicitly[AsString[LineSegment]].show(this)

  def ===(other: LineSegment): Boolean =
    implicitly[EqualTo[LineSegment]].equal(this, other)
}

object LineSegment {

  implicit val lsEqualTo: EqualTo[LineSegment] = {
    val eqPt = implicitly[EqualTo[Point]]

    EqualTo.create { (a, b) =>
      eqPt.equal(a.start, b.start) && eqPt.equal(a.end, b.end)
    }
  }

  implicit val lsAsString: AsString[LineSegment] = {
    val s = implicitly[AsString[Point]]

    AsString.create { ls =>
      s"LineSegment(start = ${s.show(ls.start)}, end = ${s.show(ls.end)})"
    }
  }

  def apply(start: Point, end: Point): LineSegment =
    new LineSegment(start, end)

  def apply(x1: Int, y1: Int, x2: Int, y2: Int): LineSegment =
    LineSegment(Point(x1, y1), Point(x2, y2))

  def apply(start: (Int, Int), end: (Int, Int)): LineSegment =
    LineSegment(Point.tuple2ToPoint(start), Point.tuple2ToPoint(end))

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */
  def calculateLineComponents(start: Point, end: Point): LineProperties =
    (start, end) match {
      case (Point(x1, y1), Point(x2, y2)) if x1 === x2 && y1 === y2 =>
        LineProperties.InvalidLine

      case (Point(x1, _), Point(x2, _)) if x1 === x2 =>
        LineProperties.ParallelToAxisY

      case (Point(_, y1), Point(_, y2)) if y1 === y2 =>
        LineProperties.ParallelToAxisX

      case (Point(x1, y1), Point(x2, y2)) =>
        val m: Float = (y2.toFloat - y1.toFloat) / (x2.toFloat - x1.toFloat)

        LineProperties.LineComponents(m, y1 - (m * x1))
    }

  def intersection(l1: LineSegment, l2: LineSegment): IntersectionResult =
    /*
    y-intercept = mx + b (i.e. y = mx + b)
    x-intercept = -b/m   (i.e. x = -b/m where y is moved to 0)
     */
    (l1.lineProperties, l2.lineProperties) match {
      case (LineProperties.LineComponents(m1, b1), LineProperties.LineComponents(m2, b2)) =>
        //x = -b/m
        val x: Float = (b2 - b1) / (m1 - m2)

        //y = mx + b
        val y: Float = (m1 * x) + b1

        IntersectionResult.IntersectionPoint(x, y)

      case (LineProperties.ParallelToAxisX, LineProperties.ParallelToAxisX) =>
        IntersectionResult.NoIntersection

      case (LineProperties.ParallelToAxisY, LineProperties.ParallelToAxisY) =>
        IntersectionResult.NoIntersection

      case (LineProperties.ParallelToAxisX, LineProperties.ParallelToAxisY) =>
        IntersectionResult.IntersectionPoint(l2.start.x.toFloat, l1.start.y.toFloat)

      case (LineProperties.ParallelToAxisY, LineProperties.ParallelToAxisX) =>
        IntersectionResult.IntersectionPoint(l1.start.x.toFloat, l2.start.y.toFloat)

      case (LineProperties.ParallelToAxisX, LineProperties.LineComponents(m, b)) =>
        IntersectionResult.IntersectionPoint(
          x = (-b / m) - l1.start.y.toFloat,
          y = l1.start.y.toFloat
        )

      case (LineProperties.LineComponents(m, b), LineProperties.ParallelToAxisX) =>
        IntersectionResult.IntersectionPoint(
          x = (-b / m) - l2.start.y.toFloat,
          y = l2.start.y.toFloat
        )

      case (LineProperties.ParallelToAxisY, LineProperties.LineComponents(m, b)) =>
        IntersectionResult.IntersectionPoint(
          x = l1.start.x.toFloat,
          y = (m * l1.start.x) + b
        )

      case (LineProperties.LineComponents(m, b), LineProperties.ParallelToAxisY) =>
        IntersectionResult.IntersectionPoint(
          x = l2.start.x.toFloat,
          y = (m * l2.start.x) + b
        )

      case (LineProperties.InvalidLine, LineProperties.InvalidLine) =>
        IntersectionResult.NoIntersection

      case (_, LineProperties.InvalidLine) =>
        IntersectionResult.NoIntersection

      case (LineProperties.InvalidLine, _) =>
        IntersectionResult.NoIntersection

      case _ =>
        IntersectionResult.NoIntersection
    }

  def calculateNormal(start: Point, end: Point): Vector2 =
    normalisePoint(Vector2(-(end.y - start.y).toDouble, (end.x - start.x).toDouble))

  def normalisePoint(vec2: Vector2): Vector2 = {
    val x: Double = vec2.x.toDouble
    val y: Double = vec2.y.toDouble

    Vector2(
      if (x === 0) 0 else (x / Math.abs(x)),
      if (y === 0) 0 else (y / Math.abs(y))
    )
  }

  def lineContainsPoint(lineSegment: LineSegment, point: Point): Boolean =
    lineContainsPoint(lineSegment, point, 0.01f)

  def lineContainsPoint(lineSegment: LineSegment, point: Point, tolerance: Float): Boolean =
    lineSegment.lineProperties match {
      case LineProperties.InvalidLine =>
        false

      case LineProperties.ParallelToAxisX =>
        if (point.y === lineSegment.start.y && point.x >= lineSegment.left && point.x <= lineSegment.right) true
        else false

      case LineProperties.ParallelToAxisY =>
        if (point.x === lineSegment.start.x && point.y >= lineSegment.top && point.y <= lineSegment.bottom) true
        else false

      case LineProperties.LineComponents(m, b) =>
        if (point.x >= lineSegment.left && point.x <= lineSegment.right && point.y >= lineSegment.top && point.y <= lineSegment.bottom) {
          slopeCheck(point.x.toDouble, point.y.toDouble, m, b, tolerance)
        } else false
    }

  def lineContainsCoords(lineSegment: LineSegment, coords: (Double, Double), tolerance: Float): Boolean =
    lineContainsXY(lineSegment, coords._1, coords._2, tolerance)

  def lineContainsXY(lineSegment: LineSegment, x: Double, y: Double, tolerance: Float): Boolean =
    lineSegment.lineProperties match {
      case LineProperties.InvalidLine =>
        false

      case LineProperties.ParallelToAxisX =>
        if (y.toInt === lineSegment.start.y && x.toInt >= lineSegment.left && x.toInt <= lineSegment.right) true
        else false

      case LineProperties.ParallelToAxisY =>
        if (x.toInt === lineSegment.start.x && y.toInt >= lineSegment.top && y.toInt <= lineSegment.bottom) true
        else false

      case LineProperties.LineComponents(m, b) =>
        if (x >= lineSegment.left.toDouble && x <= lineSegment.right.toDouble && y >= lineSegment.top.toDouble && y <= lineSegment.bottom.toDouble)
          slopeCheck(x, y, m, b, tolerance)
        else false
    }

  def slopeCheck(x: Double, y: Double, m: Float, b: Float, tolerance: Float): Boolean = {
    // This is a slope comparison.. Any point on the line should have the same slope as the line.
    val m2: Double =
      if (x === 0) 0
      else (b - y) / (0 - x)

    val mDelta: Double =
      m.toDouble - m2

    mDelta >= -tolerance.toDouble && mDelta <= tolerance.toDouble
  }

  def isFacingPoint(line: LineSegment, point: Point): Boolean =
    (line.normal dot Vector2.fromPoints(point, line.center)) < 0

}

sealed trait LineProperties
object LineProperties {
// y = mx + b
  final case class LineComponents(m: Float, b: Float) extends LineProperties
  case object ParallelToAxisX                         extends LineProperties
  case object ParallelToAxisY                         extends LineProperties
  case object InvalidLine                             extends LineProperties
}

sealed trait IntersectionResult
object IntersectionResult {
  final case class IntersectionPoint(x: Float, y: Float) extends IntersectionResult {
    def toPoint: Point =
      Point(x.toInt, y.toInt)
  }
  case object NoIntersection extends IntersectionResult
}
