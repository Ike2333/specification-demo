package com.ike.specificationdemo.repository

import com.ike.specificationdemo.entity.PermissionEntity
import com.ike.specificationdemo.entity.RoleEntity
import com.ike.specificationdemo.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

/**
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 28/6/2025
 */
interface UserRepository : JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    /**
     * 直接使用投影作为返回时, 命名必须是 `find{ProjectionClassName}By`, 你可以在投影中定义要select的字段的getter, 返回值必须与实体类一致
     */
    fun findSimpleUserProjectionBy(): List<SimpleUserProjection>
    fun findSimpleUserDTOBy(): List<SimpleUserDTO>
    fun findSimplerUserDTOBy(): List<SimplerUserDTO>
}

interface RoleRepository: JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity>

interface PermissionRepository: JpaRepository<PermissionEntity, Long>, JpaSpecificationExecutor<PermissionEntity>