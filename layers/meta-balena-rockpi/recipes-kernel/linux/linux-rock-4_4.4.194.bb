DESCRIPTION = "Linux kernel for Rock-4C+"

inherit kernel
inherit python3native
require recipes-kernel/linux/linux-yocto.inc

DEPENDS += "openssl-native u-boot-mkimage-radxa-native"
do_compile[depends] += "u-boot-mkimage-radxa-native:do_populate_sysroot"

SRC_URI = " \
	git://github.com/radxa/kernel.git;branch=stable-4.4-rk3399; \
	file://hw_intfc.conf \
	file://brcmfmac.scc \
"

S = "${WORKDIR}/git"
SRCREV = "4e1e1f3520cd63c496b16104d7f7cb524a206a83"

LINUX_VERSION = "4.4.194-rockchip"
KCONFIG_MODE = "alldefconfig"

# Override local version in order to use the one generated by linux build system
# And not "yocto-standard"
LINUX_VERSION_EXTENSION = ""
PR = "r1"
PV = "${LINUX_VERSION}"

# Include only supported boards for now
COMPATIBLE_MACHINE = "(rk3036|rk3066|rk3288|rk3328|rk3399|rk3308)"

do_compile_append() {
	oe_runmake dtbs
}

# Make sure we use /usr/bin/env ${PYTHON_PN} for scripts
do_patch_append() {
	for s in `grep -rIl python ${S}/scripts`; do
		sed -i -e '1s|^#!.*python[23]*|#!/usr/bin/env ${PYTHON_PN}|' $s
	done
}

do_deploy_append() {
	install -d ${DEPLOYDIR}/overlays
	install -m 644 ${WORKDIR}/linux-rock_4*/arch/arm64/boot/dts/rockchip/overlay/* ${DEPLOYDIR}/overlays
	install -m 644 ${WORKDIR}/hw_intfc.conf ${DEPLOYDIR}/
}

# we need some deps for the backported brcmfmac driver as per the README of the brcmfmac backport from Infineon
BALENA_CONFIGS_append_rock-4c-plus-rk3399 = " backported-brcmfmac clk-regmap"

BALENA_CONFIG[clk-regmap] += " \
    CONFIG_COMMON_CLK_ROCKCHIP_REGMAP=y \
"

