ERROR_QA:remove = "patch-status"
WARN_QA:append = " patch-status"

FILESEXTRAPATHS:prepend := "${THISDIR}/linux-yocto:"
SRC_URI += "\
    file://quectel_networking.cfg \
    file://plink_custom.cfg \
    file://0001-Add-CH9434-Serial-Driver-515.patch \
    file://0002-Enable-LAN78xx-LEDs-without-Device-Tree-Config.patch \
    file://0003-realsense-camera-formats-jammy-master.patch \
    file://0004-realsense-metadata-jammy-master.patch \
    file://0005-realsense-powerlinefrequency-control-fix-jammy.patch \
    file://firewall.cfg \
    file://intel-realsense.cfg \
"
