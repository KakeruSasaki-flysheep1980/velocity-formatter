package com.github.flysheep1980.velocity

import java.io._
import io.Source
import java.util.regex.Matcher
import scala.Some

class Formatter(val lineSeparator: String, val indentString: String) extends NodeSplitter with NodeBuilder {
  def format(input: String): String = build(split(input))
}

object Formatter {

  private val DefaultEncodeCharset = "utf-8"
  private val DefaultLineSeparator = "\n"
  private val DefaultIndentString = "\t"

  def formatString(str: String, encodeCharset: String, lineSeparator: String, indentString: String): String = {
    new Formatter(lineSeparator, indentString).format(str)
  }

  def formatFile(file: File, encodeCharset: String = DefaultEncodeCharset, lineSeparator: String = DefaultLineSeparator, indentString: String = DefaultIndentString) {
    val input = Source.fromFile(file, encodeCharset).getLines.toList.mkString(lineSeparator)
    val formatted = formatString(input, encodeCharset, lineSeparator, indentString)

    val writer = new FileWriter(file)
    writer.write(formatted)
    writer.flush()
    writer.close()
  }

}

object NodeType extends Enumeration {
  val LeftHtmlTag, RightHtmlTag, SingleHtmlTag, DoctypeHtmlTag, VelocitySingleComment, VelocityMultiComment, IfDirective, ElseIfDirective, ElseDirective, EndDirective, SetDirective, ParseDirective, ForeachDirective, LineSeparator, Other = Value
}

case class Node(s: String, t: NodeType.Value) {
  lazy val trimmed = s.trim
}

trait NodeBuilder {

  protected def indentString: String
  protected def lineSeparator: String

  /**
   * build nodes to indented string.
   *
   * @param nodes list of node
   * @return
   */
  def build(nodes: List[Node]): String = {
    var indentLevel = 0
    val builder = new StringBuilder
    nodes.foreach { node =>
      def indent = List.fill(indentLevel)(indentString).mkString

      // 改行を挿入
      if (builder.isEmpty == false && node.trimmed.isEmpty == false && node.t != NodeType.LineSeparator && node.t != NodeType.VelocitySingleComment) {
        builder.append(lineSeparator)
      }

      node.t match {
        case NodeType.LineSeparator => // do nothing
        case NodeType.LeftHtmlTag | NodeType.IfDirective | NodeType.ForeachDirective => {
          builder.append(indent)
          builder.append(node.trimmed)
          indentLevel += 1
        }
        case NodeType.ElseIfDirective | NodeType.ElseDirective => {
          indentLevel -= 1
          builder.append(indent)
          builder.append(node.trimmed)
          indentLevel += 1
        }
        case NodeType.RightHtmlTag | NodeType.EndDirective => {
          indentLevel -= 1
          builder.append(indent)
          builder.append(node.trimmed)
        }
        case NodeType.VelocitySingleComment => {
          builder.append(node.s)
        }
        case NodeType.VelocityMultiComment => {
          if (node.s.contains(lineSeparator)) { // 複数行コメント
            builder.append(indent)
            builder.append(node.trimmed.replaceAll(lineSeparator, lineSeparator + indent + " * "))
          } else { // 単一行コメント
            builder.append(indent)
            builder.append(node.trimmed)
          }
        }
        case _ => {
          builder.append(indent)
          builder.append(node.trimmed)
        }
      }
    }

    builder.toString
  }
}

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
    val regex = List("<", "#", lineSeparator).mkString("|").r
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
              case _@ s if s == "#" => splitVelocityDirective(target)
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
      case _@ s if s.startsWith("<!") => (indexToSplit, NodeType.DoctypeHtmlTag)
      case _@ s if s.startsWith("</") => (indexToSplit, NodeType.RightHtmlTag)
      case _@ s if s.startsWith("<br") | s.startsWith("<hr") | s.startsWith("<meta") => {
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
    val firstLeftIndex = target.indexOf(left)
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

}
