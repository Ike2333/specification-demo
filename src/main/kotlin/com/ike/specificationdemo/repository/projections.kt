package com.ike.specificationdemo.repository

import java.time.Instant

/**
 * 接口投影, 虽然官方曾经推荐, 但在后续版本中可能会被更直接的映射方式(数据类)取代
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 28/6/2025
 */
interface UserProjection {
    fun getId(): Long?
    fun getUsername(): String?
    fun getEmail(): String?
    fun getRoles(): MutableList<RoleProjection>
}

interface RoleProjection {
    fun getName(): String?
    fun getPath(): String?
}

interface SimpleUserProjection {
    fun getUsername(): String?
    fun getPassword(): String?
    fun getEmail(): String?
    fun getUpdatedAt(): Instant?
    fun getCreatedAt(): Instant?
    fun getId(): Long?
}