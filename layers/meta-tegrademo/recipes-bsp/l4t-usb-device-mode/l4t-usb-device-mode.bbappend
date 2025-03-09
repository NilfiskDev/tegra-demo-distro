FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "file://99-usb-network.rules"

do_install:append:jetson-orin-nano-plink-11f1e2() {
    install -m 0644 ${S}/99-usb-network.rules ${D}${sysconfdir}/udev/rules.d/
}
