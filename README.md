# Velocity Template Formatter

## Usage

### 1. build plugin

Clone and build this project, and publish-local.

```git clone velocity-formatter```

```sbt publish-local```

### 2. install plugin

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

### 3. run to format

```
> sbt velocity-format
```

## Format Configurations

* Directory of velocity template files

```
// ${project_root}/resources/vm
VelocityFormatterPlugin.velocitySourceDirectory <<= baseDirectory(_ / "resources" / "vm"),
```

* Indent String of format. ```default: \t```

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
