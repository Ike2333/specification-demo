package com.ike.specificationdemo.entity

import jakarta.persistence.*
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

/**
 * MappedSuperclass专用于为其他jpa实体提供公共属性和方法
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 27/6/2025
 */
@MappedSuperclass
class BaseEntity {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id var id: Long? = null

    @Column(updatable = false)
    var createdBy: String? = null

    @Column(updatable = false)
    @UpdateTimestamp(source = SourceType.VM)
    var createdAt:Instant? = Instant.now()

    @UpdateTimestamp(source = SourceType.VM)
    var updatedAt: Instant? = Instant.now()

}