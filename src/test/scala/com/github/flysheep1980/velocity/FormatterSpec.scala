package com.github.flysheep1980.velocity

import org.specs2.mutable._
import org.specs2.matcher.Matcher
import io.Source
import java.net.URL

class FormatterSpec extends Specification {

  protected def tester = new Formatter(lineSeparator, "  ")
  protected val lineSeparator = "\n"

  protected def beFormatted(expected: String): Matcher[String] = (input: String) => tester.execute(input) === expected
  protected def beFormatted(expected: URL): Matcher[URL] = (input: URL) => {
    val act = Source.fromURL(input, "utf-8").getLines().toList.mkString(lineSeparator)
    val expect = Source.fromURL(expected, "utf-8").getLines().toList.mkString(lineSeparator)
    act must beFormatted(expect)
  }
  protected def toURL(fileName: String): URL = this.getClass.getResource(fileName)

  "html tags" should {

    "be formatted." in {
      """<html><head></head><body></body></html>""" must beFormatted(
        """<html>
          |  <head>
          |  </head>
          |  <body>
          |  </body>
          |</html>""".stripMargin
      )
    }

    "be formatted." in {
      """<div class="class1">hoge</div>""" must beFormatted(
        """<div class="class1">
          |  hoge
          |</div>""".stripMargin
      )
    }

  }

  "velovity directive" should {

    "be formatted." in {
      """#if($hoge) hoge #else fuga #end""" must beFormatted(
        """#if($hoge)
          |  hoge
          |#else
          |  fuga
          |#end""".stripMargin
      )
    }

    "be formatted." in {
      """#if($tool.getKind($id) == 1) kind1 #elseif($tool.getKind($id) == 2) kind2 #else kind3 #end""" must beFormatted(
        """#if($tool.getKind($id) == 1)
          |  kind1
          |#elseif($tool.getKind($id) == 2)
          |  kind2
          |#else
          |  kind3
          |#end""".stripMargin
      )
    }

    "be formatetd." in {
      """#foreach($i in [1..3]) $tool.getKind($i) #end""" must beFormatted(
        """#foreach($i in [1..3])
          |  $tool.getKind($i)
          |#end""".stripMargin
      )
    }

    "be formatted." in {
      """<div>#set($hoge = $tool.getKind($i))</div>""" must beFormatted(
        """<div>
          |  #set($hoge = $tool.getKind($i))
          |</div>""".stripMargin
      )
    }

  }

  "velocity template file" should {

    "be formatted." in {
      toURL("/sample.html") must beFormatted(toURL("/sample_formatted.html"))
    }

    "be formatted." in {
      toURL("/sample.vm") must beFormatted(toURL("/sample_formatted.vm"))
    }

  }

}
