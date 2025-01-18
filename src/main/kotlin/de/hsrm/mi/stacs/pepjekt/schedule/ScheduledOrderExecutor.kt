package de.hsrm.mi.stacs.pepjekt.schedule

import de.hsrm.mi.stacs.pepjekt.services.IOrderService
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDateTime

@Component
class ScheduledOrderExecutor(val orderService: IOrderService) {

    val log: Logger = LoggerFactory.getLogger(ScheduledOrderExecutor::class.java)

    @PostConstruct
    fun scheduleOrderExecution() {
        Flux.interval(Duration.ofSeconds(5))
            .flatMap {
                log.info("Processing order...")

                orderService.processOrders()
                    .doOnTerminate { log.info("Finished processing orders.") }
                    .doOnError { error -> log.error("Error in processOrders: ${error.message}", error) }
            }
            .subscribe(
                {
                    log.info("Orders processed successfully at ${LocalDateTime.now()}")
                },
                { error ->
                    log.error("Error processing orders: ${error.message}")
                }
            )
    }

}
