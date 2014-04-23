package fastparsers.parsers

import fastparsers.input.ParseInput

/**
 * Created by Eric on 22.04.14.
 */
trait RepParsersImpl extends ParserImplHelper {
  self: ParseInput =>

  import c.universe._

  override def expand(tree: c.Tree, rs: ResultsStruct) = tree match {
    case q"$_.repParser[$d]($a)"                  => expand(a, rs)
    case q"$_.rep[$d]($a,$min,$max)"              => parseRep(a, d, min, max, rs)
    case q"$_.rep1[$d]($a)"                       => parseRep(a, d, q"1", q"-1", rs)
    case q"$_.repN[$d]($a,$n)"                    => parseRep(a, d, n, n, rs)
    case q"$_.opt[$d]($a)"                        => parseOpt(a, d, rs)
    case q"$_.repsep[$typ,$d]($a,$b)"             => parseRepsep(a, b, typ, atLeastOnce = false, rs)
    case q"$_.repsep1[$typ,$d]($a,$b)"            => parseRepsep(a, b, typ, atLeastOnce = true, rs)
    case q"$_.until[$typ,$d]($a,$b)"              => parseUntil(a, b, typ, rs)
    case q"$a foldLeft[$d]($init,$f)"             => parseFoldLeft(a, init, f, d, rs)
    case q"$a foldRight[$d,$ptype]($init,$f)"     => parseFoldRight(a, init, f, d, ptype, rs)
    case q"$a reduceLeft[$d]($f)"                 => parseReduceLeft(a, f, d, rs)
    case q"$a reduceRight[$d]($f)"                => parseReduceRight(a, f, d, rs)
    case _                                        => super.expand(tree, rs)
  }

  override def prettyPrint(tree: c.Tree) = tree match {
    case q"$_.repParser[$d]($a)"         => prettyPrint(a)
    case q"$_.rep[$d]($a,$min,$max)"     => "rep(" + prettyPrint(a) + ", " + show(min) + ", " + show(max) + ")"
    case q"$_.rep1[$d]($a)"              => "rep1(" + prettyPrint(a) + ")"
    case q"$_.repN[$d]($a,$n)"           => "repN(" + prettyPrint(a) + ", " + show(n) + ")"
    case q"$_.opt[$d]($a)"               => "opt(" + prettyPrint(a) + ")"
    case q"$_.repsep[$typ,$d]($a,$b)"    => "repsep(" + prettyPrint(a) + ", " + prettyPrint(b) + ")"
    case q"$_.repsep1[$typ,$d]($a,$b)"   => "repsep1(" + prettyPrint(a) + ", " + prettyPrint(b) + ")"
    case q"$_.until[$typ,$d]($a,$b)"     => "until(" + prettyPrint(a) + ", " + prettyPrint(b) + ")"
    case q"$a foldLeft[$d]($init,$f)"             => prettyPrint(a) + " foldLeft(" + show(init) + ", " + prettyPrint(f) + ")"
    case q"$a foldRight[$d,$ptype]($init,$f)"     => prettyPrint(a) + " foldRight(" + show(init) + ", " + prettyPrint(f) + ")"
    case q"$a reduceLeft[$d]($f)"                 => prettyPrint(a) + " reduceLeft(" + prettyPrint(f) + ")"
    case q"$a reduceRight[$d]($f)"                => prettyPrint(a) + " reduceRight(" + prettyPrint(f) + ")"
    case _                                        => super.prettyPrint(tree)
  }

  private def parseRep(a: c.Tree, typ: c.Tree, min: c.Tree, max: c.Tree, rs: ResultsStruct) = {
    val counter = TermName(c.freshName)
    val cont = TermName(c.freshName)
    val result = TermName(c.freshName)
    val tmp_result = TermName(c.freshName)
    var results_tmp = new ResultsStruct()

    val innerWhileTree = mark {
      rollback =>
        q"""
          ${expand(a, results_tmp)}
          if (success) {
              $tmp_result.append(${combineResults(results_tmp)})
              if ($counter + 1 == $max) $cont = false
          }
          else {
              success = $counter >= $min
              $cont = false
              if (!success)
                msg = "expected at least " + $min + " occurence(s) for rep(" + ${prettyPrint(a)} + ", " + $min + ", " + $max + ") at " + $pos
              else
                $rollback

          }
        """
    }

    val tree = mark {
      rollback =>
        q"""
          var $counter = 0
          var $cont = true
          val $tmp_result = new ListBuffer[$typ]()
          success = $min == 0
          while($cont){
            $innerWhileTree
            $counter = $counter + 1
          }
          if (!success) {
            $rollback
          }
          else {
             $result = $tmp_result.toList
          }
        """
    }
    results_tmp.setNoUse
    rs.append((result, tq"List[$typ]", true))
    rs.append(results_tmp)
    tree
  }

  private def parseOpt(a: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    var results_tmp = new ResultsStruct()
    val tree = mark {
      rollback =>
        q"""
        ${expand(a, results_tmp)}
        if (success) {
          $result = Some(${combineResults(results_tmp)})
        }
        else {
          $rollback
          $result = None
          success = true
        }
      """
    }
    results_tmp.setNoUse
    rs.append((result, tq"Option[$typ]", true))
    rs.append(results_tmp)
    tree
  }

