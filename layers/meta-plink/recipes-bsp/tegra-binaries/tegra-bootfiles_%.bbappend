# Hack: The fetch task is disabled on this recipe, so the following is just for the task signature.
FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI:append:$jetson-orin-nano-plink-11f1e2 = "\
    file://tegra234-mb1-bct-gpio-p3767-hdmi-c11.dtsi \
    file://tegra234-mb1-bct-pinmux-p3767-hdmi-c11.dtsi \
    file://tegra234-mb2-bct-misc-p3767-plink.dts \
    file://tegra234-bpmp-3701-0000-3737-0000.dtb \
    file://tegra234-mb1-bct-misc-common.dtsi \
"

# Hack: As the fetch task is disabled for this recipe, we have to directly access the files."
CUSTOM_DTSI_DIR := "${THISDIR}/${BPN}"
do_install:append:jetson-orin-nano-plink-11f1e2() {
    install -m 0644 ${CUSTOM_DTSI_DIR}/tegra234-mb1-bct-gpio-p3767-hdmi-c11.dtsi ${D}${datadir}/tegraflash/
    install -m 0644 ${CUSTOM_DTSI_DIR}/tegra234-mb1-bct-pinmux-p3767-hdmi-c11.dtsi ${D}${datadir}/tegraflash/
    install -m 0644 ${CUSTOM_DTSI_DIR}/tegra234-mb2-bct-misc-p3767-plink.dts ${D}${datadir}/tegraflash/
    install -m 0644 ${CUSTOM_DTSI_DIR}/tegra234-bpmp-3701-0000-3737-0000.dtb ${D}${datadir}/tegraflash/
    install -m 0644 ${CUSTOM_DTSI_DIR}/tegra234-mb1-bct-misc-common.dtsi ${D}${datadir}/tegraflash/
}
