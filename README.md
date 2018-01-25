[Release History](#release-history) | 
[Install...](#installation-instructions) | 
[![GitHub issues](https://meson4eclipse.theanubianwar.com/meson4eclipse.svg)](https://github.com/rockfireredmoon/meson4eclipse/issues)


# Introduction
The [Meson Wiki](http://mesonbuild.com/IDE-integration.html) mentions the options to use Meson with an IDE.
This Eclipse plugin extends the CDT (C/C++ Development Tools) to generate Meson meta-data, extract project settings and invoke builds. It is based on a cross between [cmake4eclipse](https://github.com/15knots/cmake4eclipse), the CMake CDT plugin and various other CDT components.

# Why meson4eclipse?
When using Eclipse with a Meson project, without integration setup has to at best be duplicated in the project settings. Your builds will be inefficient, and things are worse when working in a team, with everyone having to setup their own projects leading to all kinds of possibile issues!

Using Meson and Meson4Eclipse, means you can have a single meson.build file in your SCM, and everyone's projects will be setup and built in the same way.

# Quick start
 1. Install as described below.
 1. Use one of the new C++ Project Wizards to either create a sample Meson project or add the Meson nature to an existing project using the Empty project wizard.

# Features

 * Makes use of CDT Core Build to provide simple integration for full builds, incremental builds and cleaning.
 * Meson syntax error markers.
 * Eclipse feature includes a Ninja editor too. You can also create raw Ninja projects if you are so inclined. This is OK for VERY small projects.
 * New project wizards for both Meson and Ninja 
 * WIP - Work has started on a managed build versions of both Meson and Ninja.

 
## License
The plugin is licensed under the <a href="http://www.eclipse.org/legal/epl-v10.html">Eclipse Public License Version 1.0 ("EPL")</a>.

# Installation Instructions
The easiest way is to drag this: <a href="http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=XXXXXXXXX" class="drag" title="Drag to your running Eclipse workbench to install meson4eclipse">
<img class="img-responsive" src="https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png" alt="Drag to your running Eclipse workbench to install" /></a> to your running Eclipse workbench.

Alternatively, **meson4eclipse** can be installed into Eclipse using the "Install New Software..." dialog and entering the update site URL listed below.

### Update Site
This composite update site contains the latest release as well as selected older releases.
https://raw.githubusercontent.com/rockfireredmoon/meson4eclipse/master/releng/comp-update/

### Debug and Build
This project uses Apache maven as its build system.
To build from a command-line, run `mvn -f ./parent/pom.xml verify` in the root directory of the project source files.

There is a run configuration for eclipse to invoke maven `build meson4eclipse` plus a launch configuration to debug the plugin: `meson4eclipse`.

---
# Release History
## 0.0.1 (2018-1-12)
#### System Requirements
 Java 8, CDT v 8.1.0 or higher and Eclipse v 4.6.0 (Neon) or higher

---