  private def parseRepsep(a: c.Tree, sep: c.Tree, typ: c.Tree, atLeastOnce: Boolean, rs: ResultsStruct) = {
    var results_tmp = new ResultsStruct()
    var results_tmp2 = new ResultsStruct()
    val cont = TermName(c.freshName)
    val tmp_result = TermName(c.freshName)
    val result = TermName(c.freshName)

    val innertree2 = mark {
      rollback =>
        q"""
          ${expand(sep, results_tmp2)}
           if (!success) {
            $cont = false
            $rollback
           }
        """
    }

    val innertree1 = mark {
      rollback =>
        q"""
          ${expand(a, results_tmp)}
          if (success) {
             $tmp_result.append(${combineResults(results_tmp)})
             $innertree2
          }
          else {
            $cont = false
            $rollback
          }
         """
    }

    val assignSuccess =
      if (atLeastOnce)
        mark {
          rollback =>
            q"""
          if ($tmp_result.size == 0) {
            $rollback
            success = false
          }
          else {
            $result = $tmp_result.toList
            success = true
           }
        """
        }
      else {
        q"""
        $result = $tmp_result.toList
        success = true
      """
      }

    val tree =
      q"""
      var $cont = true
      val $tmp_result = new ListBuffer[$typ]()
      while($cont) {
        $innertree1
      }
      $assignSuccess
    """

    results_tmp.setNoUse
    results_tmp2.setNoUse
    rs.append(results_tmp)
    rs.append(results_tmp2)
    rs.append((result, tq"List[$typ]", true))
    tree
  }

  private def parseUntil(a: c.Tree, end: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    var results_tmp = new ResultsStruct()
    var results_tmp2 = new ResultsStruct()

    val cont = TermName(c.freshName)
    val tmp_result = TermName(c.freshName)
    val result = TermName(c.freshName)

    val innertree2 = mark { rollback =>
      q"""
        ${expand(end, results_tmp2)}
        if (success)
          $cont = false
        else
          $rollback
      """
    }

    val innertree = mark { rollback =>
      q"""
        ${expand(a,results_tmp)}
        if (success) {
          $tmp_result.append(${combineResults(results_tmp)})
          $innertree2
        }
        else {
          $rollback
          $cont = false
         }
      """
    }

    val tree =
    q"""
      var $cont = true
      val $tmp_result = new ListBuffer[$typ]()
      while($cont) {
        $innertree
      }
      $result = $tmp_result.toList
      success = true
    """
    results_tmp.setNoUse
    results_tmp2.setNoUse
    rs.append(results_tmp)
    rs.append(results_tmp2)
    rs.append((result, tq"List[$typ]", true))
    tree
  }

  private def parseFoldLeft(a: c.Tree, init: c.Tree, f: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    var results_tmp = new ResultsStruct()
    val result = TermName(c.freshName)
    val cont = TermName(c.freshName)
    val tmp_f = TermName(c.freshName)

    val inner = mark {
      rollback =>
        q"""
        ${expand(a, results_tmp)}
         if (success)
           $result = $tmp_f($result,${combineResults(results_tmp)})
         else {
          $cont = false
          $rollback
         }
      """
    }
    val tree =
      q"""
      val $tmp_f = $f
      var $cont = true
      $result = $init
      while($cont){
        $inner
      }
      success = true
    """
    results_tmp.setNoUse
    rs.append(results_tmp)
    rs.append((result, typ, true))
    tree
  }

  private def buffer(a: c.Tree, typ: c.Tree, rs: ResultsStruct)(process: TermName => c.Tree) = {
    var results_tmp = new ResultsStruct()
    val cont = TermName(c.freshName)
    val buffer = TermName(c.freshName)

    val buffering = mark {
      rollback =>
        q"""
        ${expand(a, results_tmp)}
        if (success)
          $buffer.append(${combineResults(results_tmp)})
        else
          $cont = false
      """
    }

    val tree =
      q"""
     var $cont = true
     val $buffer = new ListBuffer[$typ]()
     while($cont){
       $buffering
     }

     ${process(buffer)}
    """
    results_tmp.setNoUse
    rs.append(results_tmp)
    tree
  }


  private def parseFoldRight(a: c.Tree, init: c.Tree, f: c.Tree, typ: c.Tree, parserType: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val tree = buffer(a, parserType, rs) {
      buffer =>
        q"""
       $result = $buffer.foldRight[$typ]($init)($f)
       success = true
      """
    }
    rs.append((result, typ, true))
    tree
  }

  private def parseReduceLeft(a: c.Tree, f: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    var results_tmp = new ResultsStruct()
    val tree = mark {
      rollback =>
        q"""
       ${expand(a, results_tmp)}
       if (success){
          ${parseFoldLeft(a, combineResults(results_tmp), f, typ, rs)}
       }
       else {
        success = false
        msg = ${prettyPrint(a)} + ".reduceLeft failed"
        $rollback
       }
      """
    }
    results_tmp.setNoUse
    rs.append(results_tmp)
    tree
  }


  private def parseReduceRight(a: c.Tree, f: c.Tree, typ: c.Tree, rs: ResultsStruct) = {
    val result = TermName(c.freshName)
    val tree = buffer(a, typ, rs) {
      buffer =>
        q"""
        if ($buffer.size == 0){
          success = false
          msg = ${prettyPrint(a)} + ".reduceRight failed"
        }
        else {
         success = true
         $result = $buffer.reduceRight[$typ]($f)
        }
      """
    }
    rs.append((result, typ, true))
    tree
  }

}