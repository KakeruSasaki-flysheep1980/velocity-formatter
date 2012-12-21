# Velocity Template Formatter

Format velocity template. See below example:

Before:
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

After:
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

## Usage

### 1. Build plugin

Clone and build this project, and publish-local.

```
> git clone velocity-formatter```
> cd velocity-formatter
> sbt publish-local
```

### 2. Install plugin

Edit your project's ```plugins.sbt```.

```
resolvers += Resolver.file("Local Ivy2 Repository", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.flysheep1980" % "velocity-formatter-plugin" % "0.1-SNAPSHOT")
```

Edit your project's ```Build.scala```.

```
import com.github.flysheep1980.velocity.plugin.VelocityFormatterPlugin

val xxx = Project(...).settings(VelocityFormatterPlugin.velocityFormatSettings: _*).settings(
  VelocityFormatterPlugin.velocitySourceDirectory <<= baseDirectory(_ / "resources" / "vm"),
  VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
)
```

### 3. Run to format

```
> sbt velocity-format
```

## Format Configurations

* Directory of velocity template files

```
// ${project_root}/resources/vm
VelocityFormatterPlugin.velocitySourceDirectory <<= baseDirectory(_ / "resources" / "vm"),
```

* Indent of format. ```default: \t```

```
// indent string is space
VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
  .setConfig(VelocityFormatterConfigKey.IndentString, " ")
```

* Encode charset. ```default: utf-8```

```
// encode charset euc-jp
VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
  .setConfig(VelocityFormatterConfigKey.EncodeCharset, "euc-jp")
```
