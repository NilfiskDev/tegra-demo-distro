LICENSE = "CLOSED"

inherit systemd

SYSTEMD_SERVICE:${PN} = "networking.service"
SYSTEMD_AUTO_ENABLE = "enable"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://networking.service \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/networking.service \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/networking.service ${D}${systemd_unitdir}/system
}
