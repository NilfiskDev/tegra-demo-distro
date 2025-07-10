FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI += "\
    file://flash_l4t_t234_nvme_custom_rootfs_ab.xml \
    file://flash_l4t_t234_nvme_custom_rootfs_ab_enc.xml \
"

do_install() {
    install -d ${D}${datadir}/l4t-storage-layout
    install -m 0644 ${PARTITION_FILE} ${D}${datadir}/l4t-storage-layout/${PARTITION_LAYOUT_TEMPLATE}

    if [ "${PARTITION_LAYOUT_EXTERNAL}" = "flash_l4t_t234_nvme_custom_rootfs_ab.xml" ]; then
        # TODO: This is a very big hack.... should probably change
        temp_path="${@'${THISDIR}/${PN}'.replace('meta-tegra','meta-plink')}"
        [ -z "${PARTITION_LAYOUT_EXTERNAL}" ] || install -m 0644 ${temp_path}/${PARTITION_LAYOUT_EXTERNAL} ${D}${datadir}/l4t-storage-layout/${PARTITION_LAYOUT_EXTERNAL}
    else
        [ -z "${PARTITION_LAYOUT_EXTERNAL}" ] || install -m 0644 ${PARTITION_FILE_EXTERNAL} ${D}${datadir}/l4t-storage-layout/${PARTITION_LAYOUT_EXTERNAL}
    fi
    
}
