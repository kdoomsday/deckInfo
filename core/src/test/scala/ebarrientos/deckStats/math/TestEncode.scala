package ebarrientos.deckStats.math
import utest._

object TestEncode extends TestSuite {
  val tests = Tests {
    "encode eqs newEncode" - {
      val data = Seq(1, 2, 3, 2)
      val m = Seqs.encode(data)
      val m2 = Seqs.newEncode(data)

      assert(m == m2)
    }

    "newEncode works" - {
      val data = Seq(1, 2, 2, 3, 3, 3)
      val m = Seqs.newEncode(data)

      assert(m(1) == 1)
      assert(m(2) == 2)
      assert(m(3) == 3)
    }
  }
}
