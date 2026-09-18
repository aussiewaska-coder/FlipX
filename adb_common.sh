#!/data/data/com.termux/files/usr/bin/bash
# adb_common.sh - shared ADB target auto-detection for FlipX scripts.
# Wireless debugging uses a dynamic port that changes every restart,
# so scripts must never hardcode 127.0.0.1:5555.
# Usage: source "$HOME/adb_common.sh", then call pick_adb_device.
# Sets ADB_TARGET (e.g. 10.41.132.32:43527, empty if none) and
# ADB_SHELL (e.g. "adb -s 10.41.132.32:43527 shell").

pick_adb_device() {
    ADB_TARGET=""
    if command -v adb >/dev/null 2>&1; then
        # Prefer loopback - it works with WiFi off and never rotates ports
        ADB_TARGET=$(adb devices 2>/dev/null | awk '$1=="127.0.0.1:5555" && $2=="device" {print $1; exit}')
        if [ -z "$ADB_TARGET" ]; then
            ADB_TARGET=$(adb devices 2>/dev/null | awk '$2=="device" {print $1; exit}')
        fi
        if [ -z "$ADB_TARGET" ]; then
            adb connect 127.0.0.1:5555 >/dev/null 2>&1 || true
            ADB_TARGET=$(adb devices 2>/dev/null | awk '$1=="127.0.0.1:5555" && $2=="device" {print $1; exit}')
            if [ -z "$ADB_TARGET" ]; then
                ADB_TARGET=$(adb devices 2>/dev/null | awk '$2=="device" {print $1; exit}')
            fi
        fi
    fi
    if [ -n "$ADB_TARGET" ]; then
        ADB_SHELL="adb -s $ADB_TARGET shell"
    else
        ADB_SHELL="adb shell"
    fi
}
