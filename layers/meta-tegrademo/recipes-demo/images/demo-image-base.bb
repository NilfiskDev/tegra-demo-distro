DESCRIPTION = "Tegra demo base image"

require demo-image-common.inc

# Install networking dependencies
CORE_IMAGE_BASE_INSTALL += "backport-iwlwifi networkmanager modemmanager ethtool"

# Add iotedge and aziot-edged
# CORE_IMAGE_BASE_INSTALL += "iotedge aziot-edged"

# Add nvidia jetpack runtime components
CORE_IMAGE_BASE_INSTALL += "nvidia-docker cuda-libraries opencv cudnn tensorrt-core libnvvpi3"
