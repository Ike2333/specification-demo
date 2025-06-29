package com.ike.specificationdemo.repository

import java.time.Instant

/**
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 28/6/2025
 */
data class UserDTO(
    val id: Long?,
    val username: String?,
    val password: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val roleNames: Set<String?> = mutableSetOf(),
    val rolePaths: Set<String?> = mutableSetOf(),
)


data class UserFlatDTO(
    val id: Long?,
    val username: String?,
    val password: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val roleName: String?,
    val rolePath: String?,
)

data class SimpleUserDTO(
    val id: Long?,
    val username: String?,
    val password: String?,
    val email: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

data class SimplerUserDTO(
    val id: Long?,
    val username: String?,
    val email: String?,
)

data class UserRoleDTO(
    val username: String?,
    val email: String?,
    val roleNames: List<String>?
)