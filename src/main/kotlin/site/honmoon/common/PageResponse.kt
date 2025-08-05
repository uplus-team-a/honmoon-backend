package site.honmoon.common

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalSize: Long = content.size.toLong(),
)
