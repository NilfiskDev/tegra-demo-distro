#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright (c) 2024 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
# SPDX-License-Identifier: LicenseRef-NvidiaProprietary
#
# NVIDIA CORPORATION, its affiliates and licensors retain all intellectual
# property and proprietary rights in and to this material, related
# documentation and any modifications thereto. Any use, reproduction,
# disclosure or distribution of this material and related documentation
# without an express license agreement from NVIDIA CORPORATION or
# its affiliates is strictly prohibited.
#

# Service for running PVA allowlist updates via Unix domain socket

import socket
from pathlib import Path
from pvasdkAllowlist import readAllowlistHashes, generateAllowlist
import os

PVA_ALLOWD_SOCKET_DIR = Path("/run/nvidia-pva-allowd")
PVA_ALLOWD_SOCKET = PVA_ALLOWD_SOCKET_DIR / "socket.sock"
PVA_ALLOWD_REQ_UPDATE = b"do_update"
PVA_ALLOWD_REQ_DISABLE = b"do_disable"
PVA_ALLOWD_REQ_ENABLE = b"do_enable"
PVA_ALLOWD_RESP = b"done"
PVA_ALLOWLIST_DIR = "/etc/pva/allow.d"
PVA_ALLOWLIST_OUTPUT = "/lib/firmware/pva_auth_allowlist"
PVA_KERNEL_NODE_BASE = "/sys/kernel/debug/pva"
PVA_KERNEL_SUFFIX = "/vpu_app_authentication"


def doUpdate():
    hashes = {}
    rootDir = Path(PVA_ALLOWLIST_DIR)
    for root, dis, files in os.walk(PVA_ALLOWLIST_DIR, followlinks=True):
        for file in files:
            filePath = Path(root) / file
            hashes.update(readAllowlistHashes(filePath))
    allowlistData = generateAllowlist(hashes)
    with open(PVA_ALLOWLIST_OUTPUT, 'wb') as outFile:
        outFile.write(allowlistData)
    # Notify kernel to reload allowlist
    for i in range(0, 2):
        nodePath = PVA_KERNEL_NODE_BASE + str(i) + PVA_KERNEL_SUFFIX
        if Path(nodePath).exists():
            # Trigger the kernel reload if allowlist is already enabled
            with open(nodePath, 'r') as kernelNode:
                currentState = kernelNode.read()
            with open(nodePath, 'w') as kernelNode:
                kernelNode.write(currentState)


def doEnable(val):
    for i in range(0, 2):
        nodePath = PVA_KERNEL_NODE_BASE + str(i) + PVA_KERNEL_SUFFIX
        if Path(nodePath).exists():
            with open(nodePath, 'w') as kernelNode:
                kernelNode.write(val)


def handleConnection(connection):
    try:
        while True:
            data = connection.recv(32)
            if data:
                if data == PVA_ALLOWD_REQ_UPDATE:
                    print("Updating allowlist...")
                    doUpdate()
                elif data == PVA_ALLOWD_REQ_DISABLE:
                    print("Disabling allowlist protection...")
                    doEnable("0")
                elif data == PVA_ALLOWD_REQ_ENABLE:
                    print("Enabling allowlist protection...")
                    doEnable("1")
                else:
                    print(
                        "[ERROR] Unexpected request: {}".format(data))
                    break
                connection.sendall(PVA_ALLOWD_RESP)
            else:
                # Client has disconnected...
                break
    except Exception as e:
        # Ignore failures and just log...
        print("[ERROR] error handling connection: {}".format(e))
    finally:
        # Close connection on failure or success - put in finally block so its executed even on uncaught exceptions (e.g. SIGINT)
        connection.close()


def main():
    # Make dir if not already present...
    PVA_ALLOWD_SOCKET_DIR.mkdir(parents=True, exist_ok=True)
    # Remove if already exists...
    PVA_ALLOWD_SOCKET.unlink(missing_ok=True)
    # Unix domain socket
    sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    sock.bind(str(PVA_ALLOWD_SOCKET))
    sock.listen(1)
    while True:
        connection, client_address = sock.accept()
        handleConnection(connection)


if __name__ == '__main__':
    main()