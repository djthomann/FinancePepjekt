package de.hsrm.mi.stacs.pepjekt.schedule

import de.hsrm.mi.stacs.pepjekt.services.IOrderService
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDateTime

/**
 * The ScheduledOrderExecutor is responsible for periodically
 * triggering the execution of order processing tasks.
 */
@Component
class ScheduledOrderExecutor(val orderService: IOrderService) {

    val logger: Logger = LoggerFactory.getLogger(ScheduledOrderExecutor::class.java)

    /**
     * Schedules periodic execution of order processing tasks every 5 seconds.
     * Logs the progress, success, and any error encountered during order processing.
     * The process will continue even if there are errors, and these errors will be logged.
     */
    @PostConstruct
    fun scheduleOrderExecution() {
        Flux.interval(Duration.ofSeconds(5))
            .flatMap {
                logger.debug("Processing order...")

                orderService.processOrders()
                    .doOnTerminate { logger.debug("Finished processing orders.") }
                    .doOnError { error -> logger.error("Error in processOrders: ${error.message}", error) }
            }.onErrorContinue { error, _ ->
                logger.error("Error in scheduler: ${error.message}", error)
            }
            .subscribe(
                {
                    logger.debug("Orders processed successfully at {}", LocalDateTime.now())
                },
                { error ->
                    logger.error("Error processing orders: ${error.message}")
                }
            )
    }

}
