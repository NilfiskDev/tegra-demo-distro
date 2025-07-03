DESCRIPTION = "Create usergroup network. All members off this group are allowed to modify networkmanager settings"
LICENSE = "CLOSED"

DEPENDS += "polkit"

inherit features_check
REQUIRED_DISTRO_FEATURES = "polkit"

# TODO: Require the user to be in a group
# inherit useradd

# S = "${WORKDIR}/sources"

SRC_URI = "file://50-org.freedesktop.NetworkManager1.Location.rules"

do_install() {
    install -m 755 -d ${D}${datadir}/polkit-1/rules.d
    install -D -m 0755 ${UNPACKDIR}/50-org.freedesktop.NetworkManager1.Location.rules ${D}${datadir}/polkit-1/rules.d
}

FILES:${PN} += "${datadir}/polkit-1/rules.d"

# USERADD_PACKAGES = "${PN}"
# GROUPADD_PARAM:${PN} = "--system network"
