ERROR_QA:remove = "patch-status"
WARN_QA:append = " patch-status"

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI:append = "\
    file://0001-Fix-Build-Error.patch \
"
