DESCRIPTION = "Blacklist kernel modules from auto-loading"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = ""

MODULE_BLACKLIST ?= "option"

do_install() {
    install -d ${D}${sysconfdir}/modprobe.d
    for m in ${MODULE_BLACKLIST}; do
        echo "blacklist $m" >> ${D}${sysconfdir}/modprobe.d/${PN}.conf
    done
}

FILES:${PN} = "${sysconfdir}/modprobe.d/${PN}.conf"
