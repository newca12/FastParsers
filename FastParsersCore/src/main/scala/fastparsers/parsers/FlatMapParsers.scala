package fastparsers.parsers

import scala.annotation.compileTimeOnly

/**
 * Created by Eric on 22.04.14.
 * Interface for flatMap combinator
 */
trait FlatMapParsers {

  implicit class flatmapparsers[T](p: Parser[T]) {
    @compileTimeOnly("can’t be used outside FastParser")
    def flatMap[U](f: T ⇒ Parser[U]): Parser[U] = ???

    @compileTimeOnly("can’t be used outside FastParser")
    def >>[U](f: T ⇒ Parser[U]): Parser[U] = ???
  }

}
