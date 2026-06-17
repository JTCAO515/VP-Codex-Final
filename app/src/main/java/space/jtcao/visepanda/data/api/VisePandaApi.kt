package space.jtcao.visepanda.data.api

/**
 * 预留的 Retrofit 接口入口。
 *
 * 当前项目实际网络访问方式：
 * - 普通 REST 请求：各 repository 内使用 `URL(...).readText()`
 * - SSE 聊天：`SseClient`
 *
 * 保留此文件是为了后续若要统一网络层时，有明确的扩展入口。
 */
interface VisePandaApi
