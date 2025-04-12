SUMMARY = "Fast implementation of asyncio event loop on top of libuv"
HOMEPAGE = "http://github.com/MagicStack/uvloop"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE-MIT;md5=489c8bc34154e4b59f5c58e664f7d70f"

PYPI_PACKAGE = "uvloop"

SRC_URI[sha256sum] = "3bf12b0fda68447806a7ad847bfa591613177275d35b6724b1ee573faa3704e3"

inherit pypi python_setuptools_build_meta cython

S = "${WORKDIR}/uvloop-${PV}"

# RDEPENDS:${PN} += " \
#     python3-asyncio \
#     python3-core \
#     python3-ctypes \
#     python3-io \
#     python3-logging \
#     python3-math \
#     python3-multiprocessing \
#     python3-netclient \
#     python3-numbers \
#     python3-pickle \
#     python3-pprint \
#     python3-psutil \
#     python3-threading \
#     python3-unittest \
# "
# 
# WARNING: We were unable to map the following python package/module
# dependencies to the bitbake packages which include them:
#    OpenSSL
#    aiohttp
#    aiohttp.web
#    flake8
#    mypy

do_configure:prepend() {
cat > ${S}/setup.py <<-EOF
from setuptools import setup

setup(
       name="${PYPI_PACKAGE}",
       version="${PV}",
       license="${LICENSE}",
)
EOF
}

BBCLASSEXTEND = "native nativesdk"