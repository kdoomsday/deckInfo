package ebarrientos.deckStats.basics


import utest._
import ebarrientos.deckStats.basics.Supertype.Legendary
import ebarrientos.deckStats.basics.Supertype.isSupertype


object SupertypeTests extends TestSuite {

  val tests = Tests {
    test("basic conversion tests") - {
      test("apply") { assert(Supertype(Legendary.toString()) == Legendary) }
      test("unapply") { assert(Supertype.unapply(Legendary) == Some(Legendary.toString())) }
    }

    test("isSupertype") {
      assert(
        isSupertype(Legendary.toString()),
        !isSupertype("SomethingElse")
      )
    }
  }

}
