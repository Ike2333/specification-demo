package com.ike.specificationdemo.service

import com.ike.specificationdemo.entity.PermissionEntity
import com.ike.specificationdemo.entity.RoleEntity
import com.ike.specificationdemo.entity.UserEntity
import com.ike.specificationdemo.repository.*
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 28/6/2025
 */
@Service
class CommonService(
    private val userRepository: UserRepository,
    private val entityManager: EntityManager,
) {

    fun simpleFindAll(): Any {
        /*
        * Hibernate: select ue1_0.id,ue1_0.created_at,ue1_0.created_by,ue1_0.email,ue1_0.password,ue1_0.updated_at,ue1_0.username from cust_user ue1_0
        * 观察console可以看到hibernate生成的sql, 这里可以select的字段更少以提升性能
        */
        return userRepository.findAll()
            .map { SimpleUserDTO(it.id, it.username, it.password, it.email, it.createdAt, it.updatedAt) }
    }

    fun simpleFindAllOptimized(): Any {
        /*
        * Hibernate: select ue1_0.username,ue1_0.password,ue1_0.email,ue1_0.updated_at,ue1_0.created_at,ue1_0.id from cust_user ue1_0
        * 观察console可以看到hibernate生成的sql可以看出此时已经是最优解了
        */
        return userRepository.findSimpleUserProjectionBy()
            // 同样可以将返回值映射为 List<SimpleUserDTO>
            .map {
                SimpleUserDTO(
                    it.getId(),
                    it.getUsername(),
                    it.getPassword(),
                    it.getEmail(),
                    it.getCreatedAt(),
                    it.getUpdatedAt()
                )
            }
    }

    fun complexQuery(): Any {
        /*
         * 假设这次需要根据传入的角色名和权限名动态查询关联的用户
         */
        val (roleNameMock, permissionNameMock) = pair()

        val spec = Specification<UserEntity> { root, _, cb ->
            val userRoleJoin = root.join<UserEntity, RoleEntity>("roles", JoinType.LEFT)
            val rolePermissionJoin = userRoleJoin.join<RoleEntity, PermissionEntity>("permissions", JoinType.LEFT)
            val predicates = mutableListOf<Predicate>()
            if (roleNameMock.isNotBlank()) {
                predicates.add(cb.like(userRoleJoin["name"], "%$roleNameMock%"))
            }
            if (permissionNameMock.isNotBlank()) {
                predicates.add(cb.like(rolePermissionJoin["name"], "%$permissionNameMock%"))
            }
            cb.and(*predicates.toTypedArray())
        }

        /*
         * Hibernate: select ue1_0.id,ue1_0.created_at,ue1_0.created_by,ue1_0.email,ue1_0.password,r2_0.user_id,r2_1.id,r2_1.created_at,r2_1.created_by,r2_1.name,r2_1.path,r2_1.updated_at,ue1_0.updated_at,ue1_0.username
         * from cust_user ue1_0
         * left join cust_user_role r1_0 on ue1_0.id=r1_0.user_id
         * left join cust_role r1_1 on r1_1.id=r1_0.role_id
         * left join cust_role_permission p1_0 on r1_1.id=p1_0.cust_role_id
         * left join cust_permission p1_1 on p1_1.id=p1_0.cust_permission_id
         * left join cust_user_role r2_0 on ue1_0.id=r2_0.user_id
         * left join cust_role r2_1 on r2_1.id=r2_0.role_id
         * where r1_1.name like ? escape '' and p1_1.name like ? escape ''
         *
         * 观察hibernate输出的sql可以得知, 仍然查询了一些非必要的字段, 这里的接口投影无法让hibernate优化sql(本质上还是findAll(specification))
         * 这种情况下, 只能依赖criteria api优化查询
         */
        return userRepository.findBy<UserEntity, List<UserProjection>>(spec) { p ->
            p
                .project("id", "username", "email", "roles.name", "roles.path")
                .`as`(UserProjection::class.java)
                .all()
        }
    }

    fun complexQueryOptimized(): Any {
        /*
         * 假设这次需要根据传入的角色名和权限名动态查询关联的用户
         */
        val (roleNameMock, permissionNameMock) = pair()

        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(UserFlatDTO::class.java)
        val root = cq.from(UserEntity::class.java)
        val userRoleJoin = root.join<UserEntity, RoleEntity>("roles", JoinType.LEFT)
        val rolePermissionJoin = userRoleJoin.join<RoleEntity, PermissionEntity>("permissions", JoinType.LEFT)
        cq.select(
            cb.construct(
                UserFlatDTO::class.java,
                root.get<Long>("id"),
                root.get<String>("username"),
                root.get<String>("password"),
                root.get<Instant>("createdAt"),
                root.get<Instant>("updatedAt"),
                userRoleJoin.get<String>("name"),
                userRoleJoin.get<String>("path")
            )
        )
        cq.where(
            cb.like(
                userRoleJoin.get("name"), "%$roleNameMock%"
            ),
            cb.like(rolePermissionJoin.get("name"), "%$permissionNameMock%")
        )

        /*
         * Hibernate: select ue1_0.id,ue1_0.username,ue1_0.password,ue1_0.created_at,ue1_0.updated_at,r1_1.name,r1_1.path
         * from cust_user ue1_0
         * left join cust_user_role r1_0 on ue1_0.id=r1_0.user_id
         * left join cust_role r1_1 on r1_1.id=r1_0.role_id
         * left join cust_role_permission p1_0 on r1_1.id=p1_0.cust_role_id
         * left join cust_permission p1_1 on p1_1.id=p1_0.cust_permission_id
         * where r1_1.name like ? escape '' and p1_1.name like ? escape ''
         * 优化完成, 这次并未查询所有字段
         */

        return entityManager.createQuery(cq).resultList   // 笛卡尔积, 手动转换为需要的结构
            .groupBy { it.id }
            .map { (id, group) ->
                val first = group.first()
                UserDTO(
                    id,
                    first.username,
                    first.password,
                    first.createdAt,
                    first.updatedAt,
                    group.mapNotNull { it.roleName }.distinct(),
                    group.mapNotNull { it.rolePath }.distinct()
                )
            }
    }

    private fun pair(): Pair<String, String> {
        val roleNameMock = arrayOf("first", "second", "third", "").random()
        val permissionNameMock = arrayOf("", "first", "second", "third", "").random()
        println("ROLE_NAME= $roleNameMock, PERMISSION_NAME= $permissionNameMock")
        return Pair(roleNameMock, permissionNameMock)
    }

}