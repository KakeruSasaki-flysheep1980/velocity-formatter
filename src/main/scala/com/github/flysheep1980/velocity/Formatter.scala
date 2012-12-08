package com.github.flysheep1980.velocity

import java.io._
import io.Source
import java.util.regex.Matcher
import scala.Some

object Formatter {

	val LineSeparator = "\n"
	val IndentString = " "
	val EncodeCharset = "UTF-8"

	def searchIndex(str: String, left: Char, right: Char): Int = {
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
	def splitToNodes(str: String): List[Node] = {
		val r = List("<", "#", LineSeparator).mkString("|").r

		r.findFirstMatchIn(str) match {
			case Some(m) => {
				// 分割位置 - 文字列の最初からこのindex値までを取り出す
				// 切り出した文字列（ノード）の種別
				val (splitIndex, nodeType) = {
					if (m.start != 0) {
						(m.start, NodeType.Other)
					} else {
						val g = Matcher.quoteReplacement(m.group(0))
						g match {
							case LineSeparator => (m.end, NodeType.LineSeparator)
							case "<" => {
								str match {
									case _@ s if s.startsWith("<!") => (searchIndex(str, '<', '>'), NodeType.DoctypeHtmlTag)
									case _@ s if s.startsWith("</") => (searchIndex(str, '<', '>'), NodeType.RightHtmlTag)
									case _@ s if s.startsWith("<br") | s.startsWith("<hr") | s.startsWith("<meta") => (searchIndex(str, '<', '>'), NodeType.SingleHtmlTag)
									case _ => (searchIndex(str, '<', '>'), NodeType.LeftHtmlTag)
								}
							}
							case "#" => {
								str match {
									case _@ s if s.startsWith("#if") => (searchIndex(str, '(', ')'), NodeType.IfDirective)
									case _@ s if s.startsWith("#elseif") => (searchIndex(str, '(', ')'), NodeType.ElseIfDirective)
									case _@ s if s.startsWith("#foreach") => (searchIndex(str, '(', ')'), NodeType.ForeachDirective)
									case _@ s if s.startsWith("#set") => (searchIndex(str, '(', ')'), NodeType.SetDirective)
									case _@ s if s.startsWith("#parse") => (searchIndex(str, '(', ')'), NodeType.ParseDirective)
									case _@ s if s.startsWith("#else") => ("#else".length, NodeType.ElseDirective)
									case _@ s if s.startsWith("#end") => ("#end".length, NodeType.EndDirective)
									case _@ s if s.startsWith("#*") => (str.indexOf("*#") + "*#".length, NodeType.VelocityMultiComment)
									case _@ s if s.startsWith("##") => (str.indexOf(LineSeparator), NodeType.VelocitySingleComment)
								}
							}
						}
					}
				}

				val (elem, rest) = str.splitAt(splitIndex)
				List(Node(elem, nodeType)) ::: splitToNodes(rest)

			}
			case None => List(Node(str, NodeType.Other))
		}
	}

	def joinWithIndent(nodes: List[Node]): String = {
		var indentLevel = 0
		val builder = new StringBuilder
		nodes.foreach { node =>
			def indent = List.fill(indentLevel)(IndentString).mkString

			// 改行を挿入
			if (builder.isEmpty == false && node.t != NodeType.LineSeparator && node.t != NodeType.VelocitySingleComment) {
				builder.append(LineSeparator)
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
					if (node.s.contains(LineSeparator)) { // 複数行コメント
						builder.append(indent)
						builder.append(node.trimmed.replaceAll(LineSeparator, LineSeparator + indent + " * "))
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

	def format(fileName: String): String = {
		val file = new File(fileName)
		val input = Source.fromFile(file, EncodeCharset).getLines.toList.mkString(LineSeparator)
		val nodes = splitToNodes(input)
		val ret = joinWithIndent(nodes)

		//    val output = "htmls/output.vm"
		//    val writer = new OutputStreamWriter(new FileOutputStream(new File(output)), EncodeCharset)
		//    writer.write(ret)
		//    writer.flush
		//    writer.close

		ret
	}

	object NodeType extends Enumeration {
		val LeftHtmlTag, RightHtmlTag, SingleHtmlTag, DoctypeHtmlTag, VelocitySingleComment, VelocityMultiComment, IfDirective, ElseIfDirective, ElseDirective, EndDirective, SetDirective, ParseDirective, ForeachDirective, LineSeparator, Other = Value
	}
	case class Node(s: String, t: NodeType.Value) {
		lazy val trimmed = s.trim
	}

}
