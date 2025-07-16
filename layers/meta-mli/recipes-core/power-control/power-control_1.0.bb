LICENSE = "CLOSED"
SUMMARY = "Power Control Service for Control of Power States"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"

# Nvidia has same UID/GID as host so we need to skip host contamination check
ERROR_QA:remove = "host-user-contaminated"
WARN_QA:append = " host-user-contaminated"

inherit systemd

DEPENDS += "nvidia-user"
RDEPENDS:${PN} += "nvidia-user bash"

SYSTEMD_SERVICE:${PN} = "power_control.service"
SYSTEMD_AUTO_ENABLE = "enable"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://power_control.service \
    file://power_control.sh \
    file://power \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/power_control.service \
    /usr/local/bin/power_control.sh \
    /usr/config/power \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/power_control.service ${D}${systemd_unitdir}/system

    install -d ${D}/usr/local/bin/
    install -m 0755 ${UNPACKDIR}/power_control.sh ${D}/usr/local/bin

    install -d ${D}/usr/config
    install -m 0700 ${UNPACKDIR}/power ${D}/usr/config/power
    chown nvidia:nvidia ${D}/usr/config/power
}
