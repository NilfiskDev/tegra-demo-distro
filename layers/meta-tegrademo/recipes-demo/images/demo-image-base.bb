DESCRIPTION = "Tegra demo base image"

require demo-image-common.inc

# Install networking dependencies
CORE_IMAGE_BASE_INSTALL += "networkmanager modemmanager ethtool backport-iwlwifi net-tools ufw"

# Add iotedge and aziot-edged
CORE_IMAGE_BASE_INSTALL += "iotedge aziot-edged"

# Add X11 Server and dependencies
EXTRA_IMAGE_FEATURES += "x11"
EXTRA_IMAGE_FEATURES += "package-management"

# Add Virtualization Components
CORE_IMAGE_BASE_INSTALL += "nvidia-docker docker-compose"

# Add nvidia jetpack runtime components
CORE_IMAGE_BASE_INSTALL += "cuda-libraries opencv cudnn tensorrt-core"

# Add misc image components
CORE_IMAGE_BASE_INSTALL += " \
    xdg-utils \
    chromium-x11 \
    packagegroup-python3 \
    v4l2loopback \
    devmem2 \
"

IMAGE_INSTALL += "ffmpeg"

# Configure users
inherit extrausers
IMAGE_INSTALL:append = " sudo"

NVIDIA_PASSWORD = "\$6\$qToCNITxIvqTSDSF\$UmckNUSMLOr7MLtLWhOCO6Jke2a..3qc5jntDUBRQBWZU8rA6/05U/KLNLuZI7fbrwFZ7pKL9628ioA59xOQS/"
EXTRA_USERS_PARAMS:append = "\
    useradd -u 1000 -d /home/nvidia -s /bin/sh -p '${NVIDIA_PASSWORD}' nvidia; \
    usermod -a -G sudo nvidia; \
    usermod -L -e 1 root; \
"
