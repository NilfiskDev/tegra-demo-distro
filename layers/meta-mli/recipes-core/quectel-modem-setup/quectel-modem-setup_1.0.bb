LICENSE = "CLOSED"
SUMMARY = "Quectel EM05-G Modem QMI Mode Setup"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"

inherit systemd

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://quectel-modem-setup.sh \
    file://quectel-modem-setup.service \
"

SYSTEMD_SERVICE:${PN} = "quectel-modem-setup.service"
SYSTEMD_AUTO_ENABLE = "enable"

FILES:${PN} += "\
    /usr/local/bin/quectel-modem-setup.sh \
    ${systemd_unitdir}/system/quectel-modem-setup.service \
"

do_install() {
    install -d ${D}/usr/local/bin
    install -m 0755 ${UNPACKDIR}/quectel-modem-setup.sh ${D}/usr/local/bin/quectel-modem-setup.sh

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/quectel-modem-setup.service ${D}${systemd_unitdir}/system/quectel-modem-setup.service
}
