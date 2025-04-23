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

# Client for invoking nvidia-pva-allowd service

import socket
from nvidiaPvaAllowd import PVA_ALLOWD_SOCKET, PVA_ALLOWD_REQ_UPDATE, PVA_ALLOWD_REQ_DISABLE, PVA_ALLOWD_REQ_ENABLE, PVA_ALLOWD_RESP
import argparse


def parseArgs():
    """Parse script arguments"""
    top_parser = argparse.ArgumentParser(
        description="""Utility for updating, enabling and disabling PVA allowlist protection""")
    top_parser.add_argument('-a', '--allow_missing_service',
                            help='Do not report error if service connection fails, instead just exit - this is useful for running as an install step when we want to opportunistically trigger updates only when the service is available',
                            action='store_true')
    subparsers = top_parser.add_subparsers(required=True)
    subparsers.add_parser(
        'update', help='Compile /etc/pva/allow.d into system allowlist, notify kernel to refresh if PVA allow authentication is enabled.').set_defaults(cmd=PVA_ALLOWD_REQ_UPDATE)
    subparsers.add_parser(
        'enable', help='Enable PVA allow authentication system-wide.').set_defaults(cmd=PVA_ALLOWD_REQ_ENABLE)
    subparsers.add_parser(
        'disable', help='Disable PVA allow authentication system-wide.').set_defaults(cmd=PVA_ALLOWD_REQ_DISABLE)
    return top_parser.parse_args()


def main():
    args = parseArgs()
    # Unix domain socket
    sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    try:
        sock.connect(str(PVA_ALLOWD_SOCKET))
    except Exception as e:
        if args.allow_missing_service:
            return
        else:
            raise e
    sock.sendall(args.cmd)
    data = sock.recv(32)
    if data != PVA_ALLOWD_RESP:
        print("[ERROR] Unexpected response from nvidia-pva-allowd: {}".format(data))
    else:
        print("Operation completed successfully")


if __name__ == '__main__':
    main()