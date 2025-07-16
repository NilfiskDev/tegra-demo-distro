# File: recipes-images/data-partition/data-partition.bb
DESCRIPTION = "Data partition image"
LICENSE = "MIT"

# Size in KB (e.g., 64MB)
IMAGE_INSTALL = ""
IMAGE_FSTYPES = "ext4"
IMAGE_ROOTFS_SIZE = "65536" 
IMAGE_NAME_SUFFIX = ""

inherit image

fakeroot python do_rootfs:append() {
    import subprocess

    # Delete the auto generated files from image.bbclass (make it truly empty)
    rootfs_dir = d.getVar('IMAGE_ROOTFS')
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/bin'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/boot'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/dev'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/etc'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/home'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/lib'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/media'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/mnt'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/proc'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/root'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/run'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/sbin'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/sys'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/tmp'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/usr'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/var'])
    subprocess.run(['rm', '-rf', f'{rootfs_dir}/lost+found'])
}
