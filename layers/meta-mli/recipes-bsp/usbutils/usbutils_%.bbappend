do_install:append() {
    install -m 0755 ${B}/usbreset ${D}${bindir}/usbreset
}

FILES:${PN} += "${bindir}/usbreset"
