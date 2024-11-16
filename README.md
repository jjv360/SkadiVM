# SkadiVM

A tool for running virtual machines on Android. Can run on x86_64 and ARM64 devices, and supports any number of emulated architectures.

> Is this project even possible? Qemu needs to be run as a binary, and Android doesn't allow that. There are workarounds that work in the emulator but not on device.

## Development

Just run it in Android Studio normally.

If you want to update or rebuild Qemu, get yourself some Node.js and Docker and then run `npm run build:qemu`. The [Qemu dockerfile](./scripts/Dockerfile.qemu) contols which emulated architectures will be available to use in the app.