package io.launchcore.core

/**
 * 宿主侧全局配置，[LaunchCore.create] 时注入。
 *
 * @param microsoftClientId 宿主自己注册的 Azure 应用 Client ID。
 *   微软登录必须由宿主提供（ADR-11），未配置时调用 [Auth.microsoft] 会抛出
 *   [IllegalStateException]；离线登录不依赖此项。
 */
@UnstableApi
class LaunchCoreConfig(
    val microsoftClientId: String? = null,
)
