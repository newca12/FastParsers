package fastparsers.parsers

import fastparsers.input._
import fastparsers.framework._

/**
 * Expansion of Basic combinators
 */
trait BaseParsersImpl extends ParserImplHelper {
  self: ParseInput =>

  import c.universe._

  override def expand(tree: c.Tree, rs: ResultsStruct) = tree match {
    case q"$_.baseParsers[$d]($a)" => expand(a, rs)
    case q"$_.toElem(($a,$b))"     => parseRange(a, b, rs)
    case q"$_.toElem($elem)"       => parseElem(elem, rs)
    case q"$_.elemParser($elem)"   => parseElem(elem, rs)
    case q"$_.range($a,$b)"        => parseRange(a, b, rs)
    case q"$_.accept(..$a)"        => parseAccept(a,negate = false,rs)
    case q"$_.not(..$a)"           => parseAccept(a,negate = true,rs)
    case q"$_.acceptIf($f)"        => parseAcceptIf(f, rs)
    case q"$_.wildcard"            => parseWildcard(rs)
    case q"$_.guard[$d]($a)"       => parseGuard(a, d, rs)
    case q"$_.takeWhile($f)"       => parseTakeWhile(f, rs)
    case q"$_.take($n)"            => parseTake(n, rs)
    case q"$_.raw[$d]($a)"         => parseRaw(a,rs)
    case q"$_.phrase[$d]($a)"      => parsePhrase(a, rs)
    case q"$_.failure($a)"         => parseFailure(a,rs)
    case q"$_.success[$d]($a)"     => parseSuccess(a,d,rs)
    case q"$_.position"            => parsePosition(rs)
    case q"$_.positioned[$d]($a)"  => parsePositioned(a,d,rs)
    case q"$a ~[$d] $b"                     => parseThen(a, b, rs)
    case q"$a ~>[$d] $b"                    => parseIgnoreLeft(a, b, d, rs)
    case q"$a <~[$d] $b"                    => parseIgnoreRight(a, b, d, rs)
    case q"$a ||[$d] $b"                    => parseOr(a, b, d, rs)
    case q"$a |[$d] $b"                     => parseOr(a, b, d, rs)
    case q"$a ^^[$d] $f"                    => parseMap(a, f, d, rs)
    case q"$a map[$d] $f"                   => parseMap(a, f, d, rs)
    case q"$a ^^^[$d] $v"                   => parseValue(a, v, d, rs)
    case q"$a filter[$d] $f"                => parseFilter(a, f, d, rs)
    case q"$a withFailureMessage $msg"      => parseWithFailureMessage(a, msg, rs)
    case q"call[$d](${ruleCall: String},..$params)" => parseRuleCall(q"${TermName(ruleCall)}", params, d, rs)   //TODO needed?
    case q"compound[$d]($a)"                => parseCompound(a, d, rs)      //TODO needed?
    case q"foreignCall[$d]($obj,${ruleCall: String},..$params)" => parseRuleCall(q"$obj.${TermName(ruleCall)}", params, d, rs)
    case q"$_.call[$d](${ruleCall: String},..$params)" => parseRuleCall(q"${TermName(ruleCall)}", params, d, rs)
    case q"$_.compound[$d]($a)"                => parseCompound(a, d, rs)
    case q"$_.foreignCall[$d]($obj,${ruleCall: String},..$params)" => parseRuleCall(q"$obj.${TermName(ruleCall)}", params, d, rs)
    case q"if ($cond) $a else $b"           =>
      c.typecheck(tree).tpe match {
        case TypeRef(_, _, List(d)) => parseIfThnEls(cond,a,b,q"$d",rs)
        case x => c.abort(c.enclosingPosition,"Ill formed if, type must be fastparsers.parsers.Parser[_] it is now : " + show(x))
      }
    case _                                  => super.expand(tree, rs) //q"""println(show(reify($tree).tree))"""
  }

