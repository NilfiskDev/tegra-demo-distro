LICENSE = "CLOSED"
SUMMARY = "Service to start/stop Docker Compose applications"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"

inherit systemd

DEPENDS += "docker-compose"

SYSTEMD_SERVICE:${PN} = "compose_run.service"
SYSTEMD_AUTO_ENABLE = "enable"
    
S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://compose_run.service \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/compose_run.service \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/compose_run.service ${D}${systemd_unitdir}/system
}
