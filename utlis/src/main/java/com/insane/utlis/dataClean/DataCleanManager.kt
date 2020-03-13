package com.insane.utlis.dataClean

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.math.BigDecimal


class DataCleanManager {

    fun getDataPath(context: Context): String {
        return context.filesDir.parent
    }

    /**
     * 删除app_webview文件夹
     *
     * @param context
     */
    fun cleanWebCache(context: Context) {
        val path = getDataPath(context) + "/app_webview"
        deleteFolderFile(path, true)
    }

    /**
     * * 清除本应用内部缓存(/data/data/com.getTimeAfter7.getTimeAfter7/cache) * *
     *
     * @param context
     */
    fun cleanInternalCache(context: Context) {
        deleteFolderFile(context.cacheDir.path, true)
    }

    /**
     * * 清除本应用所有数据库(/data/data/com.getTimeAfter7.getTimeAfter7/databases) * *
     *
     * @param context
     */
    fun cleanDatabases(context: Context) {
        val path = getDataPath(context) + "/databases"
        deleteFolderFile(path, true)
    }

    /**
     * * 清除本应用SharedPreference(/data/data/com.getTimeAfter7.getTimeAfter7/shared_prefs) *
     *
     * @param context
     */
    fun cleanSharedPreference(context: Context) {
        val path = getDataPath(context) + "/shared_prefs"
        deleteFolderFile(path, true)
    }

    /**
     * * 按名字清除本应用数据库 * *
     *
     * @param context
     * @param dbName
     */
    fun cleanDatabaseByName(context: Context, dbName: String) {
        context.deleteDatabase(dbName)
    }

    /**
     * * 清除/data/data/com.getTimeAfter7.getTimeAfter7/files下的内容 * *
     *
     * @param context
     */
    fun cleanFiles(context: Context) {
        deleteFolderFile(context.filesDir.path, true)
    }

    /**
     * * 清除外部cache下的内容(/mnt/sdcard/android/data/com.getTimeAfter7.getTimeAfter7/cache)
     *
     * @param context
     */
    fun cleanExternalCache(context: Context) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val file = context.externalCacheDir
            if (file != null && !TextUtils.isEmpty(file.path)) {
                deleteFolderFile(file.path, true)
            }
        }
    }

    /**
     * * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除 * *
     *
     * @param filePath
     */
    fun cleanCustomCache(filePath: String) {
        deleteFilesByDirectory(File(filePath))
    }

    /**
     * * 清除本应用所有的数据 * *
     *
     * @param context
     */
    fun cleanApplicationData(context: Context) {
        cleanInternalCache(context)
        cleanExternalCache(context)
        cleanWebCache(context)
        // cleanDatabases(context);
        // cleanSharedPreference(context);
        cleanFiles(context)
    }

    @Throws(Exception::class)
    fun getCacheSize(context: Context): String {
        val `in` = getFolderSize(context.cacheDir)
        var ex: Long = 0
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            ex = getFolderSize(context.externalCacheDir)
        }
        val fi = getFolderSize(context.filesDir)
        val path = getDataPath(context) + "/app_webview"
        val web = getFolderSize(File(path))
        val size = getFormatSize((`in` + ex + fi + web).toDouble())
        return "已有缓冲$size"
    }

    /**
     * * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理 * *
     *
     * @param directory
     */
    private fun deleteFilesByDirectory(directory: File?) {
        if (directory != null && directory.exists() && directory.isDirectory) {
            for (item in directory.listFiles()!!) {
                item.delete()
            }
        }
    }

    // 获取文件
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    @Throws(Exception::class)
    fun getFolderSize(file: File?): Long {
        var size: Long = 0
        try {
            val fileList = file!!.listFiles()
            for (i in fileList!!.indices) {
                // 如果下面还有文件
                size = if (fileList[i].isDirectory) {
                    size + getFolderSize(fileList[i])
                } else {
                    size + fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return size
    }

    /**
     * 删除指定目录下文件及目录
     *
     * @param filePath
     * @param deleteThisPath
     * @return
     */
    fun deleteFolderFile(filePath: String?, deleteThisPath: Boolean) {
        filePath?.let {
            try {
                val file = File(it)
                if (file.isDirectory) {
                    val files = file.listFiles()
                    if (files.isNotEmpty()) {
                        for (i in files.indices) {
                            deleteFolderFile(files[i].absolutePath,true)
                        }
                    }
                }
                if (deleteThisPath){
                    if (!file.isDirectory){
                        file.delete()
                    }else{
                        if (file.listFiles().isEmpty()){
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    fun getFormatSize(size: Double): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return size.toString() + "Byte"
        }

        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(kiloByte.toString())
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(megaByte.toString())
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "MB"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(gigaByte.toString())
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
    }

    fun getFormatSize2(size: Double): String {
        val megaByte = size / 1024.0 / 1024.0
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(megaByte.toString())
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "MB"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(gigaByte.toString())
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
    }

    /**
     * 使用APP缓存路径
     *
     * @param context
     * @return
     */
    fun getVideoCacheDir(context: Context): File {
        val name = "/cache/audio-cache"
        return if (Environment.getExternalStorageState() === Environment.MEDIA_MOUNTED) {
            val path = "/Android/" + context.packageName + name
            File(Environment.getExternalStorageDirectory(), path)
        } else {
            File(context.cacheDir, name)
        }
    }
}