package com.danghung.elearning.api.res

import java.io.Serializable

data class PageRes<T>(
    val pageNo: Int,
    val pageSize: Int,
    val totalPage: Int,
    val totalElements: Long,
    val items: T
): Serializable