SUMMARY = "Python HTTP for Humans."
HOMEPAGE = "https://requests.readthedocs.io"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=34400b68072d710fecd0a2940a0d1658"

SRC_URI[sha256sum] = "f2e34a75f4749019bb0e3effb66683630e4ffeaf75819fb51bebef1bf5aef059"

inherit pypi python_setuptools_build_meta

RDEPENDS:${PN} += " \
    python3-certifi \
    python3-email \
    python3-json \
    python3-netserver \
    python3-pysocks \
    python3-urllib3 \
    python3-chardet \
    python3-idna \
    python3-compression \
"

CVE_PRODUCT = "requests"

BBCLASSEXTEND = "native nativesdk"
