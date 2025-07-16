SUMMARY = "Tegra swupdate update image"
DESCRIPTION = "A swupdate image for demonstrating tegra updates"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "\
    file://sw-description \
"

inherit swupdate image_types_tegra tegra_swupdate

DEPLOY_KERNEL_IMAGE ?= "${@os.path.basename(tegra_kernel_image(d))}"

ROOTFS_DEVICE_PATH ?= "/dev/disk/by-partlabel"

# By default, use demo-image-base as the base image.
# Redefine in local.conf if you'd like to use a different base image.
SWUPDATE_CORE_IMAGE_NAME ?= "prod-image-mli"

ROOTFS_FILENAME ?= "${SWUPDATE_CORE_IMAGE_NAME}-${MACHINE}.rootfs.tar.gz"

KERNEL_A_PARTNAME = "A_kernel"
KERNEL_A_DTB_PARTNAME = "A_kernel-dtb"
KERNEL_B_PARTNAME = "B_kernel"
KERNEL_B_DTB_PARTNAME = "B_kernel-dtb"

# images to build before building swupdate image
IMAGE_DEPENDS = "${SWUPDATE_CORE_IMAGE_NAME} tegra-uefi-capsules tegra-swupdate-script tegra-espimage"

ESP_ARCHIVE ?= "${TEGRA_ESP_IMAGE}-${MACHINE}.tar.gz"

# images and files that will be included in the .swu image
DTBFILE_PATH = "${@'${EXTERNAL_KERNEL_DEVICETREE}/${DTBFILE}' if len(d.getVar('EXTERNAL_KERNEL_DEVICETREE')) else '${DTBFILE}'}"
SWUPDATE_IMAGES = "${ROOTFS_FILENAME} tegra-bl.cap ${DEPLOY_KERNEL_IMAGE} ${DTBFILE_PATH} tegra-swupdate-script.lua ${ESP_ARCHIVE}"

do_swuimage[depends] += "${DTB_EXTRA_DEPS}"

# Add a link using the core image name.swu to the resulting swu image
do_swuimage:append() {
    os.symlink(d.getVar("IMAGE_NAME") + ".swu", d.getVar("SWUPDATE_CORE_IMAGE_NAME") + "-" + d.getVar("MACHINE") + ".swu")
}

