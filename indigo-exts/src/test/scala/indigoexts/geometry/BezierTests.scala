package indigoexts.geometry

import utest._
import indigo.shared.datatypes.Point
import indigo.EqualTo._
import indigo.shared.time.Millis

object BezierTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Interpolation" - {
        Bezier.interpolate(Point(0, 0), Point(10, 10), 0d) === Point(0, 0) ==> true
        Bezier.interpolate(Point(0, 0), Point(10, 10), 0.5d) === Point(5, 5) ==> true
        Bezier.interpolate(Point(0, 0), Point(10, 10), 1d) === Point(10, 10) ==> true
      }

      "Reduction" - {

        "Empty list" - {
          Bezier.reduce(Nil, 0d) === Point.zero ==> true
        }

        "one point" - {
          Bezier.reduce(List(Point(1, 1)), 0d) === Point(1, 1) ==> true
        }

        "two points" - {
          Bezier.reduce(List(Point(0, 0), Point(10, 10)), 0d) === Point(0, 0) ==> true
          Bezier.reduce(List(Point(0, 0), Point(10, 10)), 0.5d) === Point(5, 5) ==> true
          Bezier.reduce(List(Point(0, 0), Point(10, 10)), 1d) === Point(10, 10) ==> true
        }

        "three points" - {
          Bezier.reduce(List(Point(0, 0), Point(5, 5), Point(10, 0)), 0d) === Point(0, 0) ==> true
          Bezier.reduce(List(Point(0, 0), Point(5, 5), Point(10, 0)), 0.5d) === Point(4, 2) ==> true
          Bezier.reduce(List(Point(0, 0), Point(5, 5), Point(10, 0)), 1d) === Point(10, 0) ==> true
        }

      }

      "One dimensional (1 point)" - {

        val bezier =
          Bezier(Point(5, 5))

        bezier.at(0d) === Point(5, 5) ==> true
        bezier.at(0.5d) === Point(5, 5) ==> true
        bezier.at(1d) === Point(5, 5) ==> true

      }

      "Linear (2 points)" - {

        val bezier =
          Bezier(Point(0, 0), Point(10, 10))

        bezier.at(-50d) === Point(0, 0) ==> true
        bezier.at(0d) === Point(0, 0) ==> true
        bezier.at(0.25d) === Point(2, 2) ==> true
        bezier.at(0.5d) === Point(5, 5) ==> true
        bezier.at(0.75d) === Point(7, 7) ==> true
        bezier.at(1d) === Point(10, 10) ==> true
        bezier.at(100d) === Point(10, 10) ==> true

      }

      "Quadtratic (3 points)" - {

        val bezier =
          Bezier(Point(2, 2), Point(4, 7), Point(20, 10))

        /*
          For 0.5d:

          2,2 4,7 20,10
          (2,2 4,7) (4,7 20,10)
          3,4 12,8
          7,6

         */

        bezier.at(0d) === Point(2, 2) ==> true
        bezier.at(0.5d) === Point(7, 6) ==> true
        bezier.at(1d) === Point(20, 10) ==> true

      }

      "Higher-Order (4 or more points)" - {

        val bezier =
          Bezier(Point(2, 2), Point(4, 7), Point(20, 10), Point(3, 100))

        /*
          For 0.5d:

          2,2 4,7 20,10 3,100
          (2,2 4,7) (4,7 20,10) (20,10 3,100)
          (3,4 12,8) (12,8 [((3-20)/2)+20 = 11],55)
          7,6 11,31

          [((11-7)/2)+7 = 9] , ((31-6)/2)+6 = 18
          0,12

         */

        bezier.at(0d) === Point(2, 2) ==> true
        bezier.at(0.5d) === Point(9, 18) ==> true
        bezier.at(1d) === Point(3, 100) ==> true

      }

      "to points" - {

        "linear" - {
          val bezier =
            Bezier(Point.zero, Point(100, 0))

          val actual = bezier.toPoints(10)

          val expected =
            List(
              Point(0, 0),
              Point(10, 0),
              Point(20, 0),
              Point(30, 0),
              Point(40, 0),
              Point(50, 0),
              Point(60, 0),
              Point(70, 0),
              Point(80, 0),
              Point(90, 0),
              Point(100, 0)
            )

          actual === expected ==> true
        }

        "higher order" - {
          val bezier =
            Bezier(Point(2, 2), Point(4, 7), Point(20, 10), Point(3, 100))

          val actual = bezier.toPoints(2)

          val expected =
            List(
              Point(2, 2),
              Point(9, 18),
              Point(3, 100)
            )

          actual === expected ==> true
        }
      }

      "to polygon" - {

        "linear" - {
          val bezier =
            Bezier(Point.zero, Point(100, 0))

          val actual: Int = bezier.toPolygon(10).edgeCount

          val expected: Int = 10

          actual ==> expected
        }

      }

      "to line segments" - {
        val bezier =
          Bezier(Point(2, 2), Point(4, 7), Point(20, 10), Point(3, 100))

        val lineSegments = bezier.toLineSegments(2)

        lineSegments.length ==> 2
        lineSegments(0) === LineSegment(Point(2, 2), Point(9, 18)) ==> true
        lineSegments(1) === LineSegment(Point(9, 18), Point(3, 100)) ==> true
      }

      "to signal" - {
        val bezier =
          Bezier(Point(2, 2), Point(4, 7), Point(20, 10), Point(3, 100))

        val signal =
          bezier.toSignal(Millis(1500))

        signal.at(Millis(0)) === Point(2, 2) ==> true
        signal.at(Millis(750)) === Point(9, 18) ==> true
        signal.at(Millis(1500)) === Point(3, 100) ==> true
      }

    }

}
