LICENSE = "CLOSED"
SUMMARY = "NVIDIA Default User"
MAINTAINER = "Arteom Katkov <akatkov@nilfisk.com>"

DEPENDS += "polkit-group-rule-datetime systemd"
RDEPENDS:${PN} += "systemd polkit-group-rule-datetime"

inherit allarch useradd

do_configure() {
    :
}

do_compile() {
    :
}

do_install() {
    :
}

NVIDIA_PASSWORD = "\$6\$qToCNITxIvqTSDSF\$UmckNUSMLOr7MLtLWhOCO6Jke2a..3qc5jntDUBRQBWZU8rA6/05U/KLNLuZI7fbrwFZ7pKL9628ioA59xOQS/"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "\
    --uid 1000 \
    --create-home \
    --password '${NVIDIA_PASSWORD}' \
    --user-group \
    --groups datetime,sudo,systemd-journal,shutdown nvidia ; \
"

ALLOW_EMPTY:${PN} = "1"
