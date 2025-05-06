DESCRIPTION = "Python package requirements for MLI"

LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-tegra \
    gstreamer1.0-meta-video \
    gstreamer1.0-plugins-base-alsa \
"
