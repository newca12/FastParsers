import JsonParsers._
import TestsHelper._
import org.scalatest.FunSuite

/**
 * Created by Eric on 05.04.14.
 */
class JsonParserSpecs  extends FunSuite {
  (1 to 20).foreach{ i =>
    test("jsonparser " + "FastParsers\\src\\test\\resources\\json" + i){
      compareImplementations("FastParsers\\src\\test\\resources\\json" + i,
        jsonparser.value,
        JSON,
        x => JSON.parse(JSON.value, x))
    }
  }
}
