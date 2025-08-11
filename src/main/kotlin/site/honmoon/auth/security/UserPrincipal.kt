package site.honmoon.auth.security

data class UserPrincipal(
    val subject: String,
    val email: String?,
    val name: String?,
    val picture: String?,
    val provider: String,
    val roles: Set<String>,
) 
