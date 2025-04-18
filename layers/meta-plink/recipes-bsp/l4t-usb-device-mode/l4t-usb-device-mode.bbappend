FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "file://99-usb-network.rules"

do_install() {
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${S}/l4tbr0.netdev ${D}${sysconfdir}/systemd/network/
    install -m 0644 ${S}/70-l4tbr0.network ${D}${sysconfdir}/systemd/network/
    install -m 0644 ${S}/70-l4t-usb-gadget.network ${D}${sysconfdir}/systemd/network/
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${S}/99-usb-network.rules ${D}${sysconfdir}/udev/rules.d/
}
