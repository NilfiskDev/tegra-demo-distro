PACKAGECONFIG = ""

FILESEXTRAPATHS:prepend := "${THISDIR}/systemd-conf:"
SRC_URI += "\
    file://journald-custom.conf \
"

do_install:append() {
    # Install custom journald configuration
    install -D -m0644 ${S}/journald-custom.conf ${D}${systemd_unitdir}/journald.conf.d/00-${PN}.conf
}
