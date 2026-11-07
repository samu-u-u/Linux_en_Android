# Linux en Android

Aplicación Android sencilla que actúa como lanzador/gestor de una **distro Linux
con interfaz gráfica (escritorio)** ejecutándose sobre el móvil, sin root.

La app no emula hardware: Android ya usa un kernel Linux, así que lo que hace es
orquestar contenedores Linux en espacio de usuario mediante proyectos de código
abierto ya existentes:

- **[Termux](https://github.com/termux/termux-app)** (GPLv3) — terminal y entorno
  Linux para Android. La app le envía comandos mediante el intent
  `com.termux.RUN_COMMAND` (`RunCommandService`).
- **[proot-distro](https://github.com/termux/proot-distro)** (GPLv3) — instala y
  gestiona distros completas (desde imágenes OCI de Docker Hub: `alpine`,
  `debian`, `ubuntu`, `archlinux`, `fedora`, …) dentro de Termux usando
  [proot](https://github.com/proot-me/proot) (ptrace), sin root.
- **[Termux:X11](https://github.com/termux/termux-x11)** (GPLv3) — servidor X11
  para Android que muestra la GUI de la distro directamente en la pantalla del
  móvil (`DISPLAY=:0`).

## Requisitos previos (una sola vez)

1. Instala **Termux** y **Termux:X11** desde
   [F-Droid](https://f-droid.org/) (Termux ya no está en Google Play):
   - https://f-droid.org/packages/com.termux/
   - https://f-droid.org/packages/com.termux.x11/
2. Abre Termux y concede permiso a apps externas:
   ```
   echo "allow-external-apps = true" >> ~/.termux/termux.properties
   ```
   y reinicia Termux.
3. En **Ajustes de Android → Apps → Linux en Android → Permisos**, concede el
   permiso adicional **"Run commands in Termux environment"**
   (`com.termux.permission.RUN_COMMAND`).

## Uso

1. Abre la app y pulsa **Comprobar estado**: debe indicar que Termux y Termux:X11
   están instalados y listar las distros ya instaladas (`proot-distro list`).
2. Elige una distro y pulsa **1. Instalar distro** (descarga la imagen; puede
   tardar varios minutos). El progreso se ve en la propia app de Termux y en el
   registro inferior.
3. Pulsa **2. Instalar escritorio (XFCE)** para instalar el entorno gráfico
   dentro de la distro (usa el gestor de paquetes de cada distro: `apk`, `apt`,
   `pacman`, `dnf`).
4. Pulsa **3. Abrir escritorio**: se abre Termux:X11 y se lanza XFCE dentro de la
   distro con `--shared-tmp --shared-x11` y `DISPLAY=:0`.

## Compilar

Con Android Studio (recomendado) o desde terminal con JDK 17:

```
gradle wrapper        # solo la primera vez, para generar gradlew
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

- AGP 8.5.2, Kotlin 1.9.24, Gradle 8.7, `compileSdk 34`, `minSdk 24`, `targetSdk 34`.
- Solo dependencias de AndroidX/Material; sin servicios externos.

## Estructura

```
app/src/main/java/com/samu/linuxenandroid/
├── MainActivity.kt            # UI: estado, botones instalar/lanzar, registro
├── TermuxBridge.kt            # puente con Termux vía RUN_COMMAND + PendingIntent
├── CommandResultReceiver.kt   # recibe stdout/stderr/exit code de los comandos
└── Distro.kt                  # distros soportadas y comandos de GUI por distro
```

## Notas y limitaciones

- No es una máquina virtual con virtualización de hardware (KVM no está
  disponible sin root); es un contenedor proot sobre el kernel de Android.
- La primera ejecución de `proot-distro install` descarga cientos de MB.
- Distros ligeras recomendadas en móvil: Alpine y Debian con XFCE.
- Para sesiones de terminal dentro de la distro, usa el botón **Abrir Termux** y
  `proot-distro login <distro>`.

## Licencias de los componentes upstream

Termux, proot-distro, proot y Termux:X11 son GPLv3. Esta app no redistribuye su
código: solo se comunica con ellos mediante intents públicos, así que puedes
elegir la licencia que prefieras para este repositorio.
