PACKAGECONFIG = ""

FILESEXTRAPATHS:prepend := "${THISDIR}/systemd-conf:"
SRC_URI += "\
    file://timesyncd.conf \
"

do_install:append() {
    install -d ${D}${sysconfdir}/systemd/timesyncd.conf.d
    install -m0644 ${S}/timesyncd.conf ${D}${sysconfdir}/systemd/timesyncd.conf.d/ntp-servers.conf
}

FILES:${PN} += "${sysconfdir}/systemd/timesyncd.conf.d/ntp-servers.conf"
