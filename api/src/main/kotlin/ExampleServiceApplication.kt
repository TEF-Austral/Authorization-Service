package api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["api", "auth"])
class ExampleServiceApplication

fun main(args: Array<String>) {
    runApplication<ExampleServiceApplication>(*args)
}
