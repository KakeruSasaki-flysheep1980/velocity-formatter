package com.github.flysheep1980.velocity

import org.specs2.mutable._
import org.specs2.matcher.Matcher

class FormatterSpec extends Specification {

  def tester = new Formatter("\n", "  ")

  def beFormatted(expected: String): Matcher[String] = (input: String) => tester.execute(input) === expected

  "html tags" should {

    "be formatted with indent." in {
      """<html><head></head><body></body></html>""" must beFormatted(
        """<html>
          |  <head>
          |  </head>
          |  <body>
          |  </body>
          |</html>
          |""".stripMargin
      )
    }

    "be formatted with indent." in {
      """<div class="class1">hoge</div>""" must beFormatted(
        """<div class="class1">
          |  hoge
          |</div>
          |""".stripMargin
      )
    }

  }

}
