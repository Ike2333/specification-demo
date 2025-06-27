package com.ike.specificationdemo.entity

import jakarta.persistence.*

/**
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 27/6/2025
 */
@Entity
@Table(
    name = "cust_role", indexes = [
        Index(name = "idx_role_name", columnList = "name", unique = true),
        Index(name = "idx_role_path", columnList = "path", unique = true)
    ]
)
class RoleEntity : BaseEntity() {

    var name: String? = null

    var path: String? = null

    @ManyToMany(mappedBy = "roles")
    var users: MutableList<UserEntity>? = mutableListOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "cust_role_permission",
        joinColumns = [JoinColumn(name = "cust_role_id")],
        inverseJoinColumns = [JoinColumn(name = "cust_permission_id")]
    )
    var permissions: MutableList<PermissionEntity> = mutableListOf()
}