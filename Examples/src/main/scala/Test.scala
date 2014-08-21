/**
 * Created with IntelliJ IDEA.
 * User: Eric
 * Date: 12.02.14
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */

//because warnings

import fastparsers.framework.getAST
import fastparsers.framework.implementations.{ FastParsers, FastArrayParsers }
import fastparsers.framework.parseresult._
import fastparsers.input.InputWindow
import fastparsers.parsers.Parser
import scala.collection.mutable.HashMap
import scala.language.reflectiveCalls
import scala.language.implicitConversions
import scala.reflect.ClassTag

object Test {

  def main(args: Array[String]) {

    import FastParsers._
    val parser = FastParser {
      /*def rule = 'a' ~ 'c'
    def rule2(p: Parser[List[Char]]) = 'a' ~ p
    def rule3(y: Int) = rule2(repN('b', y))

    def rule1(p: Parser[List[Char]], y: Int): Parser[Any] = 'a' ~ p ~ rule2(y)
    def rule2(x: Int): Parser[Any]  = rule1(repN('c', x), x + 1) | 'b'*/
      def rule1 = 'a' ~ 'b'

    }

    parser.rule1("ab  cacccb") match {
      case Success(x) ⇒
        println(x)
      case Failure(msg) ⇒ println("failure : " + msg)
    }
  }
}