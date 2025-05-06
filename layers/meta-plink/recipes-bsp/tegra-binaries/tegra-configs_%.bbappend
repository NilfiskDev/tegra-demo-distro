FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI += "file://drivers-custom.csv"

do_install:append() {
    install -m 0644 ${UNPACKDIR}/drivers-custom.csv ${D}${sysconfdir}/nvidia-container-runtime/host-files-for-container.d/drivers.csv
}
