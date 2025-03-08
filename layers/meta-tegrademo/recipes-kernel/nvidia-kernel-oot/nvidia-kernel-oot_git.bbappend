FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI:append:$jetson-orin-nano-plink-11f1e2 = "\
    file://y-c11f1e2-orin-nano-364-04.dtb \
"

CUSTOM_DIR := "${THISDIR}/${BPN}"
do_sign_dtbs:append:jetson-orin-nano-plink-11f1e2() {
    # Copy file ${STAGING_DIR_HOST}/boot/devicetree
    install -m 0644 ${CUSTOM_DIR}/${KERNEL_DEVICETREE} ${B}/kernel-devicetree/generic-dts/dtbs/
}