  override def prettyPrint(tree: c.Tree) = tree match {
    case q"$_.baseParsers[$d]($a)" => prettyPrint(a)
    case q"$_.toElem(($a,$b))"     => "(" + a.toString + ", " + b.toString + ")"
    case q"$_.toElem($elem)"       => elem.toString
    case q"$_.elemParser($elem)"   => elem.toString
    case q"$_.accept(..$a)"        => "accept(" + a.map(prettyPrint(_)) + ")"
    case q"$_.not(..$a)"           => "not(" + a.map(prettyPrint(_)) + ")"
    case q"$_.acceptIf($f)"        => "acceptIf(" + prettyPrint(f) + ")"
    case q"$_.wildcard"            => "wildcard"
    case q"$_.guard[$d]($a)"       => "guard(" + prettyPrint(a) + ")"
    case q"$_.takeWhile($f)"       => "takeWhile(" + prettyPrint(f) + ")"
    case q"$_.take($n)"            => "take(" + show(n) + ")"
    case q"$_.raw[$d]($a)"         => "raw(" + prettyPrint(a) + ")"
    case q"$_.phrase[$d]($a)"      => "phrase(" + prettyPrint(a) + ")"
    case q"$_.failure($a)"         => "failure(" + prettyPrint(a) + ")"
    case q"$_.success[$d]($a)"     => "success(" + prettyPrint(a) + ")"
    case q"$_.position"            => "position"
    case q"$_.positioned[$d]($a)"  => "positioned(" + prettyPrint(a) + ")"
    case q"$a ~[$d] $b"                     => "(" + prettyPrint(a) + " ~ " + prettyPrint(b) + ")"
    case q"$a ~>[$d] $b"                    => "(" + prettyPrint(a) + " ~> " + prettyPrint(b) + ")"
    case q"$a <~[$d] $b"                    => "(" + prettyPrint(a) + " <~ " + prettyPrint(b) + ")"
    case q"$a ||[$d] $b"                    => prettyPrint(a) + " | " + prettyPrint(b)
    case q"$a |[$d] $b"                     => prettyPrint(a) + " | " + prettyPrint(b)
    case q"$a ^^[$d] $f"                    => prettyPrint(a) + " ^^(" + prettyPrint(f) + ")"
    case q"$a map[$d] $f"                   => prettyPrint(a) + " map (" + prettyPrint(f) + ")"
    case q"$a ^^^[$d] $v"                   => prettyPrint(a) + " ^^^ (" + prettyPrint(v) + ")"
    case q"$a filter[$d] $f"                => prettyPrint(a) + " filter (" + prettyPrint(f) + ")"
    case q"$a withFailureMessage $msg"      => prettyPrint(a) + " withFailureMessage (" + show(msg) + ")"
    case q"call[$d]($rule,..$params)"       => show(rule)
    case q"compound[$d]($a)"                => "(" + prettyPrint(a) + ")"
    case q"if ($cond) $a else $b"           => "if (" + prettyPrint(cond) + ")" + prettyPrint(a) + " else " + prettyPrint(b)
      case q"$_.toElemOrRange(($x,$y))"     => "(" + show(x) + ", " + show(y) + ")"
    case q"$_.toElemOrRange($x)"            => show(x)
    case _                                  => super.prettyPrint(tree)
  }


  private def parseElem(a: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    q"""
      if ($isNEOI && $currentInput == $a){
        $result = $a
        $advance
        success = true
       }
       else {
          success = false
          msg = "expected '" + $a + " at " + $pos
        }
     """
  }

  private def parseRange(a: c.Tree, b: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    q"""
     if ($isNEOI && $currentInput >= $a && $currentInput <= $b){
      $result = $currentInput
      $advance
      success = true
     }
     else {
        success = false
        msg = "expected in range ('" + $a + "', '" + $b + "')  at " + $pos
     }
    """
  }

  private def parseAccept(a: List[c.Tree],negate: Boolean,rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    val ranges = if (negate)  q"!(${getAcceptedElem(a)})"
                 else q"(${getAcceptedElem(a)})"
    q"""
      if ($isNEOI && $ranges){
        $result = $currentInput
        $advance
        success = true
       }
       else {
          success = false
          msg = "expected element in " + ${a.map(prettyPrint(_)).mkString} + " at " + $pos
        }
     """
  }

