DESCRIPTION = "A kernel module to create V4L2 loopback devices"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI = "git://github.com/umlaeute/v4l2loopback.git;protocol=https;nobranch=1;tag=v0.12.5"

SRCREV = "af1be06354eb2faa56bb3ce82fcd18e641fac89b"

S = "${WORKDIR}/git"

inherit module

MODULES_INSTALL_TARGET = "install-all"
EXTRA_OEMAKE += "KERNEL_DIR=${STAGING_KERNEL_DIR}"
EXTRA_OEMAKE += "PREFIX=${D}${prefix}"

DEPENDS += "help2man-native"

PACKAGES += "${PN}-utils"
FILES:${PN}-utils = "${bindir}/v4l2loopback-ctl"

RDEPENDS:${PN}-utils += " \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    sudo \
    v4l-utils \
    "
