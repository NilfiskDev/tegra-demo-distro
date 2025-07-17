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
    --home-dir /home/nvidia \
    --shell /bin/sh \
    --password '${NVIDIA_PASSWORD}' \
    --groups datetime,sudo,systemd-journal,shutdown \
    --user-group \
    nvidia ; \
"

ALLOW_EMPTY:${PN} = "1"
