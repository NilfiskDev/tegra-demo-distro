DESCRIPTION = "Python package requirements for MLI"

LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = "\
    python3 \
    python3-pip \
    python3-evdev \
    python3-dbus-next \
    python3-uvicorn \
    python3-fastapi \
    python3-toml \
    python3-loguru \
    python3-click \
    python3-h11 \
    python3-httptools \
    python3-typing-extensions \
    python3-uvloop \
    python3-websockets \
    python3-wsproto \
    python3-pydantic \
    python3-starlette \
    python3-requests \
    python3-docker \
    python3-socketio \
    python3-pyserial \
"
