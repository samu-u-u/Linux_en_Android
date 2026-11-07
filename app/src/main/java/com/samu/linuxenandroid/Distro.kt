package com.samu.linuxenandroid

data class Distro(
    val alias: String,
    val label: String,
    val image: String,
    val guiInstallCommand: String,
    val guiLaunchCommand: String,
) {
    companion object {
        private const val LAUNCH_DESKTOP =
            "export DISPLAY=:0; export PULSE_SERVER=127.0.0.1; " +
                "dbus-launch --exit-with-session startxfce4"

        val ALL = listOf(
            Distro(
                alias = "alpine",
                label = "Alpine Linux (ligera)",
                image = "alpine",
                guiInstallCommand = "apk add --no-cache xfce4 dbus dbus-x11",
                guiLaunchCommand = LAUNCH_DESKTOP,
            ),
            Distro(
                alias = "debian",
                label = "Debian",
                image = "debian",
                guiInstallCommand =
                    "apt-get update && apt-get install -y --no-install-recommends " +
                        "xfce4 xfce4-goodies dbus-x11",
                guiLaunchCommand = LAUNCH_DESKTOP,
            ),
            Distro(
                alias = "ubuntu",
                label = "Ubuntu",
                image = "ubuntu",
                guiInstallCommand =
                    "apt-get update && apt-get install -y --no-install-recommends " +
                        "xfce4 xfce4-goodies dbus-x11",
                guiLaunchCommand = LAUNCH_DESKTOP,
            ),
            Distro(
                alias = "archlinux",
                label = "Arch Linux",
                image = "archlinux",
                guiInstallCommand = "pacman -Sy --noconfirm --needed xfce4 xfce4-goodies dbus",
                guiLaunchCommand = LAUNCH_DESKTOP,
            ),
            Distro(
                alias = "fedora",
                label = "Fedora (imagen pesada)",
                image = "fedora",
                guiInstallCommand = "dnf install -y @xfce-desktop dbus-x11",
                guiLaunchCommand = LAUNCH_DESKTOP,
            ),
        )
    }
}
