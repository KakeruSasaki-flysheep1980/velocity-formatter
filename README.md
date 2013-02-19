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

# Usage(sbt-plugin)

## Step

### 1. Build plugin

Clone and build this project, and publish-local.

```
]# git clone velocity-formatter
]# cd velocity-formatter
]# sbt publish-local
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
]# sbt velocity-format
```

Or, Run to show invalid format files.

```
]# sbt velocity-invalid-format-list
```

## Format Configurations

* Directory of velocity template files. (must be set)

```
// ${project_root}/resources/vm
VelocityFormatterPlugin.velocitySourceDirectory <<= baseDirectory(_ / "resources" / "vm"),
```

* Indent string of format. ```default: \t```

```
// indent string is space
VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
  .setConfig(VelocityFormatterConfigKey.IndentString, " ")
```

* Encode charset of file. ```default: utf-8```

```
// encode charset euc-jp
VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
  .setConfig(VelocityFormatterConfigKey.EncodeCharset, "euc-jp")
```

# Usage(jar execution)

## Step

### 1. Build plugin and publish jar

Clone and build this project, and one-jar.

```
]# git clone velocity-formatter
]# cd velocity-formatter
]# sbt
> project core
> one-jar
```

### 2. Run to format

Run to show invalid format files.

```
]# java -jar {path_to_jar} {path_of_template_directory or path_of_template_file}
```

Or, Run to format.

```
]# java -Dformat.show.only=false -jar {path_to_jar} {path_of_template_directory or path_of_template_file}
```

## Format Configurations

Set by ```VM Argument```

* format.show.only ```Default: true```

* format.encode.charset ```Default: utf-8```

* format.indent.string ```Default: \t```

* format.line.separator ```Default: \n```

## Using by Ant

Edit ```Build.xml``` as below, and Call target.

```
<target name="some_target_name">
 <java jar="path_of_jar" fork="true" logError="true">
   <arg value="path_of_template_directory or path_of_template_file" />
   <jvmarg value="-Dshow.only=true" />
 </java>
</target>
```