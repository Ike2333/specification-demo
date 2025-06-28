package com.ike.specificationdemo

import com.ike.specificationdemo.entity.PermissionEntity
import com.ike.specificationdemo.entity.RoleEntity
import com.ike.specificationdemo.entity.UserEntity
import com.ike.specificationdemo.repository.PermissionRepository
import com.ike.specificationdemo.repository.RoleRepository
import com.ike.specificationdemo.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class SpecificationDemoApplication

fun main(args: Array<String>) {
    runApplication<SpecificationDemoApplication>(*args)
}

@Configuration
class DataInitializer(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
) {

    // 写入一些模拟数据
    @Bean
    fun initSomeMockData(): CommandLineRunner = CommandLineRunner { _ ->
        arrayOf("zeroth", "first", "second", "third", "forth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth", "eleven", "twelfth").forEach {
            PermissionEntity().apply {
                this.name = "test_permission_${it}"
                this.path = "test::${it}"
            }.let {entity ->  permissionRepository.save(entity) }

            RoleEntity().apply {
                this.name = "test_role_${it}"
                this.path = "test::${it}"
                this.permissions = permissionRepository.findAll()
            }.let { entity -> roleRepository.save(entity) }

            UserEntity().apply {
                this.username = "username_${it}"
                this.email = "${it}@example.com"
                this.password = "${it}${it}${it}"
                this.roles = roleRepository.findAll()
            }.let { entity ->  userRepository.save(entity) }
        }
    }
}