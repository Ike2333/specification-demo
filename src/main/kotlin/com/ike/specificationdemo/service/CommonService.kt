package com.ike.specificationdemo.service

import com.ike.specificationdemo.entity.PermissionEntity
import com.ike.specificationdemo.entity.RoleEntity
import com.ike.specificationdemo.entity.UserEntity
import com.ike.specificationdemo.repository.*
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.persistence.criteria.Subquery
import org.springframework.data.domain.*
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        * 在3.x版本中, 接口投影已经可以被直接替换为数据类了
        */
        return userRepository.findSimpleUserDTOBy()
    }

    fun complexQuery(): Any {
        /*
         * 假设这次需要根据传入的角色名和权限名动态查询关联的用户
         */
        val (roleNameMock, permissionNameMock) = genKeywordsPair()

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
         * 另外, 当需要映射包含层级关系的对象时, 必须用接口投影而不是class
         */
        return userRepository.findBy<UserEntity, List<UserProjection>>(spec) {
            it
                .project("id", "username", "email", "roles.name", "roles.path")
                .`as`(UserProjection::class.java)
                .all()
        }
    }


    private fun genKeywordsPair(): Pair<String, String> {
        val roleNameMock = arrayOf("first", "second", "third", "", "test", "").random()
        val permissionNameMock = arrayOf("", "first", "second", "third", "", "test").random()
        println("ROLE_NAME= $roleNameMock, PERMISSION_NAME= $permissionNameMock")
        return Pair(roleNameMock, permissionNameMock)
    }


    /**
     * 带有动态条件+分页的findAll()
     * 观察生成的sql可以看出, 始终都查询了cust_user表所有字段
     *
     * Hibernate: select ue1_0.id,ue1_0.created_at,ue1_0.created_by,ue1_0.email,ue1_0.password,ue1_0.updated_at,ue1_0.username
     * from cust_user ue1_0
     * where ue1_0.username like ? escape '' and ue1_0.email like ? escape '' order by ue1_0.updated_at desc,ue1_0.id desc
     * offset ? rows fetch first ? rows only
     *
     *  @see simpleSpecQueryOptimized()
     */
    fun simpleSpecQuery(): Page<Any> {
        val (spec, pageable) = specification()
        return userRepository.findAll(spec, pageable)
            .map { SimplerUserDTO(it.id, it.username, it.email) }
    }


    /**
     * 优化后只查询了需要的字段:
     * Hibernate: select ue1_0.id,ue1_0.username,ue1_0.email
     * from cust_user ue1_0
     * where ue1_0.username like ? escape '' and ue1_0.email like ? escape '' order by ue1_0.updated_at desc,1 desc
     * offset ? rows fetch first ? rows only
     */
    fun simpleSpecQueryOptimized(): Any {
        val (spec, pageable) = specification()
        return userRepository.findBy<UserEntity, Page<SimplerUserDTO>>(spec) { p ->
            p
                .project("id", "username", "email")  // 非必须
                .`as`(SimplerUserDTO::class.java)                   // 投影或DTO均可
                .page(pageable)
        }
    }


    // 组装查询条件和分页参数
    private fun specification(): Pair<Specification<UserEntity>, Pageable> {
        val usernameOrEmpty = arrayOf("username", "first", "second", "").random()
        val emailOrEmpty = arrayOf("example", "").random()
        println("USERNAME= $usernameOrEmpty, EMAIL= $emailOrEmpty")
        val spec = Specification<UserEntity> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            if (usernameOrEmpty.isNotBlank()) {
                predicates.add(cb.like(root["username"], "%$usernameOrEmpty%"))
            }
            if (emailOrEmpty.isNotBlank()) {
                predicates.add(cb.like(root["email"], "%$emailOrEmpty%"))
            }
            cb.and(*predicates.toTypedArray())
        }
        val pageable = PageRequest.of(0, 10, Sort.by("updatedAt", "id").descending())

        return spec to pageable
    }




    // 根据角色名称模糊匹配用户
    fun hasRoleWithFuzzyRoleName(inputRoleName: String): Specification<UserEntity> =
        Specification<UserEntity> { root, _, cb ->
            val urj = root.join<UserEntity, RoleEntity>("roles")
            cb.like(urj["name"], "%$inputRoleName%")
        }

    // 根据权限名称模糊匹配用户
    fun hasPermissionWithFuzzyPermissionName(inputPermissionName: String): Specification<UserEntity> =
        Specification<UserEntity> { root, _, cb ->
            val urj = root.join<UserEntity, RoleEntity>("roles")
            val rpj = urj.join<RoleEntity, PermissionEntity>("permissions")
            cb.like(rpj["name"], "%$inputPermissionName%")
        }


    /**
     * 这是一个反面示例
     * 这样可以查询出数据, 通过观察控制台hibernate输出的sql可以看到是N+1查询
     */
    @Transactional
    fun findByRoleAndPermissionName(): Any {
        val roleName = "first"
        val permissionName = "first"
        val pageable = PageRequest.of(0, 5)

        println("$roleName---------$permissionName")

        val spec = Specification<UserEntity> { root, query, cb ->
            val urj = root.join<UserEntity, RoleEntity>("roles", JoinType.LEFT)
            val rpj = urj.join<RoleEntity, PermissionEntity>("permissions", JoinType.LEFT)
            val predicates = mutableListOf<Predicate>()
            if (roleName.isNotBlank()) {
                predicates.add(cb.like(urj["name"], "%$roleName%"))
            }
            if (permissionName.isNotBlank()) {
                predicates.add(cb.like(rpj["name"], "%$permissionName%"))
            }

            cb.and(*predicates.toTypedArray())
        }

        return userRepository.findAll(spec, pageable).map {
            it.roles?.let { it1 ->
                UserDTO(
                    it.id,
                    it.username,
                    it.password,
                    it.createdAt,
                    it.updatedAt,
                    it1.map { x -> x.name }.toSet(),
                    it1.map { x -> x.path }.toSet()
                )
            }
        }
    }

    /**
     * 这并不是最佳的推荐的查询方式, 但是写起来相对简单且 *看似合理* 的方式,
     * 在数据量大时将会出现严重性能问题(hibernate会尝试再内存中进行分页):
     * HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory
     * 如需确保性能(criteria api优化后的版本), 请参考下面的方法
     * @see findByRoleAndPermissionNameOptimized
     */
    fun findByRoleAndPermissionNameUsingProjection(): Any {
        val roleName = arrayOf("second", "first", "").random()
        val permissionName = arrayOf("second", "first", "").random()

        println("$roleName---------$permissionName")
        val pageable = PageRequest.of(0, 5)
        val spec = Specification.allOf(
            hasRoleWithFuzzyRoleName(roleName),
            hasPermissionWithFuzzyPermissionName(permissionName)
        )

        // 被投影的对象如果只包含User实体中的属性, 一般是合理且没有性能问题的; 但如果包含了映射的对象属性, hibernate将会select *
        return userRepository.findBy<UserEntity, Page<UserEntityInfo>>(spec) {
            it.project(
                "id",
                "username",
                "password",
                "createdAt",
                "updatedAt",
                "roles.name",
                "roles.path",
            )
                .`as`(UserEntityInfo::class.java)
                .page(pageable)
        }
    }


    /**
     * 这种情况可以使用criteria api进行优化
     * 唯一的缺点是代码冗长
     * 可以是用QueryDSL简化这个流程, 但是需要引入额外的依赖和复杂度, 如果业务对性能要求不是很高, 用这个方法就能够简单实现相同的功能
     * @see findByRoleAndPermissionName
     */
    fun findByRoleAndPermissionNameOptimized(): Page<UserDTO> {
        val roleNameQuery = "first"
        val permissionNameQuery = "first"
        val pageable = PageRequest.of(0, 5)

        println("$roleNameQuery---------$permissionNameQuery")

        val cb = entityManager.criteriaBuilder
        // 此子查询将识别结果应包含哪些用户
        val userIdsSubquery: Subquery<Long> = cb.createQuery(UserEntity::class.java).subquery(Long::class.java)
        val subqueryRoot: Root<UserEntity> = userIdsSubquery.from(UserEntity::class.java)
        // 使用内连接查询具有角色和符合条件的权限的用户
        val subqueryUserRoleJoin = subqueryRoot.join<UserEntity, RoleEntity>("roles", JoinType.INNER)
        val subqueryRolePermissionJoin =
            subqueryUserRoleJoin.join<RoleEntity, PermissionEntity>("permissions", JoinType.INNER)

        val subqueryPredicates = mutableListOf<Predicate>()
        if (roleNameQuery.isNotBlank()) {
            subqueryPredicates.add(cb.like(subqueryUserRoleJoin.get("name"), "%$roleNameQuery%"))
        }
        if (permissionNameQuery.isNotBlank()) {
            subqueryPredicates.add(cb.like(subqueryRolePermissionJoin.get("name"), "%$permissionNameQuery%"))
        }

        userIdsSubquery.select(subqueryRoot.get("id")).where(*subqueryPredicates.toTypedArray()).distinct(true)


        // 针对即将查询的用户, 获取其角色和权限进行过滤
        val mainQuery = cb.createQuery(UserFlatDTO::class.java)
        val mainRoot = mainQuery.from(UserEntity::class.java)
        val mainUserRoleJoin = mainRoot.join<UserEntity, RoleEntity>("roles", JoinType.LEFT)

        mainQuery.select(
            cb.construct(
                UserFlatDTO::class.java,
                mainRoot.get<Long>("id"),
                mainRoot.get<String>("username"),
                mainRoot.get<String>("password"),
                mainRoot.get<Instant>("createdAt"),
                mainRoot.get<Instant>("updatedAt"),
                mainUserRoleJoin.get<String>("name"),
                mainUserRoleJoin.get<String>("path"),
            )
        )

        // 使用子查询中的ID过滤主查询
        mainQuery.where(mainRoot.get<Long>("id").`in`(userIdsSubquery))

        // 针对主查询实体(UserEntity)排序
        if (pageable.sort.isSorted) {
            val orders = pageable.sort.map { order ->
                if (order.isAscending) cb.asc(mainRoot.get<Long>("id")) else cb.desc(mainRoot.get<Long>("id"))
            }.toList()
            mainQuery.orderBy(orders)
        }

        // UserFlatDTO分页查询
        val flatTypedQuery = entityManager.createQuery(mainQuery)
        flatTypedQuery.firstResult = pageable.offset.toInt()
        flatTypedQuery.maxResults = pageable.pageSize

        val flatResults: List<UserFlatDTO> = flatTypedQuery.resultList

        // 将UserFlatDTO聚合为userDTO
        val userDtos = flatResults
            .groupBy { it.id } // 以user id分组
            .map { (userId, group) ->
                val firstFlatDto = group.first()
                UserDTO(
                    userId,
                    firstFlatDto.username,
                    firstFlatDto.password,
                    firstFlatDto.createdAt,
                    firstFlatDto.updatedAt,
                    group.mapNotNull { it.roleName }.toSet(),
                    group.mapNotNull { it.rolePath }.toSet()
                )
            }

        // 获取命中查询条件的总用户数
        val countCq = cb.createQuery(Long::class.java)
        val countRoot = countCq.from(UserEntity::class.java)
        countCq.select(cb.countDistinct(countRoot.get<Long>("id")))
        countCq.where(countRoot.get<Long>("id").`in`(userIdsSubquery))
        val totalElements = entityManager.createQuery(countCq).singleResult

        return PageImpl(userDtos, pageable, totalElements)
    }
}




