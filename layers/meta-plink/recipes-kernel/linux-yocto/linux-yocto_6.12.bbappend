ERROR_QA:remove = "patch-status"
WARN_QA:append = " patch-status"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI += "\
    file://0001-Add-CH9434-Serial-Driver.patch \
    file://0002-Update-Tegra-SPI-Serial-Driver.patch \
    file://quectel_networking.cfg \
    file://plink_custom.cfg \
"
