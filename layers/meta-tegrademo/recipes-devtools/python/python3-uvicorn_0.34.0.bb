SUMMARY = "The lightning-fast ASGI server"
HOMEPAGE = "https://www.uvicorn.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=5c778842f66a649636561c423c0eec2e"
RDEPENDS_${PN} += "\
    python3-click \
    python3-h11 \
    python3-httptools \
    python3-typing-extensions \
    python3-uvloop \
    python3-websockets \
    python3-wsproto \
"

PYPI_PACKAGE = "uvicorn"

inherit pypi python_pep517 python_hatchling

SRC_URI[md5sum] = "9944ee2becd37cb1c1e804a96c34a9e0"
SRC_URI[sha256sum] = "404051050cd7e905de2c9a7e61790943440b3416f49cb409f965d9dcd0fa73e9"
