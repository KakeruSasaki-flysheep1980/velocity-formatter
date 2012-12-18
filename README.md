velocity-formatter
==================

# Velocity Template Formatter

## Usage

### 1. build plugin

Clone this project.

```git clone velocity-formatter```

```sbt publish-local```

### 2. install plugin

Edit your project's ```plugins.sbt``` and ```Build.scala```.

```
Resolver.file("Local Ivy2 Repository", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.flysheep1980" % "velocity-formatter-plugin" % "0.1-SNAPSHOT")
```

```
import com.github.flysheep1980.velocity.plugin.VelocityFormatterPlugin
val xxx = Project(...).settings(VelocityFormatterPlugin.velocityFormatSettings: _*).settings(
  VelocityFormatterPlugin.velocitySourceDirectory <<= baseDirectory(_ / "resources" / "vm"),
  VelocityFormatterPlugin.velocityFormatConfig := VelocityFormatterPlugin.VelocityFormatterConfig()
)
```
