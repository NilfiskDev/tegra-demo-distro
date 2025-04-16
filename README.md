# Plink BSP/MLI Distribution

Nilfisk MLI ditribution and Plink BSP files built
using Yocto Project tools and the [meta-tegra](https://github.com/OE4T/meta-tegra) BSP layer.

![Build status](https://builder.madison.systems/badges/tegrademo-master.svg)

Metadata layers are brought in as git submodules:

| Layer Repo            | Branch         | Description                                         |
| --------------------- | ---------------|---------------------------------------------------- |
| poky                  | master         | OE-Core from poky repo at yoctoproject.org          |
| meta-tegra            | master         | L4T BSP layer - L4T R36.4.3/JetPack 6.2             |
| meta-tegra-community  | master         | OE4T layer with additions from the community        |
| meta-openembedded     | master         | OpenEmbedded layers                                 |
| meta-virtualization   | master         | Virtualization layer for docker support             |
| meta-clang            | master         | Clang compiler support                              |
| meta-iotedge          | master         | Microsoft Iotedge support                           |
| meta-chromium         | master         | Chromium support                                    |

## Prerequisites

See the [Yocto Project Quick Build](https://docs.yoctoproject.org/brief-yoctoprojectqs/index.html)
documentation for information on setting up your build host.

For burning SDcards (for Jetson Xavier NX developer kits), the `bmap-tools`
package is recommended.

## Setting up

1. Clone this repository:

        $ git clone https://github.com/NilfiskDev/tegra-demo-distro.git

2. Switch to the appropriate branch, jetapck-x.x.x

3. Initialize the git submodules:

        $ cd tegra-demo-distro
        $ git submodule update --init

4. Source the `setup-env` script to create a build directory,
   specifying the MACHINE you want to configure as the default
   for your builds. For example, to set up a build directory
   called `build` that is set up for the Jetson Xavier NX
   developer kit and the default `tegrademo` distro:

        $ . ./setup-env --machine jetson-plink-11f1e2

   You can get a complete list of available options, MACHINE
   names, and DISTRO names with

        $ . ./setup-env --help

5. Optional: Install pre-commit hook for commit autosigning using
        $ ./scripts-setup/setup-git-hooks

## Distributions

Use the `--distro` option with `setup-env` to specify a distribution for your build,
or customize the DISTRO setting in your `$BUILDDIR/conf/local.conf` to reference one
of the supported distributions.

Currently supported distributions are listed below:


| Distribution name | Description                                                   |
| ----------------- | ------------------------------------------------------------- |
| mli               | Ditribution used for Nilfisk MLI Machines                     |

## Images

The `tegrademo` distro includes the following image recipes, which
are dervied from the `core-image-XXX` recipes in OE-Core but configured
for Jetson platforms. They include some additional test tools and
demo applications.

| Recipe name       | Description                                                   |
| ----------------- | ------------------------------------------------------------- |
| prod-image-mli    | Production MLI Image without Development Packages             |


### Update image demo

A [swupdate](https://sbabic.github.io/swupdate/) demo image is also available which supports
A/B rootfs updates to any of the supported images.  For details refer to
[layers/meta-tegrademo/dynamic-layers/meta-swupdate/README.md](layers/meta-tegrademo/dynamic-layers/meta-swupdate/README.md).

