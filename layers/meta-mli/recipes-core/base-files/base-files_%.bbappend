FILESEXTRAPATHS:prepend := "${THISDIR}/base-files:"
SRC_URI += "\
    file://fstab_custom \
"

do_install:append() {
    # Install custom fstab
    install -m 0644 ${UNPACKDIR}/fstab_custom ${D}${sysconfdir}/fstab
}
