package de.hsrm.mi.stacs.pepjekt.controller

import de.hsrm.mi.stacs.pepjekt.services.OrderService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/order")
class OrderController {

    @Autowired
    val orderService: OrderService? = null

    val logger: Logger = LoggerFactory.getLogger(OrderController::class.java)

    @PostMapping("/buy")
    fun putOrder(
        @RequestParam accountId: String,
        @RequestParam stock: String,
        @RequestParam amount: BigDecimal,
        @RequestParam time: String
        ): Mono<String> {
        // orderService.placeBuyOrder();
        val date = LocalDate.parse(time)
        val dateTime = date.atStartOfDay()

        logger.info("Request: Buy Order $amount $stock at $dateTime")

        return orderService!!.placeBuyOrder(accountId, stock, amount, dateTime)
            .map {
                // Wenn die Bestellung erfolgreich war, wird "Klappt" zurückgegeben.
                "Klappt"
            }
            .onErrorReturn("Klappt nicht")
    }

}