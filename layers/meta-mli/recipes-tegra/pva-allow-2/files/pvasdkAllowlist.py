#!/usr/bin/env python3
#
# Copyright (c) 2022, NVIDIA CORPORATION.  All rights reserved.
#
# NVIDIA Corporation and its licensors retain all intellectual property
# and proprietary rights in and to this software, related documentation
# and any modifications thereto.  Any use, reproduction, disclosure or
# distribution of this software and related documentation without an express
# license agreement from NVIDIA Corporation is strictly prohibited.
#

# Script to generate a binary allowlist file from a collection of VPU elfs
# fmt: off
import sys
sys.dont_write_bytecode = True

import argparse
import zlib
import hashlib
import pvasdkBinTools
import struct
# fmt: on


def readElfs(vpuElfPaths):
    """ Read elfs from filesystem """
    retData = []
    for path in vpuElfPaths:
        with open(path, 'rb') as fh:
            retData += [fh.read()]
    return retData


def generateHashes(blobs):
    """ Generate a map from crc32s to sha256s """
    hashMap = {}
    for blob in blobs:
        sha256hash = hashlib.sha256(blob).digest()
        crc32hash = zlib.crc32(blob) & 0xFFFFFFFF
        # Create SHA256 set for this CRC32 if it doesn't already exist
        currentSet = hashMap.get(crc32hash, set())
        currentSet.add(sha256hash)
        hashMap[crc32hash] = currentSet
    return hashMap


def readAllowlistHashes(filename):
    """Read allowlist hashes from binary file"""
    hashMap = {}
    with open(filename, 'rb') as fh:
        data = fh.read()
    # Minimum valid allowlist is 2x 32bit integers, both of which are zero (empty allowlist)
    if len(data) < 8:
        print("WARNING: Ignoring invalid allowlist file {}".format(filename))
        return hashMap
    # First 4 bytes is sha256 count
    sha256Count = int.from_bytes(data[0:4], 'little')
    # CRC section starts after SHA256 section
    crcOffset = sha256Count * 32 + 4
    if len(data) < crcOffset + 4:
        print("WARNING: Ignoring invalid allowlist file {}".format(filename))
        return hashMap
    crcCount = int.from_bytes(data[crcOffset:crcOffset+4], 'little')
    if len(data) != crcOffset + 4 + crcCount * 12:
        print("WARNING: Ignoring invalid allowlist file {}".format(filename))
        return hashMap
    for i in range(crcOffset+4, len(data), 12):
        crcStruct = struct.unpack("<III", data[i:i+12])
        crcShaCount = crcStruct[0]
        crcShaStartIndex = crcStruct[1]
        crc32hash = crcStruct[2]
        currentSet = hashMap.get(crc32hash, set())
        for j in range(crcShaStartIndex, crcShaStartIndex+crcShaCount):
            # Skip the sha256Count (4 bytes) and read the SHAs indexed for this CRC
            startHash = 4+j*32
            currentSet.add(data[startHash:startHash+32])
        hashMap[crc32hash] = currentSet
    return hashMap


def generateAllowlist(hashes):
    """Generate allowlist bytearray to write to file"""
    outData = bytearray()
    crc32Keys = sorted(hashes.keys())
    numSha = sum([len(hashes[i]) for i in crc32Keys])
    # Write number of SHA256s
    outData += numSha.to_bytes(4, 'little')
    # Write SHA256s in order
    for key in crc32Keys:
        for sha in hashes[key]:
            outData += sha
    # Write number of CRC32s
    outData += len(crc32Keys).to_bytes(4, 'little')
    # Write CRC32s - array of {count, index, val}
    index = 0
    for key in crc32Keys:
        count = len(hashes[key])
        outData += count.to_bytes(4, 'little')
        outData += index.to_bytes(4, 'little')
        outData += key.to_bytes(4, 'little')
        index += count
    return outData


def parseArgs():
    """Parse script arguments"""
    parser = argparse.ArgumentParser(
        description='Generate a PVA allowlist, needed to allow VPU elfs to be loaded on some configurations')
    parser.add_argument('-b', '--cupva_binaries',
                        nargs='+',
                        help='Path to CUPVA binaries which contain VPU elfs embedded in .rodata. Those elfs will be included in the allowlist.')
    parser.add_argument('-v', '--vpu_elfs',
                        nargs='+',
                        help='Path to VPU elfs to hash and include in the allowlist')
    parser.add_argument('-i', '--input',
                        default=None,
                        nargs='+',
                        help='Input allowlist files. All hashes in any input allowlists will also be included in the output.')
    parser.add_argument('-o', '--output',
                        default='pva_vpu_allowlist',
                        help='Path to output file. Defaults to pva_vpu_allowlist')
    return parser.parse_args()


def main():
    args = parseArgs()
    elfs = []
    if args.vpu_elfs != None:
        elfs += readElfs(args.vpu_elfs)
    if args.cupva_binaries != None:
        elfs += [
            elf for path in args.cupva_binaries for elf in pvasdkBinTools.readElfsFromBin(path)]
    hashes = generateHashes(elfs)
    if args.input != None:
        for file in args.input:
            hashes.update(readAllowlistHashes(file))
    allowlistData = generateAllowlist(hashes)
    with open(args.output, 'wb') as outFile:
        outFile.write(allowlistData)


if __name__ == '__main__':
    main()