FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "\
    file://99-tty0-3.rules \
    file://xuser.conf \
    file://99-nvidia-modeset.conf \
"

do_install:append() {
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${UNPACKDIR}/99-tty0-3.rules ${D}${sysconfdir}/udev/rules.d/99-tty0-3.rules

    install -d ${D}${sysconfdir}/tmpfiles.d
    install -m 0644 ${UNPACKDIR}/xuser.conf ${D}${sysconfdir}/tmpfiles.d/xuser.conf
    
    install -d ${D}${sysconfdir}/modules-load.d
    install -m 0644 ${UNPACKDIR}/99-nvidia-modeset.conf ${D}${sysconfdir}/modules-load.d/99-nvidia-modeset.conf
}

FILES:${PN} += "\
    ${sysconfdir}/udev/rules.d/99-tty0-3.rules \
    ${sysconfdir}/tmpfiles.d/xuser.conf \
    ${sysconfdir}/modules-load.d/99-nvidia-modeset.conf \
"
