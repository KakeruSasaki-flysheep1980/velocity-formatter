package com.github.flysheep1980.velocity

import org.specs2.mutable._

class NodeSplitterSpec extends Specification {

  private val tester = new NodeSplitter {
    def lineSeparator: String = "\n"
  }

  "indexOf" should {

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

}
