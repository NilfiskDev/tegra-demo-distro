DESCRIPTION = "Python package requirements for MLI"

LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
    python3 \
    python3-pip \
    python3-evdev \
    python3-dbus-next \
"
