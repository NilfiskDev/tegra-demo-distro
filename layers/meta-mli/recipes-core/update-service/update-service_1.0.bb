LICENSE = "CLOSED"
SUMMARY = "Update Control Service for Control of Application Updates"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"

inherit systemd

DEPENDS += "python3-docker python3-fastapi"

SYSTEMD_SERVICE:${PN} = "update_service.service"
SYSTEMD_AUTO_ENABLE = "enable"
    
S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://update_service.service \
    file://update_service.py \
    file://update_request.py \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/update_service.service \
    /usr/local/bin/update_service.py \
    /usr/local/bin/update_request.py \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/update_service.service ${D}${systemd_unitdir}/system

    install -d ${D}/usr/local/bin
    install -m 0750 ${UNPACKDIR}/update_service.py ${D}/usr/local/bin
    install -m 0750 ${UNPACKDIR}/update_request.py ${D}/usr/local/bin
}
