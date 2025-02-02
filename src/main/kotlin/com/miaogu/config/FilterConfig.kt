package com.miaogu.config

import com.miaogu.filter.ExceptionFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

@Configuration
class FilterConfig {

    @Bean
    fun exceptionFilter(): FilterRegistrationBean<ExceptionFilter> {
        val filterRegistrationBean = FilterRegistrationBean(ExceptionFilter())
        filterRegistrationBean.order = Ordered.HIGHEST_PRECEDENCE
        filterRegistrationBean.addUrlPatterns("/*")
        return filterRegistrationBean
    }
}
