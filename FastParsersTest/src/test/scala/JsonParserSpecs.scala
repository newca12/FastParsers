import JsonParsers._
import TestsHelper._
import org.scalatest.FunSuite

/**
 * Created by Eric on 05.04.14.
 */
class JsonParserSpecs  extends FunSuite {
  (1 to 20).foreach{ i =>
    test("jsonparser " + "FastParsersTest/src/test/resources/json" + i){
      compareImplementations("FastParsersTest/src/test/resources/json" + i,
        JSonImpl1.jsonparser.value,
        JSON,
        x => JSON.parse(JSON.value, x))
    }
  }
  test("jsonparser " + "FastParsersTest/src/test/resources/json.big" + 1){
    compareImplementations("FastParsersTest/src/test/resources/json.big" + 1,
      JSonImpl1.jsonparser.value,
      JSON,
      x => JSON.parse(JSON.value, x))
  }
}
