FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI:append = "\
    file://nvfancontrol.conf \
"

CUSTOM_INSTALL_DIR := "${THISDIR}/${BPN}"
do_install:append() {
    install -m 0644 ${CUSTOM_INSTALL_DIR}/nvfancontrol.conf ${D}${sysconfdir}/nvfancontrol.conf
}