# A custom script that ignores double installed files - TODO: Remove doubled up recipes
python extend_recipe_sysroot() {
    import copy
    import subprocess
    import errno
    import collections
    import glob

    taskdepdata = d.getVar("BB_TASKDEPDATA", False)
    mytaskname = d.getVar("BB_RUNTASK")
    if mytaskname.endswith("_setscene"):
        mytaskname = mytaskname.replace("_setscene", "")
    workdir = d.getVar("WORKDIR")
    #bb.warn(str(taskdepdata))
    pn = d.getVar("PN")
    stagingdir = d.getVar("STAGING_DIR")
    sharedmanifests = d.getVar("COMPONENTS_DIR") + "/manifests"
    # only needed by multilib cross-canadian since it redefines RECIPE_SYSROOT
    manifestprefix = d.getVar("RECIPE_SYSROOT_MANIFEST_SUBDIR")
    if manifestprefix:
        sharedmanifests = sharedmanifests + "/" + manifestprefix
    recipesysroot = d.getVar("RECIPE_SYSROOT")
    recipesysrootnative = d.getVar("RECIPE_SYSROOT_NATIVE")

    # Detect bitbake -b usage
    nodeps = d.getVar("BB_LIMITEDDEPS") or False
    if nodeps:
        lock = bb.utils.lockfile(recipesysroot + "/sysroot.lock")
        staging_populate_sysroot_dir(recipesysroot, recipesysrootnative, True, d)
        staging_populate_sysroot_dir(recipesysroot, recipesysrootnative, False, d)
        bb.utils.unlockfile(lock)
        return

    start = None
    configuredeps = []
    owntaskdeps = []
    for dep in taskdepdata:
        data = taskdepdata[dep]
        if data[1] == mytaskname and data[0] == pn:
            start = dep
        elif data[0] == pn:
            owntaskdeps.append(data[1])
    if start is None:
        bb.fatal("Couldn't find ourself in BB_TASKDEPDATA?")

    # We need to figure out which sysroot files we need to expose to this task.
    # This needs to match what would get restored from sstate, which is controlled
    # ultimately by calls from bitbake to setscene_depvalid().
    # That function expects a setscene dependency tree. We build a dependency tree
    # condensed to inter-sstate task dependencies, similar to that used by setscene
    # tasks. We can then call into setscene_depvalid() and decide
    # which dependencies we can "see" and should expose in the recipe specific sysroot.
    setscenedeps = copy.deepcopy(taskdepdata)

    start = set([start])

    sstatetasks = d.getVar("SSTATETASKS").split()
    # Add recipe specific tasks referenced by setscene_depvalid()
    sstatetasks.append("do_stash_locale")
    sstatetasks.append("do_deploy")

    def print_dep_tree(deptree):
        data = ""
        for dep in deptree:
            deps = "    " + "\n    ".join(deptree[dep][3]) + "\n"
            data = data + "%s:\n  %s\n  %s\n%s  %s\n  %s\n" % (deptree[dep][0], deptree[dep][1], deptree[dep][2], deps, deptree[dep][4], deptree[dep][5])
        return data

    #bb.note("Full dep tree is:\n%s" % print_dep_tree(taskdepdata))

    #bb.note(" start2 is %s" % str(start))

    # If start is an sstate task (like do_package) we need to add in its direct dependencies
    # else the code below won't recurse into them.
    for dep in set(start):
        for dep2 in setscenedeps[dep][3]:
            start.add(dep2)
        start.remove(dep)

    #bb.note(" start3 is %s" % str(start))

    # Create collapsed do_populate_sysroot -> do_populate_sysroot tree
    for dep in taskdepdata:
        data = setscenedeps[dep]
        if data[1] not in sstatetasks:
            for dep2 in setscenedeps:
                data2 = setscenedeps[dep2]
                if dep in data2[3]:
                    data2[3].update(setscenedeps[dep][3])
                    data2[3].remove(dep)
            if dep in start:
                start.update(setscenedeps[dep][3])
                start.remove(dep)
            del setscenedeps[dep]

    # Remove circular references
    for dep in setscenedeps:
        if dep in setscenedeps[dep][3]:
            setscenedeps[dep][3].remove(dep)

    #bb.note("Computed dep tree is:\n%s" % print_dep_tree(setscenedeps))
    #bb.note(" start is %s" % str(start))

    # Direct dependencies should be present and can be depended upon
    for dep in sorted(set(start)):
        if setscenedeps[dep][1] == "do_populate_sysroot":
            if dep not in configuredeps:
                configuredeps.append(dep)
    bb.note("Direct dependencies are %s" % str(configuredeps))
    #bb.note(" or %s" % str(start))

    msgbuf = []
    # Call into setscene_depvalid for each sub-dependency and only copy sysroot files
    # for ones that would be restored from sstate.
    done = list(start)
    next = list(start)
    while next:
        new = []
        for dep in next:
            data = setscenedeps[dep]
            for datadep in data[3]:
                if datadep in done:
                    continue
                taskdeps = {}
                taskdeps[dep] = setscenedeps[dep][:2]
                taskdeps[datadep] = setscenedeps[datadep][:2]
                retval = setscene_depvalid(datadep, taskdeps, [], d, msgbuf)
                if retval:
                    msgbuf.append("Skipping setscene dependency %s for installation into the sysroot" % datadep)
                    continue
                done.append(datadep)
                new.append(datadep)
                if datadep not in configuredeps and setscenedeps[datadep][1] == "do_populate_sysroot":
                    configuredeps.append(datadep)
                    msgbuf.append("Adding dependency on %s" % setscenedeps[datadep][0])
                else:
                    msgbuf.append("Following dependency on %s" % setscenedeps[datadep][0])
        next = new

    # This logging is too verbose for day to day use sadly
    #bb.debug(2, "\n".join(msgbuf))

    depdir = recipesysrootnative + "/installeddeps"
    bb.utils.mkdirhier(depdir)
    bb.utils.mkdirhier(sharedmanifests)

    lock = bb.utils.lockfile(recipesysroot + "/sysroot.lock")

    fixme = {}
    seendirs = set()
    postinsts = []
    multilibs = {}
    manifests = {}
    # All files that we're going to be installing, to find conflicts.
    fileset = {}

    invalidate_tasks = set()
    for f in os.listdir(depdir):
        removed = []
        if not f.endswith(".complete"):
            continue
        f = depdir + "/" + f
        if os.path.islink(f) and not os.path.exists(f):
            bb.note("%s no longer exists, removing from sysroot" % f)
            lnk = os.readlink(f.replace(".complete", ""))
            sstate_clean_manifest(depdir + "/" + lnk, d, canrace=True, prefix=workdir)
            os.unlink(f)
            os.unlink(f.replace(".complete", ""))
            removed.append(os.path.basename(f.replace(".complete", "")))

        # If we've removed files from the sysroot above, the task that installed them may still
        # have a stamp file present for the task. This is probably invalid right now but may become
        # valid again if the user were to change configuration back for example. Since we've removed
        # the files a task might need, remove the stamp file too to force it to rerun.
        # YOCTO #14790
        if removed:
            for i in glob.glob(depdir + "/index.*"):
                if i.endswith("." + mytaskname):
                    continue
                with open(i, "r") as f:
                    for l in f:
                        if l.startswith("TaskDeps:"):
                            continue
                        l = l.strip()
                        if l in removed:
                            invalidate_tasks.add(i.rsplit(".", 1)[1])
                            break
    for t in invalidate_tasks:
        bb.note("Invalidating stamps for task %s" % t)
        bb.build.clean_stamp(t, d)

    installed = []
    for dep in configuredeps:
        c = setscenedeps[dep][0]
        if mytaskname in ["do_sdk_depends", "do_populate_sdk_ext"] and c.endswith("-initial"):
            bb.note("Skipping initial setscene dependency %s for installation into the sysroot" % c)
            continue
        installed.append(c)

    # We want to remove anything which this task previously installed but is no longer a dependency
    taskindex = depdir + "/" + "index." + mytaskname
    if os.path.exists(taskindex):
        potential = []
        with open(taskindex, "r") as f:
            for l in f:
                l = l.strip()
                if l not in installed:
                    fl = depdir + "/" + l
                    if not os.path.exists(fl):
                        # Was likely already uninstalled
                        continue
                    potential.append(l)
        # We need to ensure no other task needs this dependency. We hold the sysroot
        # lock so we ca search the indexes to check
        if potential:
            for i in glob.glob(depdir + "/index.*"):
                if i.endswith("." + mytaskname):
                    continue
                with open(i, "r") as f:
                    for l in f:
                        if l.startswith("TaskDeps:"):
                            prevtasks = l.split()[1:]
                            if mytaskname in prevtasks:
                                # We're a dependency of this task so we can clear items out the sysroot
                                break
                        l = l.strip()
                        if l in potential:
                            potential.remove(l)
        for l in potential:
            fl = depdir + "/" + l
            bb.note("Task %s no longer depends on %s, removing from sysroot" % (mytaskname, l))
            lnk = os.readlink(fl)
            sstate_clean_manifest(depdir + "/" + lnk, d, canrace=True, prefix=workdir)
            os.unlink(fl)
            os.unlink(fl + ".complete")

    msg_exists = []
    msg_adding = []

    # Handle all removals first since files may move between recipes
    for dep in configuredeps:
        c = setscenedeps[dep][0]
        if c not in installed:
            continue
        taskhash = setscenedeps[dep][5]
        taskmanifest = depdir + "/" + c + "." + taskhash

        if os.path.exists(depdir + "/" + c):
            lnk = os.readlink(depdir + "/" + c)
            if lnk == c + "." + taskhash and os.path.exists(depdir + "/" + c + ".complete"):
                continue
            else:
                bb.note("%s exists in sysroot, but is stale (%s vs. %s), removing." % (c, lnk, c + "." + taskhash))
                sstate_clean_manifest(depdir + "/" + lnk, d, canrace=True, prefix=workdir)
                os.unlink(depdir + "/" + c)
                if os.path.lexists(depdir + "/" + c + ".complete"):
                    os.unlink(depdir + "/" + c + ".complete")
        elif os.path.lexists(depdir + "/" + c):
            os.unlink(depdir + "/" + c)

    binfiles = {}
    # Now handle installs
    for dep in sorted(configuredeps):
        c = setscenedeps[dep][0]
        if c not in installed:
            continue
        taskhash = setscenedeps[dep][5]
        taskmanifest = depdir + "/" + c + "." + taskhash

        if os.path.exists(depdir + "/" + c):
            lnk = os.readlink(depdir + "/" + c)
            if lnk == c + "." + taskhash and os.path.exists(depdir + "/" + c + ".complete"):
                msg_exists.append(c)
                continue

        msg_adding.append(c)

        os.symlink(c + "." + taskhash, depdir + "/" + c)

        manifest, d2 = oe.sstatesig.find_sstate_manifest(c, setscenedeps[dep][2], "populate_sysroot", d, multilibs)
        if d2 is not d:
            # If we don't do this, the recipe sysroot will be placed in the wrong WORKDIR for multilibs
            # We need a consistent WORKDIR for the image
            d2.setVar("WORKDIR", d.getVar("WORKDIR"))
        destsysroot = d2.getVar("RECIPE_SYSROOT")
        # We put allarch recipes into the default sysroot
        if manifest and "allarch" in manifest:
            destsysroot = d.getVar("RECIPE_SYSROOT")

        native = False
        if c.endswith("-native") or "-cross-" in c or "-crosssdk" in c:
            native = True

        if manifest:
            newmanifest = collections.OrderedDict()
            targetdir = destsysroot
            if native:
                targetdir = recipesysrootnative
            if targetdir not in fixme:
                fixme[targetdir] = []
            fm = fixme[targetdir]

            with open(manifest, "r") as f:
                manifests[dep] = manifest
                for l in f:
                    l = l.strip()
                    if l.endswith("/fixmepath"):
                        fm.append(l)
                        continue
                    if l.endswith("/fixmepath.cmd"):
                        continue
                    dest = l.replace(stagingdir, "")
                    dest = "/" + "/".join(dest.split("/")[3:])
                    newmanifest[l] = targetdir + dest

                    # Check if files have already been installed by another
                    # recipe and abort if they have, explaining what recipes are
                    # conflicting.
                    hashname = targetdir + dest
                    if not hashname.endswith("/"):
                        if hashname in fileset:
                            bb.warn("The file %s is installed by both %s and %s, aborting" % (dest, c, fileset[hashname]))
                        else:
                            fileset[hashname] = c

            # Having multiple identical manifests in each sysroot eats diskspace so
            # create a shared pool of them and hardlink if we can.
            # We create the manifest in advance so that if something fails during installation,
            # or the build is interrupted, subsequent exeuction can cleanup.
            sharedm = sharedmanifests + "/" + os.path.basename(taskmanifest)
            if not os.path.exists(sharedm):
                smlock = bb.utils.lockfile(sharedm + ".lock")
                # Can race here. You'd think it just means we may not end up with all copies hardlinked to each other
                # but python can lose file handles so we need to do this under a lock.
                if not os.path.exists(sharedm):
                    with open(sharedm, 'w') as m:
                       for l in newmanifest:
                           dest = newmanifest[l]
                           m.write(dest.replace(workdir + "/", "") + "\n")
                bb.utils.unlockfile(smlock)
            try:
                os.link(sharedm, taskmanifest)
            except OSError as err:
                if err.errno == errno.EXDEV:
                    bb.utils.copyfile(sharedm, taskmanifest)
                else:
                    raise
            # Finally actually install the files
            for l in newmanifest:
                    dest = newmanifest[l]
                    if l.endswith("/"):
                        staging_copydir(l, targetdir, dest, seendirs)
                        continue
                    if "/bin/" in l or "/sbin/" in l:
                        # defer /*bin/* files until last in case they need libs
                        binfiles[l] = (targetdir, dest)
                    else:
                        try:
                            staging_copyfile(l, targetdir, dest, postinsts, seendirs)
                        except FileExistsError as e:
                            pass
                            # bb.warn("Failed to copy %s to %s: %s" % (l, dest, e))

    # Handle deferred binfiles
    for l in binfiles:
        (targetdir, dest) = binfiles[l]
        staging_copyfile(l, targetdir, dest, postinsts, seendirs)

    bb.note("Installed into sysroot: %s" % str(msg_adding))
    bb.note("Skipping as already exists in sysroot: %s" % str(msg_exists))

    for f in fixme:
        staging_processfixme(fixme[f], f, recipesysroot, recipesysrootnative, d)

    for p in sorted(postinsts):
        bb.note("Running postinst {}, output:\n{}".format(p, subprocess.check_output(p, shell=True, stderr=subprocess.STDOUT)))

    for dep in manifests:
        c = setscenedeps[dep][0]
        os.symlink(manifests[dep], depdir + "/" + c + ".complete")

    with open(taskindex, "w") as f:
        f.write("TaskDeps: " + " ".join(owntaskdeps) + "\n")
        for l in sorted(installed):
            f.write(l + "\n")

    bb.utils.unlockfile(lock)
}

