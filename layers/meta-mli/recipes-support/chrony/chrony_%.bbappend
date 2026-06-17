do_install:append() {
    sed -i 's/^makestep 1.0 3/makestep 60.0 -1/' ${D}${sysconfdir}/chrony.conf
}
