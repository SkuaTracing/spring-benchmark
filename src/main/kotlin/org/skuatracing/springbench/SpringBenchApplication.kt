package org.skuatracing.springbench

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
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
class SpringBenchApplication

fun main(args: Array<String>) {
    runApplication<SpringBenchApplication>(*args)
}
