SUMMARY = "Programmable Vision Accelerator allowlist tool"
LICENSE = "CLOSED"

inherit systemd

SYSTEMD_SERVICE:${PN} = "nvidia-pva-allowd.service"
SYSTEMD_AUTO_ENABLE = "enable"

S = "${WORKDIR}/sources"
UNPACKDIR = "${S}"

SRC_URI += "\
    file://nvidia-pva-allowd.service \
    file://nvidia-pva.yaml \
    file://nvidiaPvaAllow.py \
    file://nvidiaPvaAllowd.py \
    file://nvidiaPvaHook.py \
    file://pvasdkAllowlist.py \
    file://pvasdkBinTools.py \
"

FILES:${PN} += "\
    ${systemd_unitdir}/system/nvidia-pva-allowd.service \
    /opt/nvidia/pva-allow-2/bin/nvidiaPvaAllow.py \
    /opt/nvidia/pva-allow-2/bin/nvidiaPvaAllowd.py \
    /opt/nvidia/pva-allow-2/bin/nvidiaPvaHook.py \
    /opt/nvidia/pva-allow-2/bin/pvasdkAllowlist.py \
    /opt/nvidia/pva-allow-2/bin/pvasdkBinTools.py \
    ${sysconfdir}/cdi/nvidia-pva.yaml \
"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/nvidia-pva-allowd.service ${D}${systemd_unitdir}/system

    install -d ${D}/opt/nvidia/pva-allow-2/bin/
    install -m 0755 ${UNPACKDIR}/nvidiaPvaAllow.py ${D}/opt/nvidia/pva-allow-2/bin
    install -m 0755 ${UNPACKDIR}/nvidiaPvaAllowd.py ${D}/opt/nvidia/pva-allow-2/bin
    install -m 0755 ${UNPACKDIR}/nvidiaPvaHook.py ${D}/opt/nvidia/pva-allow-2/bin
    install -m 0755 ${UNPACKDIR}/pvasdkAllowlist.py ${D}/opt/nvidia/pva-allow-2/bin
    install -m 0755 ${UNPACKDIR}/pvasdkBinTools.py ${D}/opt/nvidia/pva-allow-2/bin

    install -d ${D}${sysconfdir}/cdi/
    install -m 0644 ${UNPACKDIR}/nvidia-pva.yaml ${D}${sysconfdir}/cdi
}
