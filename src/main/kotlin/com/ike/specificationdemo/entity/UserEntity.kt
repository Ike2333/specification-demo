package com.ike.specificationdemo.entity

import jakarta.persistence.*

/**
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 27/6/2025
 */
@Entity
@Table(
    name = "cust_user",
    indexes = [
        Index(name = "idx_user_username", columnList = "username", unique = true),
        Index(name = "idx_user_email", columnList = "email", unique = true)
    ]
)
@SequenceGenerator(name = "user_seq")
class UserEntity:BaseEntity() {
    @Column(updatable = false, length = 32, unique = true)
    var username:String? = null
    @Column(length = 64)
    var password:String? = null
    var email:String? = null

    @JoinTable(name = "cust_user_role",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    @ManyToMany(fetch = FetchType.LAZY)
    var roles: MutableList<RoleEntity>? = mutableListOf()
}