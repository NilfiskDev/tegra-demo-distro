FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "\
    file://daemon_custom.json \
"

do_install:append() {
    install -m 0644 ${UNPACKDIR}/daemon_custom.json ${D}${sysconfdir}/docker/daemon.json
}
