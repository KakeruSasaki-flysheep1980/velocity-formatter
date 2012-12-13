package com.github.flysheep1980.velocity

import org.specs2.mutable._

class NodeSplitterSpec extends Specification {

  private val tester = new NodeSplitter {
    def lineSeparator: String = "\n"
  }

  "NodeSplitter#indexOf" should {

    "be returned not found index if left char not exist." in {
      tester.indexOf("hoge)fuga", '(', ')') === -1
    }

    "search right char if left char exist." in {
      tester.indexOf("()()", '(', ')') === 2
    }

    "be returned not found index if left char not exist before right char." in {
      tester.indexOf(")(", '(', ')') === -1
    }

    "search right char if a few left char exist." in {
      tester.indexOf("(())", '(', ')') === 4
    }

    "search right char if left char and other char exist." in {
      tester.indexOf("a(b(c)d)e", '(', ')') === 8
    }

  }

  "NodeSplitter" should {

    "split 'if/elseif/else/end' directive." in {
      tester.split("""1 #if($hoge) 2 #elseif($fuga.method($piyo)) 3 #else 4 #end 5""") === List(
        Node("1 ", NodeType.Other),
        Node("#if($hoge)", NodeType.IfDirective),
        Node(" 2 ", NodeType.Other),
        Node("#elseif($fuga.method($piyo))", NodeType.ElseIfDirective),
        Node(" 3 ", NodeType.Other),
        Node("#else", NodeType.ElseDirective),
        Node(" 4 ", NodeType.Other),
        Node("#end", NodeType.EndDirective),
        Node(" 5", NodeType.Other)
      )
    }

    "split 'parse' directive." in {
      tester.split("""1 #parse("file.vm") 2""") === List(
        Node("1 ", NodeType.Other),
        Node("""#parse("file.vm")""", NodeType.ParseDirective),
        Node(" 2", NodeType.Other)
      )
    }

    "split 'foreach/end' directive." in {
      tester.split("""1 #foreach($n in [1..3]) $n #end 2""") === List(
        Node("1 ", NodeType.Other),
        Node("""#foreach($n in [1..3])""", NodeType.ForeachDirective),
        Node(" $n ", NodeType.Other),
        Node("#end", NodeType.EndDirective),
        Node(" 2", NodeType.Other)
      )
    }

    "split 'set' directive." in {
      tester.split("""1 #set($hoge = $fuga.method($piyo)) 2""") === List(
        Node("1 ", NodeType.Other),
        Node("""#set($hoge = $fuga.method($piyo))""", NodeType.SetDirective),
        Node(" 2", NodeType.Other)
      )
    }

    "not split 'style color code'." in {
      val input = """ style="background-color:#FFEEF4;color:#FF8991;" """
      tester.split(input) === List(Node(input, NodeType.Other))
    }

    "split 'html doctype tag'." in {
      tester.split("""xxx <!DOCTYPE HTML> xxx""") === List(
        Node("xxx ", NodeType.Other),
        Node("<!DOCTYPE HTML>", NodeType.DoctypeHtmlTag),
        Node(" xxx", NodeType.Other)
      )

      tester.split("""xxx <!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"> xxx""") === List(
        Node("xxx ", NodeType.Other),
        Node("""<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">""", NodeType.DoctypeHtmlTag),
        Node(" xxx", NodeType.Other)
      )
    }

    "split 'html comment'." in {
      tester.split("""<html><body>xxx<div align="left">yyy</div></body></html>""") === List(
        Node("<html>", NodeType.LeftHtmlTag),
        Node("<body>", NodeType.LeftHtmlTag),
        Node("xxx", NodeType.Other),
        Node("""<div align="left">""", NodeType.LeftHtmlTag),
        Node("yyy", NodeType.Other),
        Node("</div>", NodeType.RightHtmlTag),
        Node("</body>", NodeType.RightHtmlTag),
        Node("</html>", NodeType.RightHtmlTag),
        Node("", NodeType.Other)
      )
    }

  }

}
