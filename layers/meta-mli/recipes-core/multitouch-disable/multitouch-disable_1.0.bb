LICENSE = "CLOSED"
SUMMARY = "Multitouch Disable Service for Disabling Multitouch Functionality on Certain Touchscreens"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"

inherit systemd

DEPENDS += "python3-evdev"

SYSTEMD_SERVICE:${PN} = "touchpad_service.service"
SYSTEMD_AUTO_ENABLE = "enable"
    
S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://touchpad_service.service \
    file://multitouch_disable.py \
    file://90-touchscreen.conf \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/touchpad_service.service \
    /usr/local/bin/multitouch_disable.py \
    /usr/share/X11/xorg.conf.d/90-touchscreen.conf \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/touchpad_service.service ${D}${systemd_unitdir}/system

    install -d ${D}/usr/local/bin
    install -m 0750 ${UNPACKDIR}/multitouch_disable.py ${D}/usr/local/bin

    install -d ${D}/usr/share/X11/xorg.conf.d
    install -m 0644 ${UNPACKDIR}/90-touchscreen.conf ${D}/usr/share/X11/xorg.conf.d
}
