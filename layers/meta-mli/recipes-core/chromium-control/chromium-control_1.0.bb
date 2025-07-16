LICENSE = "CLOSED"
SUMMARY = "Power Control Service for Control of Power States"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"

inherit systemd

DEPENDS += "xuser-account"
RDEPENDS:${PN} += "bash xuser-account chromium-x11"

SYSTEMD_SERVICE:${PN} = "run_chromium.service"
SYSTEMD_AUTO_ENABLE = "enable"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://run_chromium.service \
    file://chromium_control.sh \
    file://chromium \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/run_chromium.service \
    /usr/local/bin/chromium_control.sh \
    /usr/config/chromium \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/run_chromium.service ${D}${systemd_unitdir}/system

    install -d ${D}/usr/local/bin/
    install -m 0755 ${UNPACKDIR}/chromium_control.sh ${D}/usr/local/bin

    install -d ${D}/usr/config
    install -m 0660 ${UNPACKDIR}/chromium ${D}/usr/config/chromium
    chown xuser:chromiumctl ${D}/usr/config/chromium
}

inherit useradd
USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = ""
GROUPADD_PARAM:${PN} = "\
    --gid 796 --system chromiumctl; \
"
