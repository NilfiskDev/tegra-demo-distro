DESCRIPTION = "Tegra demo base image"

require demo-image-common.inc

# Install networking dependencies
CORE_IMAGE_BASE_INSTALL += "networkmanager modemmanager ethtool backport-iwlwifi"

# Add iotedge and aziot-edged
CORE_IMAGE_BASE_INSTALL += "iotedge aziot-edged"

# Add X11 Server and dependencies
# CORE_IMAGE_BASE_INSTALL += "packagegroup-core-x11"
EXTRA_IMAGE_FEATURES += "x11"
EXTRA_IMAGE_FEATURES += "package-management"

# Add Virtualization Components
CORE_IMAGE_BASE_INSTALL += "nvidia-docker docker-compose"

# Add nvidia jetpack runtime components
CORE_IMAGE_BASE_INSTALL += "nvidia-docker cuda-libraries opencv cudnn tensorrt-core libnvvpi3"

# Add misc image components
CORE_IMAGE_BASE_INSTALL += " \
    xdg-utils \
    chromium-x11 \
"
