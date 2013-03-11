# Velocity Template Formatter

Velocityテンプレートファイルをフォーマットします。以下、フォーマット例です。

フォーマット前:
```
<!DOCTYPE html><html><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
<meta http-equiv="Cache-Control" content="no-cache">
</head>
<body style="background-color:#ffffff;color:#000000;">
<div id="container">
<span class="class1">hoge</span><br>
#set($numbers = [1...5])
#foreach($number in $numbers)
 $number<br>
#end
#set($linkUrl = "/hoge")
#*<span class="hoge">aa</span>*#<a href="${linkUrl}">hoge</a><br />
#* hoge
fuga
miso *#
hogehoge ## hogehogehoge
#foreach($i in $hoge.fuga($miso)) $i #end
#if($link.setAction("hogefuga").addQueryData("query1", "value1")) hogehoge #elseif($hogehoge()) fugafuga #else piyopiyo #end
</div></body></html>
```

フォーマット後:
```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
		<meta http-equiv="Cache-Control" content="no-cache">
	</head>
	<body style="background-color:#ffffff;color:#000000;">
		<div id="container">
			<span class="class1">
				hoge
			</span>
			<br>
			#set($numbers = [1...5])
			#foreach($number in $numbers)
				$number
				<br>
			#end
			#set($linkUrl = "/hoge")
			#*<span class="hoge">aa</span>*#
			<a href="${linkUrl}">
				hoge
			</a>
			<br />
			#* hoge
			   fuga
			   miso *#
			hogehoge## hogehogehoge
			#foreach($i in $hoge.fuga($miso))
				$i
			#end
			#if($link.setAction("hogefuga").addQueryData("query1", "value1"))
				hogehoge
			#elseif($hogehoge())
				fugafuga
			#else
				piyopiyo
			#end
		</div>
	</body>
</html>
```

# 使い方 - Sbt Plugin

## 手順

### 1. プラグインのビルド

プロジェクトをclone、ビルドし、ローカルにpublishします。

```
]# git clone velocity-formatter
]# cd velocity-formatter
]# sbt publish-local
```

### 2. プラグインのインストール

インストールしたいプロジェクトの ```plugins.sbt``` を以下のように修正します。

```
resolvers += Resolver.file("Local Ivy2 Repository", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.flysheep1980" % "velocity-formatter-plugin" % "0.1-SNAPSHOT")
```

インストールしたいプロジェクトの ```Build.scala``` を以下のように修正します。

```
import com.github.flysheep1980.velocity.plugin.VelocityFormatterPlugin

val xxx = Project(...).settings(VelocityFormatterPlugin.velocityFormatSettings: _*).settings(
  VelocityFormatterPlugin.velocitySourceDirectory <<= baseDirectory(_ / "resources" / "vm"),
  VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
)
```

### 3. フォーマットの実行

```
]# sbt velocity-format
```

フォーマット不正なファイル一覧を出力する場合は以下のようにします。

```
]# sbt velocity-invalid-format-list
```

## フォーマット設定について

* Velocityテンプレートファイルがあるディレクトリのパス. (必須)

```
// ${project_root}/resources/vm
VelocityFormatterPlugin.velocitySourceDirectory <<= baseDirectory(_ / "resources" / "vm"),
```

* インデント文字列. ```デフォルト: \t```

```
// indent string is space
VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
  .setConfig(VelocityFormatterConfigKey.IndentString, " ")
```

* Velocityテンプレートファイルの文字コード. ```デフォルト: utf-8```

```
// encode charset euc-jp
VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
  .setConfig(VelocityFormatterConfigKey.EncodeCharset, "euc-jp")
```

# 使い方 - Jar

## 手順

### 1. プラグインのビルド、jarの作成

```
]# git clone velocity-formatter
]# cd velocity-formatter
]# sbt
> project core
> one-jar
```

jarは ```core/target/scala-2.9.1/velocity-formatter_2.9.1-xxxxxxxx-one-jar.jar``` に作成されます。

### 2. フォーマットの実行

フォーマット不正なファイル一覧を出力する場合は以下のようにします。

```
]# java -jar {path_to_jar} {path_of_template_directory or file} {path_of_template_directory or file}...
```

フォーマットするには以下のようにします。

```
]# java -Dformat.show.only=false -jar {path_to_jar} {path_of_template_directory or file}...
```

## フォーマット設定

```VM Argument``` で設定します。

* format.show.only ```デフォルト: true```. 実際にファイルをフォーマットするには ```false``` として下さい。

* format.encode.charset ```デフォルト: utf-8```

* format.indent.string ```デフォルト: \t```

* format.line.separator ```デフォルト: \n```

## Antで実行

以下のように ```Build.xml``` を編集し、ターゲットを呼び出して下さい。

```
<target name="some_target_name">
 <java jar="path_of_jar" fork="true" logError="true">
   <arg value="path_of_template_directory or file" />
   <jvmarg value="-Dshow.only=true" />
 </java>
</target>
```