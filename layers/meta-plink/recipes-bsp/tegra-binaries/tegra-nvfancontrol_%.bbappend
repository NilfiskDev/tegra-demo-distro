FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI:append:$jetson-orin-nano-plink-11f1e2 = "\
    file://nvfancontrol.conf \
"

CUSTOM_INSTALL_DIR := "${THISDIR}/${BPN}"
do_install:append:jetson-orin-nano-plink-11f1e2() {
    install -m 0644 ${CUSTOM_INSTALL_DIR}/nvfancontrol.conf ${D}${sysconfdir}/nvfancontrol.conf
}
