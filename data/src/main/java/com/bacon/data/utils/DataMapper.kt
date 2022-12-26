package com.bacon.data.utils

interface DataMapper<T> {
    fun mapToDomain(): T
}