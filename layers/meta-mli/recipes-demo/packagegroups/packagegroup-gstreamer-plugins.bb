DESCRIPTION = "Python package requirements for MLI"

LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-nvarguscamerasrc \
    gstreamer1.0-plugins-nvv4l2camerasrc \
    gstreamer1.0-plugins-nveglgles \
    gstreamer1.0-plugins-nvipcpipeline \
    gstreamer1.0-plugins-nvjpeg \
    gstreamer1.0-plugins-nvvideo4linux2 \
    gstreamer1.0-plugins-nvvideosinks \
    gstreamer1.0-plugins-nvtee \
    gstreamer1.0-plugins-nvdrmvideosink \
    gstreamer1.0-plugins-nvunixfd \
    gstreamer1.0-plugins-nvvidconv \
    gstreamer1.0-plugins-nvcompositor \
    gstreamer1.0-plugins-tegra-binaryonly \
"
