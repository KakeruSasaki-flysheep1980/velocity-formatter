package com.github.flysheep1980.velocity

import java.io._
import io.Source
import java.util.regex.Matcher
import scala.Some

class Formatter(val lineSeparator: String, val indentString: String) {

  def execute(input: String): String = {
    joinWithIndent(splitToNodes(input))
  }

  protected def searchIndex(str: String, left: Char, right: Char): Int = {
    val firstLeftIndex = str.indexOf(left)
    var diff = 0
    // TODO indexWhereを使ってできそう
    val taken = str.drop(firstLeftIndex).takeWhile { c =>
      c match {
        case _@ a if a == left => diff += 1
        case _@ a if a == right => diff -= 1
        case _ => // do nothing
      }
      diff != 0
    }
    firstLeftIndex + taken.length + 1
  }

  //    @scala.annotation.tailrec
  protected def splitToNodes(str: String): List[Node] = {

    def splitHtmlNode = {
      lazy val indexOfSplit = searchIndex(str, '<', '>')

      str match {
        case _@ s if s.startsWith("<!") => (indexOfSplit, NodeType.DoctypeHtmlTag)
        case _@ s if s.startsWith("</") => (indexOfSplit, NodeType.RightHtmlTag)
        case _@ s if s.startsWith("<br") | s.startsWith("<hr") | s.startsWith("<meta") => {
          (indexOfSplit, NodeType.SingleHtmlTag)
        }
        case _ => (indexOfSplit, NodeType.LeftHtmlTag)
      }
    }

    def splitVelocityNode = {
      lazy val indexOfSplit = searchIndex(str, '(', ')')

      str match {
        case _@ s if s.startsWith("#if") => (indexOfSplit, NodeType.IfDirective)
        case _@ s if s.startsWith("#elseif") => (indexOfSplit, NodeType.ElseIfDirective)
        case _@ s if s.startsWith("#foreach") => (indexOfSplit, NodeType.ForeachDirective)
        case _@ s if s.startsWith("#set") => (indexOfSplit, NodeType.SetDirective)
        case _@ s if s.startsWith("#parse") => (indexOfSplit, NodeType.ParseDirective)
        case _@ s if s.startsWith("#else") => ("#else".length, NodeType.ElseDirective)
        case _@ s if s.startsWith("#end") => ("#end".length, NodeType.EndDirective)
        case _@ s if s.startsWith("#*") => {
          val right = "*#"
          (str.indexOf(right) + right.length, NodeType.VelocityMultiComment)
        }
        case _@ s if s.startsWith("##") => (str.indexOf(lineSeparator), NodeType.VelocitySingleComment)
      }
    }

    // HTML関連やVelocity関連の文字列を検索
    val regex = List("<", "#", lineSeparator).mkString("|").r
    regex.findFirstMatchIn(str) match {
      case Some(m) => {
        // 分割位置 - 文字列の最初からこのindex値までを取り出す
        // 切り出した文字列（ノード）の種別
        val (splitIndex, nodeType) = {
          if (m.start != 0) {
            (m.start, NodeType.Other)
          } else {
            Matcher.quoteReplacement(m.group(0)) match {
              case _@ s if s == lineSeparator => (m.end, NodeType.LineSeparator)
              case _@ s if s == "<" => splitHtmlNode
              case _@ s if s == "#" => splitVelocityNode
            }
          }
        }

        val (elem, rest) = str.splitAt(splitIndex)
        List(Node(elem, nodeType)) ::: splitToNodes(rest)

      }
      case None => List(Node(str, NodeType.Other))
    }
  }

  protected def joinWithIndent(nodes: List[Node]): String = {
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

object Formatter {

  private val DefaultEncodeCharset = "utf-8"
  private val DefaultLineSeparator = "\n"
  private val DefaultIndentString = "\t"

  def format(str: String, encodeCharset: String, lineSeparator: String, indentString: String): String = {
    new Formatter(lineSeparator, indentString).execute(str)
  }

  def format(file: File, encodeCharset: String = DefaultEncodeCharset, lineSeparator: String = DefaultLineSeparator, indentString: String = DefaultIndentString): String = {
    val input = Source.fromFile(file, encodeCharset).getLines.toList.mkString(lineSeparator)
    this.format(input, encodeCharset, lineSeparator, indentString)
  }

}

object NodeType extends Enumeration {
  val LeftHtmlTag, RightHtmlTag, SingleHtmlTag, DoctypeHtmlTag, VelocitySingleComment, VelocityMultiComment, IfDirective, ElseIfDirective, ElseDirective, EndDirective, SetDirective, ParseDirective, ForeachDirective, LineSeparator, Other = Value
}
case class Node(s: String, t: NodeType.Value) {
  lazy val trimmed = s.trim
}
