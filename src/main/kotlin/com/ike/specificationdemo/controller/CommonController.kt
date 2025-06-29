package com.ike.specificationdemo.controller

import com.ike.specificationdemo.service.CommonService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 28/6/2025
 */
@RequestMapping("/test")
@RestController
class CommonController(
    private val commonService: CommonService
) {

    @GetMapping("/sfa")
    fun simpleFindAll(): Any{
        return commonService.simpleFindAll()
    }

    @GetMapping("/sfao")
    fun simpleFindAllOpt(): Any{
        return commonService.simpleFindAllOptimized()
    }

    @GetMapping("/cq")
    fun complexQuery(): Any{
        return commonService.complexQuery()
    }



    @GetMapping("/ssq")
    fun simpleSpecQuery(): Any{
        return commonService.simpleSpecQuery()
    }

    @GetMapping("/ssqo")
    fun simpleSpecQueryOptimized(): Any{
        return commonService.simpleSpecQueryOptimized()
    }


    @GetMapping("/fbrap")
    fun findByRoleAndPermissionName(): Any{
        return commonService.findByRoleAndPermissionName()
    }

    @GetMapping("/fbrapp")
    fun findByRoleAndPermissionNameUsingProjection(): Any{
        return commonService.findByRoleAndPermissionNameUsingProjection()
    }

    @GetMapping("/fbrapo")
    fun findByRoleAndPermissionNameOptimized(): Any{
        return commonService.findByRoleAndPermissionNameOptimized()

    }

}