package com.ike.specificationdemo.entity

import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

/**
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 27/6/2025
 */
@Entity
@Table(
    name = "cust_permission",
    indexes = [
        Index(name = "idx_permission_name", columnList = "name", unique = true),
        Index(name = "idx_permission_path", columnList = "path", unique = true)
    ]
)
class PermissionEntity : BaseEntity() {

    var name: String? = null

    var path: String? = null

    @ManyToMany(mappedBy = "permissions")
    var roles: MutableList<RoleEntity>? = mutableListOf()
}