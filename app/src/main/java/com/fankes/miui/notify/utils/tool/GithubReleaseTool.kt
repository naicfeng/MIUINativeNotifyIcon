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
 * This file is Created by fankes on 2022/3/20.
 */
package com.fankes.miui.notify.utils.tool

import android.app.Activity
import android.content.Context
import com.fankes.miui.notify.utils.factory.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.io.Serializable

/**
 * 获取 Github Release 最新版本工具类
 */
object GithubReleaseTool {

    /** 仓库作者 */
    private const val REPO_AUTHOR = "fankes"

    /** 仓库名称 */
    private const val REPO_NAME = "MIUINativeNotifyIcon"

    /**
     * 获取最新版本信息
     * @param context 实例
     * @param version 当前版本
     * @param result 成功后回调 - ([String] 最新版本,[Function] 更新对话框方法体)
     */
    fun checkingForUpdate(context: Context, version: String, result: (String, () -> Unit) -> Unit) = checkingInternetConnect(context) {
        OkHttpClient().newBuilder().build().newCall(
            Request.Builder()
                .url("https://api.github.com/repos/$REPO_AUTHOR/$REPO_NAME/releases/latest")
                .get()
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) = runInSafe {
                JSONObject(response.body?.string() ?: "").apply {
                    GithubReleaseBean(
                        name = getString("name"),
                        htmlUrl = getString("html_url"),
                        content = getString("body"),
                        date = getString("published_at").replace(oldValue = "T", newValue = " ").replace(oldValue = "Z", newValue = "")
                    ).apply {
                        fun showUpdate() = context.showDialog {
                            title = "最新版本 $name"
                            msg = "发布于 $date\n\n" +
                                    "更新日志\n\n" + content
                            confirmButton(text = "更新") { context.openBrowser(htmlUrl) }
                            cancelButton()
                        }
                        if (name != version) (context as? Activity?)?.runOnUiThread {
                            showUpdate()
                            result(name) { showUpdate() }
                        }
                    }
                }
            }
        })
    }

    /**
     * 检查网络连接情况
     * @param context 实例
     * @param callback 已连接回调
     */
    private fun checkingInternetConnect(context: Context, callback: () -> Unit) = runInSafe {
        if (isNetWorkSuccess)
            OkHttpClient().newBuilder().build().newCall(
                Request.Builder()
                    .url("https://www.baidu.com")
                    .get()
                    .build()
            ).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    (context as? Activity?)?.runOnUiThread {
                        context.showDialog {
                            title = "网络不可用"
                            msg = "模块的联网权限可能已被禁用，请开启联网权限以定期检查更新。"
                            confirmButton(text = "去开启") { context.openSelfSetting() }
                            cancelButton()
                            noCancelable()
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) = runInSafe {
                    (context as? Activity?)?.runOnUiThread { runInSafe { callback() } }
                }
            })
    }

    /**
     * Github Release bean
     * @param name 版本名称
     * @param htmlUrl 网页地址
     * @param content 更新日志
     * @param date 发布时间
     */
    private data class GithubReleaseBean(
        var name: String,
        var htmlUrl: String,
        var content: String,
        var date: String
    ) : Serializable
}