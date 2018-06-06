package org.skuatracing.springbench

import io.jaegertracing.Tracer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicLong

data class MeowResponse(val meows: Long);

@RestController
class MeowController {
    val meows = AtomicLong()

    @RequestMapping("/")
    fun index(): MeowResponse {
        return MeowResponse(meows.incrementAndGet())
    }
}

@SpringBootApplication
class SpringBenchApplication {
    @Bean
    fun tracer(): io.opentracing.Tracer {
        return Tracer.Builder("spring-bench").build()
    }
}

fun main(args: Array<String>) {
    runApplication<SpringBenchApplication>(*args)
}
