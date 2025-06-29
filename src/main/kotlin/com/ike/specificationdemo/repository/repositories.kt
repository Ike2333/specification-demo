package com.ike.specificationdemo.repository

import com.ike.specificationdemo.entity.PermissionEntity
import com.ike.specificationdemo.entity.RoleEntity
import com.ike.specificationdemo.entity.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

/**
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 28/6/2025
 */
interface UserRepository : JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    /**
     * 直接使用投影作为返回时, 命名必须是 `find{ProjectionClassName}By`, 你可以在投影中定义要select的字段的getter, 返回值必须与实体类一致
     */
    fun findSimpleUserProjectionBy(): List<SimpleUserProjection>
    fun findSimpleUserDTOBy(): List<SimpleUserDTO>


    @EntityGraph(attributePaths = ["roles"])
    @Query("select distinct u from UserEntity u")
    fun findBy(spec: Specification<UserEntity>, pageable: Pageable): Page<UserEntityInfo>

}

interface RoleRepository: JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity>

interface PermissionRepository: JpaRepository<PermissionEntity, Long>, JpaSpecificationExecutor<PermissionEntity>