DESCRIPTION = "Tegra demo base image"

require demo-image-common.inc

# Install networking dependencies
CORE_IMAGE_BASE_INSTALL += "backport-iwlwifi networkmanager modemmanager ethtool"

# Add nvidia jetpack runtime components -  cuda-libraries opencv cudnn
CORE_IMAGE_BASE_INSTALL += "nvidia-docker"
