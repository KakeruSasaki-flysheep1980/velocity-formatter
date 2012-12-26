package com.github.flysheep1980.velocity

//object NodeType extends Enumeration {
//  val LeftHtmlTag, RightHtmlTag, SingleHtmlTag, DoctypeHtmlTag, HtmlComment, VelocitySingleComment, VelocityMultiComment, IfDirective, ElseIfDirective, ElseDirective, EndDirective, SetDirective, ParseDirective, ForeachDirective, LineSeparator, Other = Value
//}

case class Node(s: String, afterLineBreak: Boolean = false, indent: Boolean = false) {
  lazy val trimmed = s.trim
}
