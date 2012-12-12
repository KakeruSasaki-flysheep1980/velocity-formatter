package com.github.flysheep1980.velocity

import java.io._
import io.Source

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

    val writer = new OutputStreamWriter(new FileOutputStream(file), encodeCharset)
    writer.write(formatted)
    writer.flush()
    writer.close()
  }

  def formatFileNoOverwrite(file: File, encodeCharset: String = DefaultEncodeCharset, lineSeparator: String = DefaultLineSeparator, indentString: String = DefaultIndentString) {
    val input = Source.fromFile(file, encodeCharset).getLines.toList.mkString(lineSeparator)
    val formatted = formatString(input, encodeCharset, lineSeparator, indentString)

    val output = new File("output.dat")
    val writer = new OutputStreamWriter(new FileOutputStream(output), encodeCharset)
    writer.write(formatted)
    writer.flush()
    writer.close()
  }

}
