package com.ike.specificationdemo.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.web.config.EnableSpringDataWebSupport

/**
 * 使用官方推荐的稳定Page序列化结构
 * @see org.springframework.data.web.PagedModel
 * @author <a href=mailto://idiotpre@outlook.com>IKE</a> 28/6/2025
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
class AppConfig{
    // TODO("你也可以在这里添加一些其他配置")
}
