package com.github.flysheep1980.velocity

import java.util.regex.Matcher

trait NodeSplitter {

  type SplitIndex = Int

  def lineSeparator: String

  /**
   * split string to list of node.
   *
   * @param target string to split
   * @return list of node
   */
  def split(target: String): List[Node] = {
    val regex = List("<", "#if", "#elseif", "#else", "#end", "#foreach", "#parse", "#set", "#\\*", lineSeparator).mkString("|").r
    regex.findFirstMatchIn(target) match {
      case Some(m) => {
        // 分割位置 - 文字列の最初からこのindex値までを取り出す
        // 切り出した文字列（ノード）の種別
        val (splitIndex, nodeType) = {
          if (m.start != 0) {
            (m.start, NodeType.Other)
          } else {
            Matcher.quoteReplacement(m.group(0)) match {
              case _@ s if s == lineSeparator => (m.end, NodeType.LineSeparator)
              case _@ s if s == "<" => splitHtml(target)
              case _@ s if s.startsWith("#") => splitVelocityDirective(target)
            }
          }
        }

        val (elem, rest) = target.splitAt(splitIndex)
        List(Node(elem, nodeType)) ::: split(rest)

      }
      case None => List(Node(target, NodeType.Other))
    }
  }

  def splitHtml(target: String): (SplitIndex, NodeType.Value) = {
    lazy val indexToSplit = indexOf(target, '<', '>')

    target match {
      case _@ s if s.startsWith("<!--") => {
        val right = "-->"
        (s.indexOf(right) + right.length, NodeType.HtmlComment)
      }
      case _@ s if s.startsWith("<!") => (indexToSplit, NodeType.DoctypeHtmlTag)
      case _@ s if s.startsWith("</") => (indexToSplit, NodeType.RightHtmlTag)
      case _@ s if s.startsWith("<br") | s.startsWith("<hr") | s.startsWith("<meta") | s.startsWith("<img") => {
        (indexToSplit, NodeType.SingleHtmlTag)
      }
      case _ => (indexToSplit, NodeType.LeftHtmlTag)
    }
  }

  def splitVelocityDirective(target: String): (SplitIndex, NodeType.Value) = {
    lazy val indexToSplit = indexOf(target, '(', ')')

    target match {
      case _@ s if s.startsWith("#if") => (indexToSplit, NodeType.IfDirective)
      case _@ s if s.startsWith("#elseif") => (indexToSplit, NodeType.ElseIfDirective)
      case _@ s if s.startsWith("#foreach") => (indexToSplit, NodeType.ForeachDirective)
      case _@ s if s.startsWith("#set") => (indexToSplit, NodeType.SetDirective)
      case _@ s if s.startsWith("#parse") => (indexToSplit, NodeType.ParseDirective)
      case _@ s if s.startsWith("#else") => ("#else".length, NodeType.ElseDirective)
      case _@ s if s.startsWith("#end") => ("#end".length, NodeType.EndDirective)
      case _@ s if s.startsWith("#*") => {
        val right = "*#"
        (s.indexOf(right) + right.length, NodeType.VelocityMultiComment)
      }
      case _@ s if s.startsWith("##") => (s.indexOf(lineSeparator), NodeType.VelocitySingleComment)
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
