SUMMARY = "FastAPI framework, high performance, easy to learn, fast to code, ready for production"
HOMEPAGE = "https://fastapi.tiangolo.com/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=95792ff3fe8e11aa49ceb247e66e4810"

RDEPENDS_${PN} += "\
    python3-pydantic \
    python3-starlette \
"

DEPENDS += "\
    python3-pdm-backend-native \
"

PYPI_PACKAGE = "fastapi"

inherit pypi python_setuptools_build_meta

SRC_URI[sha256sum] = "1e2c2a2646905f9e83d32f04a3f86aff4a286669c6c950ca95b5fd68c2602681"
