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

import argparse
import json
from pathlib import Path


def createLink(dirName, idString):
    """Create a link from the container directory to the host directory."""
    # Create the parent dir in the host if it doesn't exist
    dirPath = Path(dirName)
    dirPath.mkdir(parents=True, exist_ok=True)
    # Create dir in container namespace if if doesnt exist
    containerDir = Path.cwd() / dirPath.relative_to('/')
    containerDir.mkdir(parents=True, exist_ok=True)
    # Create symlink
    symlinkPath = dirPath / idString
    symlinkPath.symlink_to(containerDir, target_is_directory=True)


def removeLink(dirName, idString):
    """Remove any links from the container directory to the host directory."""
    dirPath = Path(dirName)
    symlinkPath = dirPath / idString
    # Allow missing symlink in case user has manually modified allowlist dir
    symlinkPath.unlink(missing_ok=True)


def parseArgs():
    """Parse script arguments"""
    top_parser = argparse.ArgumentParser(
        description="""Handle OCI runtime hooks for linking dir owned by the container back to the host, with one level of nesting.
                       For example, it will link the containers /etc/pva/allow.d to the hosts /etc/pva/allow.d/<container_id>.
                       Must be invoked as a hook in order to read container info from stdin.""")
    top_parser.add_argument('-d', '--directory',
                            required=True,
                            help='Directory to link.')

    subparsers = top_parser.add_subparsers(required=True)

    # Create the link
    create = subparsers.add_parser(
        'create', help='Create the link from hosts <dir>/<container_id> to containers <dir>.')
    create.set_defaults(func=createLink)

    # Remove the link
    remove = subparsers.add_parser(
        'remove', help='Remove any link on host at <dir>/<container_id>.')
    remove.set_defaults(func=removeLink)

    return top_parser.parse_args()


def main():
    args = parseArgs()
    # Per OCI spec, container info is passed in on stdin
    containerInfo = input()
    containerInfo = json.loads(containerInfo)
    args.func(args.directory, containerInfo['id'])


if __name__ == '__main__':
    main()