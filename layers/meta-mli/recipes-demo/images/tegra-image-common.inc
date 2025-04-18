IMAGE_FEATURES += "ssh-server-openssh"

LICENSE = "MIT"

inherit core-image

CORE_IMAGE_BASE_INSTALL += "packagegroup-image-base"
CORE_IMAGE_BASE_INSTALL += "${@'packagegroup-image-systemd' if d.getVar('VIRTUAL-RUNTIME_init_manager') == 'systemd' else ''}"
TOOLCHAIN_HOST_TASK += "nativesdk-packagegroup-cuda-sdk-host"

inherit nopackages
