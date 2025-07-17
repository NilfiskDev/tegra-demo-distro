LICENSE = "CLOSED"
SUMMARY = "Files Necessary for Nilfisk MLI"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"
    
S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://interfaces \
    file://99-usb-camera.rules \
    file://NetworkManager.conf \
    file://CellCon.nmconnection \
"

FILES:${PN} += "\
    ${sysconfdir}/network/interfaces \
    ${sysconfdir}/udev/rules.d/99-usb-camera.rules \
    ${sysconfdir}/NetworkManager/NetworkManager.conf \
    ${sysconfdir}/NetworkManager/system-connections/CellCon.nmconnection \
"

do_install() {
    install -d ${D}${sysconfdir}/network
    install -m 0644 ${UNPACKDIR}/interfaces ${D}${sysconfdir}/network/interfaces

    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${UNPACKDIR}/99-usb-camera.rules ${D}${sysconfdir}/udev/rules.d/99-usb-camera.rules

    install -d ${D}${sysconfdir}/NetworkManager
    install -m 0644 ${UNPACKDIR}/NetworkManager.conf ${D}${sysconfdir}/NetworkManager/NetworkManager.conf

    install -d ${D}${sysconfdir}/NetworkManager/system-connections
    install -m 0600 ${UNPACKDIR}/CellCon.nmconnection ${D}${sysconfdir}/NetworkManager/system-connections/CellCon.nmconnection
}
