PACKAGE_ARCH:tegra = "${TEGRA_PKGARCH}"

# Cannot install both nvidia-drm-loadconf-modeset and xserver-xorg-video-nvidia but that happens with x11 package group...
RRECOMMENDS:${PN}:tegra = "kernel-module-nvidia-drm"
