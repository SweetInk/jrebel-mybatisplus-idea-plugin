# jrebel-mybatisplus-idea-plugin
JRebel MybatisPlus hot reloading extension for IntelliJ

# Change logs

## `0.0.8`
Compatible with IntelliJ IDEA 2026.2.2 [#36](https://github.com/SweetInk/jrebel-mybatisplus/issues/36)

## `0.0.7`
Compatible with mybatis-plus 3.5.7 [#12](https://github.com/SweetInk/jrebel-mybatisplus-idea-plugin/issues/12)

Compatible with mybatis-plus 3.5.6 [#13](https://github.com/SweetInk/jrebel-mybatisplus-idea-plugin/issues/13)

## `0.0.6`
Compatible with mybatis-plus 3.5.6 [#10](https://github.com/SweetInk/jrebel-mybatisplus-idea-plugin/issues/10)

## `0.0.5`
Compatible with mybatis-plus 3.4.0+ [#5](https://github.com/SweetInk/jrebel-mybatisplus-idea-plugin/issues/5)

Some small refactorings

## `0.0.4`

Support Tomcat Run/Debug Configuration

Add Debug console log output

Fixed NPE when both configuration and configurationLocation are null

Fixed JVM class verification error [#14](https://github.com/SweetInk/jrebel-mybatisplus/issues/14)

Add log outputs when markFile create failed.


## `0.0.3`
Compatible with `mybatis-plus-3.4.0`

## `0.0.2`
Fixed NPE for MybatisSqlSessionFactoryBean#buildSessionFactory in `mybatis-plus-3.2.0`

## `0.0.1`
Simple integrate with Intellij

# Distribution

1. You can download from [here](https://plugins.jetbrains.com/plugin/12682-jrebel-mybatisplus-extension)

2. Open your `Intellij IDEA` ,find the `Settings->Plugins-Marketplace` ,then search the `JRebel mybatisPlus extension`

# Building from source

## Requirements

- JDK 8
- A local IntelliJ IDEA installation, used as the plugin SDK
- Maven (only for building the runtime plugin, see step 2)

## Steps

1. Point the build at your own IntelliJ IDEA installation. `build.gradle` ships with a
   hard-coded Windows `localPath`, so you must change it. Either set `localPath`
   to your IDE directory, or remove that line and rely on `version` instead.
   (If both are set, `localPath` wins — the build prints
   `Both 'localPath' and 'version' specified, second would be ignored`.)

   ```groovy
   intellij {
       localPath "/path/to/your/IntelliJ IDEA"   // or: version '2018.3.2'
   }
   ```

2. Build the runtime plugin this extension injects. The actual hot-reload logic
   lives in a separate project,
   [jrebel-mybatisplus](https://github.com/SweetInk/jrebel-mybatisplus) — a
   JRebel plugin built with Maven. This repository is only the IntelliJ side:
   it patches the JVM start-up arguments to load that jar, so the jar must be
   in your local Maven repository before this project can build.

   ```bash
   git clone https://github.com/SweetInk/jrebel-mybatisplus
   cd jrebel-mybatisplus
   mvn clean install
   ```

   The version you install has to match the one this build depends on,
   `online.githuboy:jr-mybatisplus:1.0.7`, as declared in `build.gradle`.
   It is not published to Maven Central.

3. Build the plugin:

   ```bash
   ./gradlew build      # macOS / Linux
   gradlew.bat build    # Windows
   ```

   The installable archive is written to
   `build/distributions/jr-mp-ide-idea-<version>.zip`.

4. To try the plugin in a sandbox IDE without installing it:

   ```bash
   ./gradlew runIde
   ```

> **Note:** `org.jetbrains.intellij` is pinned to `0.7.3`, the last version
> compatible with this project's Gradle 6.7 + JDK 8 toolchain. Do not downgrade
> it to `0.6.x` — those releases depend on `structure-*` artifacts that were
> published to Bintray, which has since been shut down, so they can no longer
> be resolved.
