package org.skuatracing.springbench

import io.jaegertracing.Tracer
import io.jaegertracing.samplers.ProbabilisticSampler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.util.*
import java.util.concurrent.ThreadLocalRandom

//data class MeowResponse(val meows: Long);
//
//@RestController
//class MeowController {
//    val meows = AtomicLong()
//
//    @RequestMapping("/")
//    fun index(): MeowResponse {
//        return MeowResponse(meows.incrementAndGet())
//    }
//}
//


@Controller
@EnableAutoConfiguration
class HelloController {


    @Autowired
    internal var jdbcTemplate: JdbcTemplate? = null


    @RequestMapping("/plaintext")
    @ResponseBody
    internal fun plaintext(): String {
        return "Hello, World!"
    }


    @RequestMapping("/json")
    @ResponseBody
    internal fun json(): Map<String, String> {
        return mapOf("message" to "Hello, World!")
    }


    @RequestMapping("/db")
    @ResponseBody
    internal fun db(): World {
        return randomWorld()
    }


    @RequestMapping("/queries")
    @ResponseBody
    internal fun queries(@RequestParam queries: String): Array<World?> {
        val worlds = arrayOfNulls<World>(parseQueryCount(queries))
        Arrays.setAll<World>(worlds) { i -> randomWorld() }
        return worlds
    }


    @RequestMapping("/updates")
    @ResponseBody
    internal fun updates(@RequestParam queries: String): Array<World?> {
        val worlds = arrayOfNulls<World>(parseQueryCount(queries))
        Arrays.setAll<World>(worlds) { i -> randomWorld() }
        for (world in worlds) {
            world!!.randomNumber = randomWorldNumber()
            jdbcTemplate!!.update(
                    "UPDATE world SET randomnumber = ? WHERE id = ?",
                    world.randomNumber,
                    world.id)
        }
        return worlds
    }


    @RequestMapping("/fortunes")
    @ModelAttribute("fortunes")
    internal fun fortunes(): List<Fortune?> {
        val fortunes = jdbcTemplate!!.query<Fortune>(
                "SELECT * FROM fortune"
        ) { rs, rn -> Fortune(rs.getInt("id"), rs.getString("message")) }


        fortunes.add(Fortune(0, "Additional fortune added at request time."))
        fortunes.sortBy { it.message }
//        fortunes.sort(comparing { fortune -> fortune.message })
        return fortunes
    }


    private fun randomWorld(): World {
        return jdbcTemplate!!.queryForObject<World>(
                "SELECT * FROM world WHERE id = ?",
                RowMapper { rs, rowNum -> World(rs.getInt("id"), rs.getInt("randomnumber")) },
                randomWorldNumber())!!
    }


    class Fortune(var id: Int, var message: String)


    class World(var id: Int, var randomNumber: Int)

    companion object {


//        @JvmStatic
//        fun main(args: Array<String>) {
//            SpringApplication.run(HelloController::class.java, *args)
//        }


        private fun randomWorldNumber(): Int {
            return 1 + ThreadLocalRandom.current().nextInt(10000)
        }


        private fun parseQueryCount(textValue: String?): Int {
            if (textValue == null) {
                return 1
            }
            val parsedValue: Int
            try {
                parsedValue = Integer.parseInt(textValue)
            } catch (e: NumberFormatException) {
                return 1
            }

            return Math.min(500, Math.max(1, parsedValue))
        }
    }
}

@SpringBootApplication
class SpringBenchApplication {
    @Bean
    fun tracer(): io.opentracing.Tracer {
        return Tracer.Builder("spring-bench").withSampler(ProbabilisticSampler(0.001)).build()
    }
}

fun main(args: Array<String>) {
    runApplication<SpringBenchApplication>(*args)
}
