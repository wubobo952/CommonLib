package com.insane.utlis.statusBar

import android.os.Build
import android.text.TextUtils
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 *Created by 翊宸
 *Data:2020-03-09
 *Describe:
 */
object OSUtil {

    const val ROM_MIUI = "MIUI"
    const val ROM_EMUI = "EMUI"
    const val ROM_FLYME = "FLYME"
    const val ROM_OPPO = "OPPO"
    const val ROM_SMARTISAN = "SMARTISAN"
    const val ROM_VIVO = "VIVO"
    const val ROM_QIKU = "QIKU"

    private val KEY_VERSION_MIUI = "ro.miui.ui.version.name"
    private val KEY_VERSION_BUILD = "ro.build.version.incremental"
    private val KEY_VERSION_EMUI = "ro.build.version.emui"
    private val KEY_VERSION_OPPO = "ro.build.version.opporom"
    private val KEY_VERSION_SMARTISAN = "ro.smartisan.version"
    private val KEY_VERSION_VIVO = "ro.vivo.os.version"

    private var sName: String? = null
    private var sVersion: String? = null

    val isEmui: Boolean
        get() = check(ROM_EMUI)

    val isMiui: Boolean
        get() = check(ROM_MIUI)

    val isVivo: Boolean
        get() = check(ROM_VIVO)

    val isOppo: Boolean
        get() = check(ROM_OPPO)

    val isFlyme: Boolean
        get() = check(ROM_FLYME)

    val is360: Boolean
        get() = check(ROM_QIKU) || check("360")

    val isSmartisan: Boolean
        get() = check(ROM_SMARTISAN)

    val name: String?
        get() {
            if (sName == null) {
                check("")
            }
            return sName
        }

    val version: String?
        get() {
            if (sVersion == null) {
                check("")
            }
            return sVersion
        }

    // eg.V7.5.5.0.MXGCNDE
    // 新的 MIUI 版本（即基于 Android 6.0 ，开发版 7.7.13 及以后版本）
    val isMIUI7Later: Boolean
        get() {
            if (isMiui) {
                val buildVersionString = getProp(KEY_VERSION_BUILD)
                if (TextUtils.isEmpty(buildVersionString)) {
                    return false
                }
                try {
                    val buildVersionArray =
                        buildVersionString!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    buildVersionArray[0] = buildVersionArray[0].replace("[vV]".toRegex(), "")
                    val buildVersionA = Integer.parseInt(buildVersionArray[0])
                    val buildVersionB = Integer.parseInt(buildVersionArray[1])
                    val buildVersionC = Integer.parseInt(buildVersionArray[2])
                    return buildVersionA > 7 || buildVersionA == 7 && buildVersionB > 7 || buildVersionA == 7 && buildVersionB == 7 && buildVersionC >= 13
                } catch (e: Exception) {
                    return false
                }

            } else {
                return false
            }
        }

    private fun check(rom: String): Boolean {
        if (sName != null) {
            return sName == rom
        }
        when (rom) {
            ROM_MIUI -> {
                sVersion = getProp(KEY_VERSION_MIUI)
                setName(ROM_MIUI)
            }
            ROM_EMUI -> {
                sVersion = getProp(KEY_VERSION_EMUI)
                setName(ROM_EMUI)
            }
            ROM_OPPO -> {
                sVersion = getProp(KEY_VERSION_OPPO)
                setName(ROM_OPPO)
            }
            ROM_VIVO -> {
                sVersion = getProp(KEY_VERSION_VIVO)
                setName(ROM_VIVO)
            }
            ROM_SMARTISAN -> {
                sVersion = getProp(KEY_VERSION_SMARTISAN)
                setName(ROM_SMARTISAN)
            }
            else -> {
                sVersion = Build.DISPLAY
                sVersion?.let {
                    if (it.toUpperCase().contains(ROM_FLYME)) {
                        sName = ROM_FLYME
                    } else {
                        sVersion = Build.UNKNOWN
                        sName = Build.MANUFACTURER.toUpperCase()
                    }
                }
            }
        }
        return sName == rom
    }

    private fun setName(rom: String) {
        sVersion?.let {
            sName = rom
        }
    }

    private fun getProp(name: String): String? {
        var line: String? = null
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $name")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: Exception) {
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return line
    }
}