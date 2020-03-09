package com.insane.utlis

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.insane.utlis.statusBar.MeiZuStatusBarUtils
import com.insane.utlis.statusBar.OSUtils
import com.insane.utlis.statusBar.OppoStatusBarUtils
import com.insane.utlis.statusBar.SystemBarTintManager

/**
 *Created by 翊宸
 *Data:2020-03-06
 *Describe:
 */
class StatusBarUtil {

    fun init(activity: Activity) {
        init(activity, false)
    }

    fun init(activity: Activity?, dark: Boolean) {
        if (activity == null) {
            return
        }
        setTranslucentStatus(activity.window)
        if (dark) {
            if (!setStatusBarDarkTheme(activity, true)) {
                //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
                //这样半透明+白=灰, 状态栏的文字能看得清
                setStatusBarColor(activity, 0x55000000)
            }
        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param colorId 颜色
     */
    private fun setStatusBarColor(activity: Activity, colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.statusBarColor = colorId
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTintManager,需要先将状态栏设置为透明
            setTranslucentStatus(activity.window)
            val systemBarTintManager = SystemBarTintManager(activity)
            systemBarTintManager.apply {
                isStatusBarTintEnabled = true//显示状态栏
                setStatusBarTintColor(colorId)//设置状态栏颜色
            }
        }
    }

    /**
     * 设置状态栏深色浅色切换
     */
    private fun setStatusBarDarkTheme(activity: Activity, dark: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return if (OSUtils.isMiui() && !OSUtils.isMIUI7Later()) {
                setMiUiUI(activity, dark)
            } else if (OSUtils.isFlyme()) {
                MeiZuStatusBarUtils.setStatusBarDarkIcon(activity, dark)
                true
            } else if (OSUtils.isOppo()) {
                OppoStatusBarUtils.setLightStatusBarIcon(activity, dark)
                true
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setCommonUI(activity, dark)
            } else {//其他情况
                false
            }
        }
        return false
    }

    private fun setTranslucentStatus(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            val decorView = window.decorView
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = option
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.TRANSPARENT
            }
            //导航栏颜色也可以正常设置
            //window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val attributes = window.attributes
            val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            attributes.flags = attributes.flags or flagTranslucentStatus
            //int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            //attributes.flags |= flagTranslucentNavigation;
            window.attributes = attributes
        }
    }

    //设置6.0 状态栏深色浅色切换
    private fun setCommonUI(activity: Activity, dark: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = activity.window.decorView
            var vis = decorView.systemUiVisibility
            vis = if (dark) {
                vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            if (decorView.systemUiVisibility != vis) {
                decorView.systemUiVisibility = vis
            }
            return true
        }
        return false

    }

    //设置MiUi 状态栏深色浅色切换
    private fun setMiUiUI(activity: Activity, dark: Boolean): Boolean {
        val window = activity.window
        val clazz = window.javaClass
        try {
            // do not set anything for this
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            val darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod(
                "setExtraFlags",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            extraFlagField.invoke(window, if (dark) darkModeFlag else 0, darkModeFlag)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }
}