package com.milepicture.app.data.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

data class NetworkLogItem(
    val id: Long,
    val time: String,
    val source: String,
    val url: String,
    val latencyMs: Long,
    val isSuccess: Boolean,
    val statusCode: Int,
    val itemCount: Int,
    val errorMessage: String? = null
)

/**
 * 商业级应用内实时网络与 Bug 诊断排查日志中心
 * 1. 自动记录每次 API 请求的 URL、延迟、HTTP 状态码、返回素材数、异常信息
 * 2. 内存环形队列存储最新 100 条实时日志，支持 UI 实时响应式观察与一键复制排查
 */
object NetworkLogger {

    private val logsQueue = ConcurrentLinkedDeque<NetworkLogItem>()
    private val _logsFlow = MutableStateFlow<List<NetworkLogItem>>(emptyList())
    val logsFlow: StateFlow<List<NetworkLogItem>> = _logsFlow.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private var counter = 0L

    fun log(
        source: String,
        url: String,
        latencyMs: Long,
        isSuccess: Boolean,
        statusCode: Int,
        itemCount: Int,
        errorMessage: String? = null
    ) {
        val item = NetworkLogItem(
            id = ++counter,
            time = timeFormat.format(Date()),
            source = source,
            url = url,
            latencyMs = latencyMs,
            isSuccess = isSuccess,
            statusCode = statusCode,
            itemCount = itemCount,
            errorMessage = errorMessage
        )
        logsQueue.addFirst(item)
        while (logsQueue.size > 100) {
            logsQueue.removeLast()
        }
        _logsFlow.value = logsQueue.toList()
    }

    fun clear() {
        logsQueue.clear()
        _logsFlow.value = emptyList()
    }

    fun exportSummary(): String {
        val list = logsQueue.toList()
        if (list.isEmpty()) return "暂无网络请求日志记录。"

        val sb = StringBuilder()
        sb.append("=== MiLePicture 实时网络诊断与 Bug 排查日志 ===\n")
        sb.append("生成时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("总记录数: ${list.size}\n\n")

        for (log in list) {
            sb.append("[${log.time}] 【${log.source.uppercase()}】 ${if (log.isSuccess) "✅ 成功" else "❌ 失败"}\n")
            sb.append("  - 耗时: ${log.latencyMs}ms | 状态码: ${log.statusCode} | 获取条数: ${log.itemCount}\n")
            sb.append("  - 请求URL: ${log.url}\n")
            if (!log.errorMessage.isNullOrBlank()) {
                sb.append("  - 错误详情: ${log.errorMessage}\n")
            }
            sb.append("--------------------------------------------------\n")
        }
        return sb.toString()
    }
}
