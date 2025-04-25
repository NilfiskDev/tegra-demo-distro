DESCRIPTION = "MLI Production Image"

require tegra-image-common.inc

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

# Add iotedge and aziot-edged
CORE_IMAGE_BASE_INSTALL += "iotedge aziot-edged"

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

IMAGE_INSTALL += "ffmpeg"

# Configure users
inherit extrausers
IMAGE_INSTALL:append = " sudo"

NVIDIA_PASSWORD = "\$6\$qToCNITxIvqTSDSF\$UmckNUSMLOr7MLtLWhOCO6Jke2a..3qc5jntDUBRQBWZU8rA6/05U/KLNLuZI7fbrwFZ7pKL9628ioA59xOQS/"
EXTRA_USERS_PARAMS:append = "\
    useradd -u 1000 -d /home/nvidia -s /bin/sh -p '${NVIDIA_PASSWORD}' nvidia; \
    usermod -a -G sudo,systemd-journal,shutdown nvidia; \
    groupadd --system chromiumctl; \
    usermod -a -G chromiumctl xuser; \
    usermod -a -G chromiumctl nvidia; \
"
