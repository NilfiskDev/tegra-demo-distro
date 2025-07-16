FILESEXTRAPATHS:prepend := "${THISDIR}/tegra-minimal-init:"
SRC_URI += "file://init-boot-custom.sh"

do_install:append(){
    install -m 0755 ${UNPACKDIR}/init-boot-custom.sh ${D}/init
}
