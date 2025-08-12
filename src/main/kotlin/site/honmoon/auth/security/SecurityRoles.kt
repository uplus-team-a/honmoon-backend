package site.honmoon.auth.security

/**
 * 보안 영역에서 사용하는 역할/권한 상수 정의.
 * - SecurityConfig의 hasRole()/roles()에는 접두사 없는 값을 사용한다.
 * - Authentication 권한 목록(GrantedAuthority)에는 ROLE_ 접두사가 포함된 값을 사용한다.
 */
object SecurityRoles {
    const val USER: String = "USER"
    const val ADMIN: String = "ADMIN"
}

object SecurityAuthorities {
    const val ROLE_USER: String = "ROLE_USER"
    const val ROLE_ADMIN: String = "ROLE_ADMIN"
}


