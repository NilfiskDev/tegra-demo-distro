SUMMARY = "Fake hardware clock service for systems without a real-time clock"
LICENSE = "CLOSED"

inherit systemd

SYSTEMD_SERVICE:${PN} = "fake-hwclock.service"
SYSTEMD_AUTO_ENABLE = "enable"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://fake-hwclock.service \
    file://fake-hwclock-tick.service \
    file://fake-hwclock-tick.timer \
    file://fake-hwclock \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/fake-hwclock.service \
    ${systemd_unitdir}/system/fake-hwclock-tick.service \
    ${systemd_unitdir}/system/fake-hwclock-tick.timer \\
    /usr/local/bin/fake-hwclock \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/fake-hwclock.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/fake-hwclock-tick.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/fake-hwclock-tick.timer ${D}${systemd_unitdir}/system

    install -d ${D}/usr/local/bin/
    install -m 0755 ${UNPACKDIR}/fake-hwclock ${D}/usr/local/bin
}
