LICENSE = "CLOSED"
SUMMARY = "Setup of Nilfisk CAN Bus Communication"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"

inherit systemd

RDEPENDS:${PN} += "bash can-utils devmem2"

SYSTEMD_SERVICE:${PN} = "nilfisk_can_service.service"
SYSTEMD_AUTO_ENABLE = "enable"
    
S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://nilfisk_can_service.service \
    file://setup_can0.sh \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/nilfisk_can_service.service \
    ${sysconfdir}/nilfisk/setup_can0.sh \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/nilfisk_can_service.service ${D}${systemd_unitdir}/system

    install -d ${D}${sysconfdir}/nilfisk
    install -m 0755 ${UNPACKDIR}/setup_can0.sh ${D}${sysconfdir}/nilfisk/setup_can0.sh
}
