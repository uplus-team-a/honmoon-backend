package site.honmoon.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

/**
 * AbstractProjectConfig를 상속해서 Kotest에 전역적인 설정을 해줄 수 있습니다.
 * SpringExtension : Kotest에서 DI를 위해 스프링을 사용하는 코드를 테스트할 수 있는 Spring Extension을 제공합니다.
 */
object KoTestSpringBootSupportConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)
}
