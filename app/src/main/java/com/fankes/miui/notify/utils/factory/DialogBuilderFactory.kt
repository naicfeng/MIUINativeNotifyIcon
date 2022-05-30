/*
 * MIUINativeNotifyIcon - Fix the native notification bar icon function abandoned by the MIUI development team.
 * Copyright (C) 2019-2022 Fankes Studio(qzmmcn@163.com)
 * https://github.com/fankes/MIUINativeNotifyIcon
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 * <p>
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 *
 * This file is Created by fankes on 2022/1/7.
 */
@file:Suppress("unused", "OPT_IN_USAGE", "EXPERIMENTAL_API_USAGE")

package com.fankes.miui.notify.utils.factory

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.yukihookapi.annotation.CauseProblemsApi
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.LayoutInflaterClass

/**
 * 构造对话框
 * @param isUseBlackTheme 是否使用深色主题
 * @param initiate 对话框方法体
 */
fun Context.showDialog(isUseBlackTheme: Boolean = false, initiate: DialogBuilder.() -> Unit) =
    DialogBuilder(context = this, isUseBlackTheme).apply(initiate).show()

/**
 * 显示时间选择对话框
 * @param timeSet 当前时间 - 不写将使用当前时间格式：HH:mm
 * @param result 回调 - 小时与分钟 HH:mm
 */
fun Context.showTimePicker(timeSet: String = "", result: (String) -> Unit) =
    TimePickerDialog(this, { _, h, m -> result("${h.autoZero}:${m.autoZero}") }, timeSet.hour, timeSet.minute, true).show()

/**
 * 对话框构造器
 * @param context 实例
 * @param isUseBlackTheme 是否使用深色主题 - 对 AndroidX 风格无效
 */
class DialogBuilder(val context: Context, private val isUseBlackTheme: Boolean) {

    private var instanceAndroidX: androidx.appcompat.app.AlertDialog.Builder? = null // 实例对象
    private var instanceAndroid: android.app.AlertDialog.Builder? = null // 实例对象

    private var dialogInstance: Dialog? = null // 对话框实例

    @CauseProblemsApi
    var customLayoutView: View? = null // 自定义布局

    /**
     * 是否需要使用 AndroidX 风格对话框
     * @return [Boolean]
     */
    private val isUsingAndroidX get() = runCatching { context is AppCompatActivity }.getOrNull() ?: false

    init {
        if (isUsingAndroidX)
            runInSafe { instanceAndroidX = MaterialAlertDialogBuilder(context) }
        else runInSafe {
            instanceAndroid = android.app.AlertDialog.Builder(
                context,
                if (isUseBlackTheme) android.R.style.Theme_Material_Dialog else android.R.style.Theme_Material_Light_Dialog
            )
        }
    }

    /** 设置对话框不可关闭 */
    fun noCancelable() {
        if (isUsingAndroidX)
            runInSafe { instanceAndroidX?.setCancelable(false) }
        else runInSafe { instanceAndroid?.setCancelable(false) }
    }

    /** 设置对话框标题 */
    var title
        get() = ""
        set(value) {
            if (isUsingAndroidX)
                runInSafe { instanceAndroidX?.setTitle(value) }
            else runInSafe { instanceAndroid?.setTitle(value) }
        }

    /** 设置对话框消息内容 */
    var msg
        get() = ""
        set(value) {
            if (isUsingAndroidX)
                runInSafe { instanceAndroidX?.setMessage(value) }
            else runInSafe { instanceAndroid?.setMessage(value) }
        }

    /** 设置进度条对话框消息内容 */
    var progressContent
        get() = ""
        set(value) {
            if (customLayoutView == null)
                customLayoutView = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER or Gravity.START
                    addView(ProgressBar(context))
                    addView(View(context).apply { layoutParams = ViewGroup.LayoutParams(20.dp(context), 5) })
                    addView(TextView(context).apply {
                        tag = "progressContent"
                        text = value
                    })
                    setPadding(20.dp(context), 20.dp(context), 20.dp(context), 20.dp(context))
                }
            else customLayoutView?.findViewWithTag<TextView>("progressContent")?.text = value
        }

    /**
     * 设置对话框自定义布局
     * @return [ViewBinding]
     */
    inline fun <reified T : ViewBinding> bind() =
        T::class.java.method {
            name = "inflate"
            param(LayoutInflaterClass)
        }.get().invoke<T>(LayoutInflater.from(context))?.apply {
            customLayoutView = root
        } ?: error("binding failed")

    /**
     * 设置对话框确定按钮
     * @param text 按钮文本内容
     * @param callback 点击事件
     */
    fun confirmButton(text: String = "确定", callback: () -> Unit = {}) {
        if (isUsingAndroidX)
            runInSafe { instanceAndroidX?.setPositiveButton(text) { _, _ -> callback() } }
        else runInSafe { instanceAndroid?.setPositiveButton(text) { _, _ -> callback() } }
    }

    /**
     * 设置对话框取消按钮
     * @param text 按钮文本内容
     * @param callback 点击事件
     */
    fun cancelButton(text: String = "取消", callback: () -> Unit = {}) {
        if (isUsingAndroidX)
            runInSafe { instanceAndroidX?.setNegativeButton(text) { _, _ -> callback() } }
        else runInSafe { instanceAndroid?.setNegativeButton(text) { _, _ -> callback() } }
    }

    /**
     * 设置对话框第三个按钮
     * @param text 按钮文本内容
     * @param callback 点击事件
     */
    fun neutralButton(text: String = "更多", callback: () -> Unit = {}) {
        if (isUsingAndroidX)
            runInSafe { instanceAndroidX?.setNeutralButton(text) { _, _ -> callback() } }
        else runInSafe { instanceAndroid?.setNeutralButton(text) { _, _ -> callback() } }
    }

    /** 取消对话框 */
    fun cancel() = dialogInstance?.cancel()

    /** 显示对话框 */
    internal fun show() =
        if (isUsingAndroidX) runInSafe {
            instanceAndroidX?.create()?.apply {
                customLayoutView?.let { setView(it) }
                dialogInstance = this
            }?.show()
        } else runInSafe {
            instanceAndroid?.create()?.apply {
                customLayoutView?.let { setView(it) }
                window?.setBackgroundDrawable(
                    GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        if (isUseBlackTheme) intArrayOf(0xFF2D2D2D.toInt(), 0xFF2D2D2D.toInt())
                        else intArrayOf(Color.WHITE, Color.WHITE)
                    ).apply {
                        shape = GradientDrawable.RECTANGLE
                        gradientType = GradientDrawable.LINEAR_GRADIENT
                        cornerRadius = 15.dpFloat(this@DialogBuilder.context)
                    })
                dialogInstance = this
            }?.show()
        }
}