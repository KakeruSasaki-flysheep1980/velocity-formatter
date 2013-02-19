package com.github.flysheep1980.velocity

trait NodeBuilder {

  protected def indentString: String
  protected def lineSeparator: String

  /**
   * build nodes to indented string.
   *
   * @param nodes list of node
   * @return
   */
  def build(nodes: List[Node]): (String, BuildResult) = {
    var indentLevel = 0
    var ifIndentLevel = Seq.empty[Int]
    val builder = new StringBuilder
    nodes.foreach { node =>
      def indent = List.fill(indentLevel)(indentString).mkString

      // 改行を挿入
      if (builder.isEmpty == false && node.trimmed.isEmpty == false
        && node.t != NodeType.LineSeparator && node.t != NodeType.VelocitySingleComment) {

        builder.append(lineSeparator)
      }

      node.t match {
        case NodeType.LineSeparator => {
          // do nothing
        }
        case NodeType.IfDirective => {
          builder.append(indent)
          builder.append(node.trimmed)
          ifIndentLevel = Seq(indentLevel) ++ ifIndentLevel
          indentLevel += 1
        }
        case NodeType.LeftHtmlTag | NodeType.ForeachDirective => {
          builder.append(indent)
          builder.append(node.trimmed)
          indentLevel += 1
        }
        case NodeType.ElseIfDirective | NodeType.ElseDirective => {
          indentLevel -= 1
          if (ifIndentLevel.isEmpty == false) {
            indentLevel = ifIndentLevel.head
          }
          builder.append(indent)
          builder.append(node.trimmed)
          indentLevel += 1
        }
        case NodeType.EndDirective => {
          indentLevel -= 1
          if (ifIndentLevel.isEmpty == false) {
            indentLevel = ifIndentLevel.head
            ifIndentLevel = ifIndentLevel.tail
          }
          builder.append(indent)
          builder.append(node.trimmed)
        }
        case NodeType.RightHtmlTag => {
          indentLevel -= 1
          builder.append(indent)
          builder.append(node.trimmed)
        }
        case NodeType.VelocitySingleComment => {
          builder.append(node.s)
        }
        case NodeType.VelocityMultiComment | NodeType.HtmlComment => {
          if (node.s.contains(lineSeparator)) { // 複数行コメント
            builder.append(indent)
            builder.append(node.trimmed.replaceAll(lineSeparator, lineSeparator + indent + "   " /* [#* ][ *#]の整形のため */ ))
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

    (builder.toString, BuildResult(indentLevel))
  }
}

case class BuildResult(indentLevel: Int)