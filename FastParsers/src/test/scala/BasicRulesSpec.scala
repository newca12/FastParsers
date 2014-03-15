import org.scalatest._
import FastParsers._
import StreamMarked._

import UnitTests._

class BasicRulesSpec extends FunSuite {

  val parser = FastParser{
    def rule1 = 'b' ~ 'a' ~ rule2// ~ 'b' ~ 'l'
    def rule2 = 'c' ~ 'b'

    def rule3 = ('a' ~ 'b').rep(2,3) ~ 'c'
    def rule4 = phrase(('a' ~ 'b').+)
    def rule5 = phrase(('a' ~ 'b').* ~ 'c')
    def rule6 = phrase(('a' ~ ('b' || 'c')).?)

    def rule10 = range('0','9') ~> rep(range('a','z')) <~ '0'

    def rule12 = guard('a' ~ 'b' ~ 'c') ~ rep(range('a','z'))
    def rule13 = not('a' ~ 'b' ~ 'c') ~ rep(range('a','z'))


    def rule18 = phrase(rep('a',3,3) | (rep('a',2,2) ~ 'b') ^^ {case (x:List[Char],y:Char) => x ++ List(y)})
    def rule19 = phrase(rep('a',0,3)) | phrase(rep('a',0,4))
    def rule20 = phrase((rep('a',0,3) ~ 'b') ^^ {case (x:List[Char],y:Char) => x ++ List(y)} | rep('a' || 'b'))

    def rule21 = ((rep(range('a','z'))) filter {case x:List[_] => x.mkString == "salut" || x.mkString == "hello"})

    def rule22 = repFold(range('0','9'))(0){(y:Int,x:Char) => x.asDigit +y}



    def rule27 = rep('a' ~ 'b') ~ rep('a')
    def rule28 = rep('a') ~ 'b'
    /*def rule29 = seq(List('s','a','l','u','t'))
    def rule30 = seq("salut")   */

    def rule31 = 'a' ~ ('b' withFailureMessage("JE VEUX UN 'b' ICI")) ~ 'c'

    def rule32 = range('0','9')
    def rule33 = rep(rule32,3,3)
    def rule34 = rule33 ~ rule33

    def rule35 = rep(wildcard[Char])

    def rule36 = -('a' ~ 'b') ~ rule35
  }

  test("Rule1 test") {
    shouldSucced(parser.rule1){
      "bacb" gives ('b','a','c','b')
    }
    shouldFail(parser.rule1){
      "bbacb"
    }
  }
  test("Rule3 test") {             //('a' ~ 'b').rep(2,3) ~ 'c'
    shouldSucced(parser.rule3)(
      "ababc" gives (repeat(('a','b'),2),'c'),
      "abababc" gives (repeat(('a','b'),3),'c')
    )
    shouldFail(parser.rule3) (
       "a", "ab", "abc","ababababc","ababab","abababac"
    )
  }
  test("Rule4 test") {  //def rule4 = ('a' ~ 'b').+
    shouldSucced(parser.rule4)(
      "ab" gives List(('a','b')),  "abab" gives repeat(('a','b'),2),  "ababab" gives repeat(('a','b'),3)
    )
    shouldFail(parser.rule4) (
      "", "a","b","aba","ababa"
    )
  }

  test("Rule5 test") {  //('a' ~ 'b').* ~ 'c'
    shouldSucced(parser.rule5)(
      "c" gives (Nil,'c'),  "abc" gives (List(('a','b')),'c'),  "ababababc" gives (repeat(('a','b'),4),'c')
    )
    shouldFail(parser.rule5) (
      "", "ab","abcab"
    )
  }

  test("Rule6 test") {  //('a' ~ ('b' || 'c')).?
    shouldSucced(parser.rule6)(
      "" gives Nil,"ab" gives List(('a','b')),  "ac" gives List(('a','c'))
    )
    shouldFail(parser.rule6) (
      "ad", "acab","b","aba"
    )
  }

