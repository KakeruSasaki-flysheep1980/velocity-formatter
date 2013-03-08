package com.github.flysheep1980.velocity

import org.specs2.mutable._
import org.specs2.matcher.Matcher
import io.Source
import java.net.URL

class FormatterSpec extends Specification {

  protected def tester = new Formatter(lineSeparator, "  ")
  protected val lineSeparator = "\n"

  protected def beFormatted(expected: String): Matcher[String] = (input: String) => {
    val actual = tester.format(input)._1
    //    printChars(actual, expected)
    actual === expected
  }

  private def printChars(actual: String, expected: String) {
    println("'%s'".format(actual))
    println("--")
    println("'%s'".format(expected))
    println("--")
    println("%s %s".format(actual.length, expected.length))

    (actual, expected).zipped.toList.zipWithIndex.foreach {
      case ((a, e), index) => {
        println("[%s]'%s' '%s'".format(index, a, e))
      }
    }
  }

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

    "be formatted #if-else-end derective." in {
      """#if($hoge) hoge #else fuga #end""" must beFormatted(
        """#if($hoge)
                                  |  hoge
                                  |#else
                                  |  fuga
                                  |#end""".stripMargin
      )
    }

    "be formatted #if-elseif-else-end derective." in {
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

    "be formatetd #foreach-end derective." in {
      """#foreach($i in [1..3]) $tool.getKind($i) #end""" must beFormatted(
        """#foreach($i in [1..3])
                                  |  $tool.getKind($i)
                                  |#end""".stripMargin
      )
    }

    "be formatted #set derective." in {
      """<div>#set($hoge = $tool.getKind($i))</div>""" must beFormatted(
        """<div>
                                  |  #set($hoge = $tool.getKind($i))
                                  |</div>""".stripMargin
      )
    }

    "be formatted #if derective contains open tag only." in {
      """#if($hoge) <div class="style1"> #else <div class="style2"> #end fugafuga</div>""" must beFormatted(
        """#if($hoge)
          |  <div class="style1">
          |#else
          |  <div class="style2">
          |#end
          |fugafuga
          |</div>""".stripMargin
      )
    }

    "be formatted multi #if derective contains open tag only." in {
      """#if($hoge) <div class="style1"> #if($fuga) <span class="style3"> #else <span class="style4"> #end #else <div class="style2"> #if($fuga) <span class="style3"> #else <span class="style4"> #end #end fugafuga</span></div>""" must beFormatted(
        """#if($hoge)
              |  <div class="style1">
              |    #if($fuga)
              |      <span class="style3">
              |    #else
              |      <span class="style4">
              |    #end
              |#else
              |  <div class="style2">
              |    #if($fuga)
              |      <span class="style3">
              |    #else
              |      <span class="style4">
              |    #end
              |#end
              |fugafuga
              |</span>
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
