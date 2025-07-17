# Make sure timesyncd has a uid of 984
USERADD_PARAM:${PN}:remove = "--system -d / -M --shell /sbin/nologin systemd-timesync"
USERADD_PARAM:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'timesyncd', '--system -d / -M --shell /sbin/nologin --uid 984 systemd-timesync;', '', d)}"
