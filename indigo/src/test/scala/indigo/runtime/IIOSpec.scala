package indigo.runtime

import org.scalatest.{FunSpec, Matchers}

class IIOSpec extends FunSpec with Matchers {

  describe("creating and destroying IIO monads") {

    it("should allow creation of an IIO") {
      IIO.pure(10).unsafeRun shouldEqual 10
    }

    it("should allow delayed construction of an IIO") {
      IIO.delay(10).unsafeRun() shouldEqual 10
    }

    it("should not evaluate code on creation of an IIO if delayed") {
      val e = new Exception("test")

      IIO.delay(throw e).attemptRun shouldEqual Left(e)
    }

    it("should be able to recover from errors") {
      IIO.pure(10).recover(IIO.pure(50)).unsafeRun() shouldEqual 10
      IIO.raiseError(new Exception("BOOM")).recover(IIO.pure(50)).unsafeRun() shouldEqual 50
    }

  }

  describe("Functor operations") {

    it("should be a functor") {
      IIO.pure(10).map(_ * 10).unsafeRun() shouldEqual 100
    }

    it("should be a monad") {
      IIO.pure(10).flatMap(i => IIO.pure(i * 10)).unsafeRun() shouldEqual 100
    }

    it("should be flattenable") {
      IIO.pure(IIO.pure("hello")).flatten shouldEqual IIO.pure("hello")
    }

  }

  describe("Laws: Pure") {

    // Left identity: return a >>= f ≡ f a
    it("should respect left identity") {
      val a = 10
      val f = (i: Int) => IIO.pure(i)
      assert(IIO.pure(a).flatMap(f) eq f(a))
    }

    // Right identity: m >>= return ≡ m
    it("should respect right identity") {
      val m = IIO.pure(2)
      assert(m.flatMap(x => IIO.pure[Int](x)) eq m)
    }

    // Associativity: (m >>= f) >>= g ≡ m >>= (\x -> f x >>= g)
    it("should respect associativity identity") {
      val m = IIO.pure(3)
      val f = (i: Int) => IIO.pure(s"$i")
      val g = (s: String) => IIO.pure(s.length > 1)

      assert(m.flatMap(f).flatMap(g) eq m.flatMap((x: Int) => f(x).flatMap(g)))
    }

  }

  describe("evaluation") {

    it("should not evaluate a delayed action") {

      val result: IIO[Int] =
        for {
          a <- IIO.delay(10)
          b <- IIO.delay(a * 10)
          c <- IIO.delay(b / 2)
        } yield c

      result match {
        case IIO.Pure(_) =>
          fail("Expected delay, got pure")

        case IIO.RaiseError(_) =>
          fail("Expected delay, got raise error")

        case IIO.Delay(thunk) =>
          thunk() shouldEqual 50
      }

    }

  }

}
