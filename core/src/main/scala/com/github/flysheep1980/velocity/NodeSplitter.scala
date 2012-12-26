package com.github.flysheep1980.velocity

import java.util.regex.Matcher
import java.io.File

trait NodeSplitter {

  type SplitIndex = Int

  def lineSeparator: String

  lazy val patterns = List(
    """#\*.*\*#""",
    """##""",
    """<!--.*-->""",
    """<!DOCTYPE [.[^<>]]+>""",
    """<[.[^/!<>]]+ />""",
    """</[a-zA-Z]+>""",
    """<[.[^/!<>]]+>""",
    """#if""",
    """#elseif""",
    """#else""",
    """#end""",
    """#foreach""",
    """#parse""",
    """#set"""
  )

  lazy val regex = patterns.mkString("|").r

  /**
   * split string to list of node.
   *
   * @param target string to split
   * @return list of node
   */
  def split(target: String): List[Node] = {
    regex.findFirstMatchIn(target) match {
      case Some(m) => {
        val (node, rest) = {
          if (m.start != 0) {
            val (n, rest) = target.splitAt(m.start)
            (Node(n), rest)
          } else {
            Matcher.quoteReplacement(m.group(0)) match {
              case _@ s if s.startsWith("<!DOCTYPE ") => {
                val (n, rest) = target.splitAt(m.end)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("<!--") => {
                val (n, rest) = target.splitAt(m.end)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("<") && s.endsWith("/>") => {
                val (n, rest) = target.splitAt(m.end)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("</") && s.endsWith(">") => {
                val (n, rest) = target.splitAt(m.end)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("<") && s.endsWith(">") => {
                //                val tagName = """<([a-zA-Z]+)[.[^/!<>]]*>""".r.findFirstMatchIn(s).map(ma => Matcher.quoteReplacement(ma.group(1)))
                //                val inlineTags = Seq("span", "a")
                //                val isInlineTag = tagName.map(inlineTags.contains(_)).getOrElse(false)
                //                lazy val rightTag = "</%s>".format(tagName.get)
                //                lazy val rightIndex = target.indexOf(rightTag)
                //                lazy val innerText = target.substring(m.end, rightIndex)
                //
                //                if (tagName.isDefined && isInlineTag) {
                //                  val (n, rest) = target.splitAt(rightIndex + rightTag.length)
                //                  (Node(n), rest)
                //                } else {
                //                  val (n, rest) = target.splitAt(m.end)
                //                  (Node(n), rest)
                //                }
                val (n, rest) = target.splitAt(m.end)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("#if") => {
                val splitIndex = indexOf(target, '(', ')')
                val (n, rest) = target.splitAt(splitIndex)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("#elseif") => {
                val splitIndex = indexOf(target, '(', ')')
                val (n, rest) = target.splitAt(splitIndex)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("#else") => {
                val (n, rest) = target.splitAt(m.end)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("#end") => {
                val (n, rest) = target.splitAt(m.end)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("#foreach") => {
                val splitIndex = indexOf(target, '(', ')')
                val (n, rest) = target.splitAt(splitIndex)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("#parse") => {
                val splitIndex = indexOf(target, '(', ')')
                val (n, rest) = target.splitAt(splitIndex)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("#set") => {
                val splitIndex = indexOf(target, '(', ')')
                val (n, rest) = target.splitAt(splitIndex)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("#*") => {
                val (n, rest) = target.splitAt(m.end)
                (Node(n), rest)
              }
              case _@ s if s.startsWith("##") => {
                val splitIndex = target.indexOf(lineSeparator) + lineSeparator.length
                val (n, rest) = target.splitAt(splitIndex)
                (Node(n), rest)
              }
            }
          }
        }

        List(node) ::: split(rest)
      }
      case None => List(Node(target))
    }
  }

  def indexOf(target: String, left: Char, right: Char): SplitIndex = {
    lazy val firstRightIndex = target.indexOf(right)
    target.indexOf(left) match {
      case _@ firstLeftIndex if firstLeftIndex != -1 && firstLeftIndex < firstRightIndex => {
        var diff = 0
        // TODO indexWhereを使ってできそう
        val taken = target.drop(firstLeftIndex).takeWhile { c =>
          c match {
            case _@ a if a == left => diff += 1
            case _@ a if a == right => diff -= 1
            case _ => // do nothing
          }
          diff != 0
        }
        firstLeftIndex + taken.length + 1
      }
      case _ => -1
    }
  }

}

object NodeSplitter {
  def execute = {
    val file = new File("sample.vm")
    val input = scala.io.Source.fromFile(file, "utf-8").getLines.toList.mkString("\n")
    val splitter = new NodeSplitter {
      def lineSeparator: String = "\n"
    }
    val nodes = splitter.split(input)

    nodes.foreach(println)
  }
}
