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

# Script to read VPU elfs which have been embedded in a binary
# fmt: off
import sys
sys.dont_write_bytecode = True

import subprocess
import re
import tempfile
import uuid
import os
import argparse
# fmt: on

EXPECTED_SHENT_SIZE = 40
EXPECTED_PHENT_SIZE = 32
EXPECTED_PH_OFFSET = 52


def dumpElf(elf, outputDir):
    """ Dump an elf to file """
    os.makedirs(outputDir, exist_ok=True)
    outputName = uuid.uuid4().hex + ".elf"
    outPath = os.path.join(outputDir, outputName)
    with open(outPath, 'wb') as fh:
        fh.write(elf)
    # See if we can give the elf a better name, if the elf has symbols
    syms = subprocess.run(
        ["readelf", outPath, "-sW"], stdout=subprocess.PIPE, check=True)
    pattern = "([\S]*.elf)\n".encode('utf-8')
    result = re.search(pattern, syms.stdout)
    if (result != None):
        os.rename(outPath, os.path.join(
            outputDir, result.group(1).decode('utf-8')))


def validateElf(elf):
    """ Validate that an ELF is indeed a CUPVA elf by checking for export table and that readelf doesn't crash """
    with tempfile.NamedTemporaryFile(suffix='.rodata') as t:
        with open(t.name, 'wb') as fh:
            fh.write(elf)
        progHeaders = subprocess.run(
            ["readelf", t.name, "-lW"], stdout=subprocess.PIPE, check=True)
    pattern = ".data.DMb.4.EXPORTS".encode('utf-8')
    result = re.search(pattern, progHeaders.stdout)
    if (result == None):
        return False
    return True


def trimElfCandidates(candidates):
    """ Get size of elf from header fields and trim to this size """
    shnums = [int.from_bytes(cand[48:50], byteorder='little')
              for cand in candidates]
    shoffs = [int.from_bytes(cand[32:36], byteorder='little')
              for cand in candidates]
    sizes = [shnums[i] * EXPECTED_SHENT_SIZE +
             shoff for i, shoff in enumerate(shoffs)]
    return [cand[0:sizes[i]] for i, cand in enumerate(candidates)]


def extractElfCandidates(binPath):
    """ Read file and extract likely elf blobs """
    with tempfile.NamedTemporaryFile(suffix='.rodata') as t:
        subprocess.run(
            ["objcopy", "--only-section=.rodata", "-O", "binary", binPath, "{}".format(t.name), "-Ielf32-little"], check=True)
        with open("{}".format(t.name), 'rb') as fh:
            data = fh.read()
    # Start of VPU elf header
    pattern = b'\x7F\x45\x4C\x46\x01\x01\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x02\x00\x00\x00\x01\x00\x00\x00'
    result = re.split(pattern, data)
    if (len(result) <= 1):
        return []
    # Strip off prefix which has no match
    result = result[1:]
    # Add back regex pattern
    result = [pattern + res for res in result]
    # Strip out any ELFs which don't have correct section headers
    # Make sure that offset in elf header is correct as one more step in validation
    result = [res for res in result if int.from_bytes(
        res[28:32], byteorder='little') == EXPECTED_PH_OFFSET]
    # Make sure that SH size is correct as one more step in validation
    result = [res for res in result if int.from_bytes(
        res[46:48], byteorder='little') == EXPECTED_SHENT_SIZE]
    # Make sure that PH size is correct  as one more step in validation
    result = [res for res in result if int.from_bytes(
        res[42:44], byteorder='little') == EXPECTED_PHENT_SIZE]
    if (len(result) == 0):
        return []
    return result


def readElfsFromBin(binPath):
    """ Extract an array of elf data, each entry represents an elf embedded in a CUPVA binary """
    elfCandidates = extractElfCandidates(binPath)
    trimmedElfs = trimElfCandidates(elfCandidates)
    validatedElfs = [elf for elf in trimmedElfs if validateElf(elf) == True]
    if (len(validatedElfs) > 0):
        print("Found {} embedded VPU elfs in {}...".format(
            len(validatedElfs), binPath))
    else:
        print("WARNING: No embedded VPU elfs found in {}".format(binPath))
    return validatedElfs


def parseArgs():
    """Parse script arguments"""
    parser = argparse.ArgumentParser(
        description='Extract VPU elfs from a CUPVA binary which has embedded VPU elfs')
    parser.add_argument('-b', '--cupva_binaries',
                        nargs='+',
                        help='Path to CUPVA binaries with embedded elfs to extract')
    parser.add_argument('-o', '--output_dir',
                        default='vpu_elfs',
                        help='Output directory for VPU elfs. Defaults to ./vpu_elfs. Will be created if it does not exist')
    return parser.parse_args()


def main():
    args = parseArgs()
    elfs = [
        elf for path in args.cupva_binaries for elf in readElfsFromBin(path)]
    [dumpElf(elf, args.output_dir) for elf in elfs]


if __name__ == '__main__':
    main()