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
  def build(nodes: List[Node]): String = {
    var indentLevel = 0
    val builder = new StringBuilder
    nodes.foreach { node =>
      def indent = List.fill(indentLevel)(indentString).mkString

      // 改行を挿入
      if (builder.isEmpty == false && node.trimmed.isEmpty == false
        && node.t != NodeType.LineSeparator && node.t != NodeType.VelocitySingleComment) {

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
        case NodeType.VelocityMultiComment | NodeType.HtmlComment => {
          if (node.s.contains(lineSeparator)) { // 複数行コメント
            builder.append(indent)
            builder.append(node.trimmed.replaceAll(lineSeparator, lineSeparator + indent + "   "))
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
