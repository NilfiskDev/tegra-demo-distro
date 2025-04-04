do_install:append() {
    echo "nvidia ALL=(ALL:ALL) ALL" > ${D}${sysconfdir}/sudoers.d/nvidia
}

FILES_${PN} += "${sysconfdir}/sudoers.d/nvidia"