  test("Rule10 test") {  //range('0','9') ~> rep(range('a','z')) <~ '0'
    shouldSucced(parser.rule10)(
      "00" gives Nil,"0a0" gives List('a'),  "7abcd0" gives List('a','b','c','d')
    )
    shouldFail(parser.rule10) (
      "", "5","b","aba" ,"7abcd"
    )
  }

  test("Rule12 test") {  //guard('a' ~ 'b' ~ 'c') ~ rep(range('a','z'))
    shouldSucced(parser.rule12)(
      "abc" gives List('a','b','c'),"abcdef" gives List('a','b','c','d','e','f'),  "abc7" gives List('a','b','c')
    )
    shouldFail(parser.rule12) (
      "", "acc","v","sadsa"
    )
  }

  test("Rule13 test") {  //not('a' ~ 'b' ~ 'c') ~ rep(range('a','z'))
    shouldSucced(parser.rule13)(
      "" gives Nil,"abd" gives List('a','b','d'),"abddef" gives List('a','b','d','d','e','f'),  "dbd7" gives List('d','b','d')
    )
    shouldFail(parser.rule13) (
      "abc", "abcadsdsaf","abca5435"
    )
  }

  test("Rule18 test") {  //phrase(rep('a',3,3) | (rep('a',2,2) ~ 'b') ^^ {case (x:List[Char],y:Char) => x ++ List(y)})
    shouldSucced(parser.rule18)(
      "aaa" gives List('a','a','a'),"aab" gives List('a','a','b')
    )
    shouldFail(parser.rule18) (
      "aa", "aaaa"
    )
  }

  test("Rule19 test") {  //phrase(rep('a',0,3)) | rep('a',0,4))
    shouldSucced(parser.rule19)(
      "" gives Nil, "a" gives List('a'), "aa" gives List('a','a'), "aaa" gives List('a','a','a'),"aaaa" gives List('a','a','a','a')
    )
    shouldFail(parser.rule19) (
      "aaaaa"
    )
  }
  test("Rule20 test") {  //phrase((rep('a',0,3) ~ 'b') ^^ {case (x:List[Char],y:Char) => x ++ List(y)} | rep('a' || 'b'))
    shouldSucced(parser.rule20)(
      "b" gives List('b'),
      "ab" gives List('a','b'),
      "aab" gives List('a','a','b'),
      "aaab" gives List('a','a','a','b'),
      "" gives Nil,
      //"babba" gives List('b','a','b','b','a'),
      "aaaab" gives List('a','a','a','a','b')

    )
    shouldFail(parser.rule20) (
      "aba","cababds", "babba"
    )
  }

  test("Rule21 test") {  //((rep(range('a','z'))) filter {case x:List[_] => x.mkString == "salut" || x.mkString == "hello"})
    shouldSucced(parser.rule21)(
      "salut" gives List('s','a','l','u','t'),
      "hello" gives List('h','e','l','l','o')

    )
    shouldFail(parser.rule21) (
      "","salutt", "asalut","hellut"
    )
  }
  //def rule23 = range('0','9').repFold(1){(y:Int,x:Char) => x.asDigit * y}

  test("Rule22 test") {  //repFold(range('0','9'))(0){(y:Int,x:Char) => x.asDigit +y}
    shouldSucced(parser.rule22)(
      "123" gives 6,"" gives 0,"b" gives 0, "99" gives 18
    )
  }

  test("Rule34 test") {  //rule33 ~ rule33  -> rep(rep(0 - 9,3),2)
    shouldSucced(parser.rule34)(
      "123321" gives (List('1','2','3'),List('3','2','1'))
    )
    shouldFail(parser.rule34) (
      "1","12", "543","24334"
    )
  }

  test("Rule36 test") {  // -('a' ~ 'b') ~ rule35
    shouldSucced(parser.rule36)(
      "absaf9.h" gives List('s','a','f','9','.','h')
    )
    shouldFail(parser.rule36) (
      "ac","dfghjg",""
    )
  }
}