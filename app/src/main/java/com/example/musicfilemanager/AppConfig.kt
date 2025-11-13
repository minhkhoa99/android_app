package com.example.musicfilemanager

/**
 * App configuration constants
 */
object AppConfig {

    /**
     * API Base URL
     *
     * Môi trường development:
     * - Android Emulator -> localhost: "http://10.0.2.2:3005/api/"
     * - Thiết bị thật -> local network: "http://192.168.1.XXX:3005/api/"
     *
     * Môi trường production:
     * - Server online: "https://api.yourdomain.com/api/"
     */
    const val API_BASE_URL = "http://10.0.2.2:3005/api/"

    /**
     * API Timeout (seconds)
     */
    const val API_TIMEOUT = 30L

    /**
     * Enable debug logging
     */
    const val DEBUG_MODE = true

    /**
     * App version
     */
    const val APP_VERSION = "1.0.0"
}

