FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

ERROR_QA:remove = "patch-status"
WARN_QA:append = " patch-status"

SRC_URI += "file://0001-Remove-Shell-Support-and-UEFI-Timeout.patch;patchdir=../edk2-nvidia"
