DESCRIPTION = "MLI Production Image"

require tegra-image-common.inc

IMAGE_OVERHEAD_FACTOR = "1.05"
EXTRA_IMAGE_FEATURES += "read-only-rootfs"
IMAGE_FSTYPES += "tar.gz"

# Install networking dependencies
CORE_IMAGE_BASE_INSTALL += "\
    networkmanager \
    modemmanager \
    ethtool \
    backport-iwlwifi \
    net-tools \
    ufw \
    networking-service \
    iproute2 \
    ifupdown \
"

# Add iotedge, aziot-edged, and ADU
CORE_IMAGE_BASE_INSTALL += "\
    iotedge \
    aziot-edged \
    adu-agent-service \
"

# Add X11 Server and dependencies
IMAGE_INSTALL += "\
    xserver-xorg \
    xserver-xorg-extension-dri \
    xserver-xorg-extension-dri2 \
    xf86-video-fbdev \
    xf86-video-modesetting \
    packagegroup-core-x11-utils \
"
EXTRA_IMAGE_FEATURES += "x11 package-management"

# Add Virtualization Components
CORE_IMAGE_BASE_INSTALL += "nvidia-docker docker-compose"

# Add nvidia jetpack runtime components
CORE_IMAGE_BASE_INSTALL += "\
    cuda-libraries \
    opencv \
    v4l-utils \
    cudnn \
    tensorrt-core \
    tegra-mmapi \
    libnvvpi3 \
    tegra-libraries-vulkan \
    tegra-libraries-multimedia-v4l \
    packagegroup-gstreamer-plugins \
    pva-allow-2 \
"

# Add misc image components
CORE_IMAGE_BASE_INSTALL += " \
    polkit \
    polkit-group-rule-datetime \
    polkit-group-rule-location \
    ttf-dejavu-sans \
    ttf-dejavu-serif \
    dpkg \
    tailscale \
    xdg-utils \
    chromium-x11 \
    packagegroup-python3 \
    devmem2 \
    e2fsprogs-resize2fs \
    fake-hwclock \
    tzdata \
    can-utils \
    vulkan-headers \
    xuser-account \
    zram \
"

IMAGE_INSTALL += "\
    ffmpeg \
    sudo \
"

# Add application components
CORE_IMAGE_BASE_INSTALL += "\
    data-overlay-setup \
    nvidia-user \
    power-control \
    update-service \
    multitouch-disable \
    compose-run \
    nilfisk-can \
    chromium-control \
"

# Configure users
inherit extrausers
EXTRA_USERS_PARAMS:append = "\
    usermod -a -G chromiumctl xuser; \
    usermod -a -G chromiumctl nvidia; \
"