  private def getAcceptedElem(elems: List[c.Tree]) = {
    def acceptElem(elem: c.Tree) = elem match {
      case q"$_.toElemOrRange(($x,$y))"  => q"($currentInput >= $x && $currentInput <= $y)"
      case q"$_.toElemOrRange($x)"       => q"$currentInput == $x"
    }
    elems.tail.foldLeft(q"${acceptElem(elems.head)}"){(acc, c) => q"$acc || ${acceptElem(c)}"}
  }

  private def parseAcceptIf(f: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    q"""
     if ($isNEOI && $f($currentInput)){
      $result = $currentInput
      $advance
      success = true
     }
     else {
        success = false
        msg = "acceptIf combinator failed at " + $pos
     }
     """
  }

  private def parseWildcard(rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputElemType, true))
    q"""
      if ($isNEOI){
        $result = $currentInput
        $advance
        success = true
      }
      else
        success = false

    """
  }


  private def parseGuard(a: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val tree = mark {
      rollback =>
        q"""
         ${expand(a, rs)}
         $rollback
       """
    }
    tree
  }


  private def parseTakeWhile(f: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val tmp_f = TermName(c.freshName)
    val beginpos = TermName(c.freshName)
    rs.append((result, inputType, true))
    q"""
      val $tmp_f = $f
      val $beginpos = $pos
      while ($isNEOI && $tmp_f($currentInput))
        $advance
      $result = ${slice(q"$beginpos", q"$pos")}
      success = true
    """
  }

  private def parseTake(n: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, inputType, true))
    q"""
    if ($pos + $n <= $inputsize) {
      success = true
      $result = ${slice(pos, q"$pos + $n")}
    }
    else {
      success = false
      msg = "take(" + $n + ") cannot proceed, only " + ($inputsize - $pos) + " elements left at " + $pos
    }
    """
  }

  private def parseRaw(a: c.Tree, rs: ResultsStruct) = {
    val beginpos = TermName(c.freshName)
    val result = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    //${slice(q"$beginpos", q"$pos")}
    val tree = q"""
      val $beginpos = $pos
      ${expand(a,results_tmp)}
      if (success) {
        $result = new fastparsers.input.InputWindow.InputWindow(fastparsers.input,$beginpos,$pos)
      }
      else {
        msg = "raw(" + ${prettyPrint(a)} +  ") failure at " + $pos
      }
    """
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, tq"fastparsers.input.InputWindow.InputWindow[$inputType]", true))
    tree
  }

  private def parsePhrase(a: c.Tree, rs: ResultsStruct) = {
    q"""
    ${expand(a, rs)}
    if (success) {
      if (!$isEOI){
        success = false
        msg = "not all the input is consummed, at pos " + $pos
      }
    }
    """
  }


  private def parseFailure(a: c.Tree, rs: ResultsStruct) = {
    q"""
      success = false
      msg = $a
    """
  }
  private def  parseSuccess(a: c.Tree,typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, typ, true))
    q"""
      success = true
      $result = $a
    """
  }

  private def parsePosition(rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    rs.append((result, q"Int", true))
    q"$result = $pos"
  }

  private def parsePositioned(a: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val beginpos = TermName(c.freshName)
    val result = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    //if ($result.pos == NoPosition)
    val tree =
    q"""
     val $beginpos = $pos
     ${expand(a,results_tmp)}
     if (success) {
        $result = ${combineResults(results_tmp)}
          $result setPos ${getPositionned(q"$beginpos")}
     }
     """
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, typ, true))
    tree
  }


  private def parseThen(a: c.Tree, b: c.Tree, rs: ResultsStruct) = {
    q"""
      ${expand(a, rs)}
      if (success) {
        ${expand(b, rs)}
      }
   """
  }

  private def parseIgnoreLeft(a: c.Tree, b: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val results_tmp = new ResultsStruct()
    val tree =
      q"""
      ${expand(a, results_tmp)}
      if (success) {
        ${expand(b, rs)}
      }
     """
    results_tmp.setNoUse
    rs.append(results_tmp)
    tree
  }

  private def parseIgnoreRight(a: c.Tree, b: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val results_tmp = new ResultsStruct()
    val tree =
      q"""
      ${expand(a, rs)}
      if (success) {
        ${expand(b, results_tmp)}
      }
     """
    results_tmp.setNoUse
    rs.append(results_tmp)
    tree
  }

  def parseOr(a: c.Tree, b: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    var results_tmp1 = new ResultsStruct()
    var results_tmp2 = new ResultsStruct()
    val tree = mark {
      rollback =>
        q"""
          ${expand(a, results_tmp1)}
          if (!success) {
            $rollback
            ${expand(b, results_tmp2)}
            if (success)
              $result = ${combineResults(results_tmp2)}
          }
          else {
            $result = ${combineResults(results_tmp1)}
          }
        """
    }

    results_tmp1.setNoUse
    results_tmp2.setNoUse
    rs.append((result, typ, true)) //tq"typ"    Ident(TypeName("Any"))
    rs.append(results_tmp1)
    rs.append(results_tmp2)
    tree
  }

  private def parseMap(a: c.Tree, f: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val tmp_f = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    val tree =
      q"""
      val $tmp_f = $f
      ${expand(a, results_tmp)}
       if (success)
         $result = $tmp_f.apply(${combineResults(results_tmp)})
      """
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, typ, true))
    c.untypecheck(tree)
  }

  private def parseValue(a: c.Tree, v: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    val tree =
      q"""
      ${expand(a, results_tmp)}
       if (success)
         $result = $v
      """
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, typ, true))
    tree
  }

  private def parseFilter(a: c.Tree, f: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val tmp_f = TermName(c.freshName)
    val results_tmp = new ResultsStruct()
    val tree = mark {
      rollback =>
        q"""
      val $tmp_f = $f
      ${expand(a, results_tmp)}
       if (success && $tmp_f(${combineResults(results_tmp)}))
         $result = ${combineResults(results_tmp)}
       else {
        success = false
        msg = "incorrect result for " + ${prettyPrint(a)} + ".filter at " + $pos
        $rollback
       }
      """
    }
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, typ, true))
    c.untypecheck(tree)
  }

  private def parseWithFailureMessage(a: c.Tree, msg: c.Tree, rs: ResultsStruct) = {
    q"""
     ${expand(a, rs)}
      if (!success)
         msg = $msg
    """
  }

  private def parseRuleCall(ruleCall: c.Tree, params: List[c.Tree], typ: c.Tree, rs: ResultsStruct) = {
    val callResult = TermName(c.freshName)
    val result = TermName(c.freshName)

    // val $callResult = $ruleCall(fastparsers.input.substring($offset),0)
    //${advanceTo(q"$callResult.inputPos")}
    val call = params match {
      case Nil => q"$ruleCall(input,$pos)"
      case p => q"$ruleCall(input,..$p,$pos)"
    }
    val tree = q"""
        val $callResult = $call
        success = $callResult.success
        if (success){
          ${setpos(q"$callResult.inputPos")}
          $result = $callResult.result
         }
        else
          msg = $callResult.msg
        """
    rs.append((result, typ, true))
    c.untypecheck(tree)
  }

  private def parseCompound(a: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    var results_tmp = new ResultsStruct()
    val tree =
      q"""
        ${expand(a, results_tmp)}
        $result = ${combineResults(results_tmp)}
      """
    results_tmp.setNoUse
    rs.append((result, typ, true))
    rs.append(results_tmp)
    tree
  }

  private def parseIfThnEls(cond: c.Tree,a: c.Tree,b: c.Tree,typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    var results_tmp1 = new ResultsStruct()
    var results_tmp2 = new ResultsStruct()
    val tree =
    q"""
    if ($cond){
      ${expand(a,results_tmp1)}
      $result = ${combineResults(results_tmp1)}
     }
    else {
      ${expand(b,results_tmp2)}
      $result = ${combineResults(results_tmp2)}
     }
    """
    results_tmp1.setNoUse
    results_tmp2.setNoUse
    rs.append((result, typ, true))
    rs.append(results_tmp1)
    rs.append(results_tmp2)
    tree
  }
}