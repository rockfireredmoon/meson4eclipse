[Release History](#release-history) | 
[Install...](#installation-instructions) | 
[![Mailing-list](https://img.shields.io/badge/Mailing-list-blue.svg)](http://groups.google.com/d/forum/cmake4eclipse-users)
[![Build Status](https://travis-ci.org/15knots/cmake4eclipse.svg?branch=master)](https://travis-ci.org/15knots/cmake4eclipse)
[![GitHub issues](https://img.shields.io/github/issues/15knots/cmake4eclipse.svg)](https://github.com/15knots/cmake4eclipse/issues)


# Introduction
The [CMake Wiki](https://cmake.org/Wiki/CMake:Eclipse_UNIX_Tutorial#CMake_with_Eclipse) mentions the options to use CMake with Eclipse.
This Eclipse plugin offers an option to **automatically** generate buildscripts for the Eclipse CDT managed build system from your CMake scripts. 

# Why cmake4eclipse?
Blindly invoked, CMake will generate makefiles (or other build scripts) inside the source tree, cluttering it with lots of files and directories that have to be fleed out from version control: This practice might be ok for simple hello-world-projects, but for larger projects, the CMake developers recommend _You_ to set up a separate directory for building the source.

Annoyingly, these recommended out-of-source-builds impose some tedious tasks on Your co-workers who check out the code and just want to build it:
  1. leave eclipse workbench,
  1. manually fire up a command-line shell,
  1. manually create a directory for the out-of-source-build,
  1. manually change the CWD to that directory,
  1. manually invoke cmake, telling it to generate build scripts, which kind of build scripts you want and where source source files live,
  1. re-enter eclipse workbench, configure the checked out project to use the generated buildscripts.

**Cmake4eclipse** aims to address these tasks: Co-workers can just check out the source and have all the tedious tasks automated.

## Screenshots
Screenshots can be found at the <a href="https://marketplace.eclipse.org/content/cmake4eclipse#group-screenshots" title="Screenshots">Eclipse Marketplace</a>.

# Quick start
 1. If you do not have any existing code, check out the [Sample Projects](https://github.com/15knots/cmake4eclipse-sample-projects), chose one and fill in your code.
 1. If you have an existing C/C++ project code, inside Eclipse, goto `Help:Help Contents`, then read the `CMake for CDT User Guide:Getting Started` node to adjust your project settings.
 
## License
The plugin is licensed under the <a href="http://www.eclipse.org/legal/epl-v10.html">Eclipse Public License Version 1.0 ("EPL")</a>.

# Installation Instructions
The easiest way is to drag this: <a href="http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=2318334" class="drag" title="Drag to your running Eclipse workbench to install cmake4eclipse">
<img class="img-responsive" src="https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png" alt="Drag to your running Eclipse workbench to install" /></a> to your running Eclipse workbench.

Alternatively, **cmake4eclipse** can be installed into Eclipse using the "Install New Software..." dialog and entering the update site URL listed below.

### Update Site
This composite update site contains the latest release as well as selected older releases.
https://raw.githubusercontent.com/15knots/cmake4eclipse/master/releng/comp-update/

### Debug and Build
This project uses Apache maven as its build system.
To build from a command-line, run `mvn -f ./parent/pom.xml verify` in the root directory of the project source files.

There is a run configuration for eclipse to invoke maven `build cmake4eclipse` plus a launch configuration to debug the plugin: `cmake4eclipse`.

---
# Release History
## 0.0.1 (2018-1-12)
#### Repository URL
`jar:https://googledrive.com/host/0B-QU1Qnto3huZUZ0QUdxM01pR0U/cmake4eclipsecdt-1.0.0.zip!/`
#### System Requirements
 CDT v 8.1.0 or higher and Eclipse v 3.8.0 (Juno) or higher

---

