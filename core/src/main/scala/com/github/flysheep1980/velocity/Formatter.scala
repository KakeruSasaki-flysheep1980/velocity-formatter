package com.github.flysheep1980.velocity

import java.io._
import io.Source

class Formatter(val lineSeparator: String, val indentString: String) extends NodeSplitter with NodeBuilder {
  def format(input: String): (String, FormatResult) = {
    val (output, result) = build(split(input))
    (output, FormatResult(result.indentLevel))
  }
}

case class FormatResult(indentLevel: Int)

case class FormatConfig(overwrite: Boolean = false, encodeCharset: String = "utf-8", indentString: String = "\t", lineSeparator: String = "\n") {
  override def toString: String = "overwrite[%s], encodeCharset[%s], indentString[%s], lineSeparator[%s]".format(overwrite, encodeCharset, indentString, lineSeparator)
}

object Formatter {

  private val DefaultEncodeCharset = "utf-8"
  private val DefaultLineSeparator = "\n"
  private val DefaultIndentString = "\t"

  def formatString(str: String, encodeCharset: String, lineSeparator: String, indentString: String): (String, FormatResult) = {
    new Formatter(lineSeparator, indentString).format(str)
  }

  def formatFile(file: File, encodeCharset: String = DefaultEncodeCharset, lineSeparator: String = DefaultLineSeparator, indentString: String = DefaultIndentString): FormatResult = {
    val input = Source.fromFile(file, encodeCharset).getLines.toList.mkString(lineSeparator)
    val (output, result) = formatString(input, encodeCharset, lineSeparator, indentString)

    val writer = new OutputStreamWriter(new FileOutputStream(file), encodeCharset)
    writer.write(output)
    writer.flush()
    writer.close()

    result
  }

  def format(file: File, config: FormatConfig): FormatResult = {
    if (config.overwrite) {
      formatFile(file, config.encodeCharset, config.lineSeparator, config.indentString)
    } else {
      formatFileNoOverwrite(file, config.encodeCharset, config.lineSeparator, config.indentString)
    }
  }

  def formatFileNoOverwrite(file: File, encodeCharset: String = DefaultEncodeCharset, lineSeparator: String = DefaultLineSeparator, indentString: String = DefaultIndentString): FormatResult = {
    val input = Source.fromFile(file, encodeCharset).getLines.toList.mkString(lineSeparator)
    val (formatted, result) = formatString(input, encodeCharset, lineSeparator, indentString)

    //    val output = new File("output.dat")
    //    val writer = new OutputStreamWriter(new FileOutputStream(output), encodeCharset)
    //    writer.write(formatted)
    //    writer.flush()
    //    writer.close()

    result
  }

  /**
   * フォーマットをする.
   *
   * [引数]
   *  ディレクトリが指定された場合は、そのディレクトリ内の拡張子vmのファイルが対象となる.
   *  ファイルが指定された場合は、そのファイルが対象となる.（拡張子は問わない）
   *  複数指定することも可能.
   *  絶対パスで指定すること.
   *
   * [フォーマット設定]
   *  Java起動時のオプション（jvm引数）で指定することができます.
   *  "format.show.only" : フォーマット異常なファイルを表示するだけでファイルのフォーマットはしない（Default: true）
   *  "format.encode.charset" : 文字コード（Default: utf-8）
   *  "format.indent.string" : インデント文字列（Default: \t）
   *  "format.line.separator" : 改行文字（Default: \n）
   */
  def main(args: Array[String]) {
    println("velocity formatter execute...")

    // 引数が無い場合はエラー
    if (args.isEmpty) throw new IllegalArgumentException("target file or directory must be set.")

    // format対象のファイル
    val files = {
      args.map(new File(_)).toSet.map { t: File =>
        t match {
          case f if f.exists() == false => throw new FileNotFoundException("file[%s] no found.".format(f.getAbsolutePath))
          case f if f.isDirectory => {
            val lists = listInDirectory(f)
            lists.filter(_.getName.endsWith(".vm"))
          }
          case _@ f => List(f)
        }
      }.flatten
    }
    println("target is [%d] files.".format(files.size))

    // format設定
    val config = {
      import scala.util.control.Exception.allCatch
      val overwrite = getSystemProperty(JVMKeys.ShowOnly).flatMap(prop => allCatch.opt(!prop.toBoolean)).getOrElse(false)
      val encodeCharset = getSystemProperty(JVMKeys.EncodeCharset).getOrElse("utf-8")
      val indentString = getSystemProperty(JVMKeys.IndentString).getOrElse("\t")
      val lineSeparator = getSystemProperty(JVMKeys.LineSeparator).getOrElse("\n")
      FormatConfig(overwrite, encodeCharset, indentString, lineSeparator)
    }
    //    println("format configuration is [%s]".format(config))

    // formatした結果
    val results = files.map { file =>
      val formatResult = format(file, config)
      if (formatResult.indentLevel != 0) Some(file) else None
    }.flatten

    // 出力
    val size = results.size
    if (size == 0) {
      println("all file is valid format.")
    } else {
      System.err.println("invalid files found. size is [%d]. see below.".format(size))
      System.err.println("---------------------------------------------")
      results.foreach { file =>
        System.err.println(file.getAbsolutePath)
      }
      System.err.println("---------------------------------------------")
    }
  }

  /** JVM引数から値を取得する. */
  private def getSystemProperty(key: String): Option[String] = Option(System.getProperty(key))

  /** 指定したディレクトリにあるファイルを再帰的に得る. */
  private def listInDirectory(dir: File): List[File] = {
    val list = dir.listFiles(new FileFilter {
      def accept(pathname: File): Boolean = true
    })
    list.toList.map { f =>
      if (f.isDirectory) listInDirectory(f) else List(f)
    }.flatten
  }

  private object JVMKeys {
    val ShowOnly = "format.show.only"
    val EncodeCharset = "format.encode.charset"
    val IndentString = "format.indent.string"
    val LineSeparator = "format.line.separator"
  }

}
