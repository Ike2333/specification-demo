# 快速开始

这是一个使用Kotlin和Gradle构建的Spring Boot 3.X应用程序, 旨在演示基于Spring Data JPA规范的查询模式以及性能优化实践. 


## 先决条件
* JDK17及以上版本
* JPA/Hibernate基础知识
* 获取源码: `git clone https://github.com/Ike2333/specification-demo.git`


## 项目结构
```bash
│   .gitattributes
│   .gitignore
│   build.gradle.kts
│   gradlew
│   gradlew.bat
│   README.md
│   settings.gradle.kts
│
├───gradle
│   └───wrapper
│           gradle-wrapper.jar
│           gradle-wrapper.properties
│
└───src
    ├───main
    │   ├───kotlin
    │   │   └───com
    │   │       └───ike
    │   │           └───specificationdemo
    │   │               │   SpecificationDemoApplication.kt
    │   │               │
    │   │               ├───controller
    │   │               │       CommonController.kt
    │   │               │
    │   │               ├───entity
    │   │               │       BaseEntity.kt
    │   │               │       PermissionEntity.kt
    │   │               │       RoleEntity.kt
    │   │               │       UserEntity.kt
    │   │               │
    │   │               ├───repository
    │   │               │       dto.kt
    │   │               │       projections.kt
    │   │               │       repositories.kt
    │   │               │
    │   │               └───service
    │   │                       CommonService.kt  # 你只需要重点关注这个文件
    │   │
    │   └───resources
    │           application.yaml
    │
    └───test
        └───kotlin
            └───com
                └───ike
                    └───specificationdemo
                            SpecificationDemoApplicationTests.kt
```


## 其他
* 如果你使用本项目默认的h2数据库, 项目运行(`./gradlew bootRun`)后你可以通过 `http://localhost:8080/h2-console` 查看其内容
* 实际优化效果你可以通过postman等工具调用相关接口关注执行耗时进行对比