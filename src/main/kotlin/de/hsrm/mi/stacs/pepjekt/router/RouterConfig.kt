package de.hsrm.mi.stacs.pepjekt.router

import de.hsrm.mi.stacs.pepjekt.handler.BankAccountHandler
import de.hsrm.mi.stacs.pepjekt.handler.InvestmentAccountHandler
import de.hsrm.mi.stacs.pepjekt.handler.OrderHandler
import de.hsrm.mi.stacs.pepjekt.handler.StockHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

/**
 * Configuration class for defining the main routes of the application.
 * This class configures the routing of different API endpoints
 * for bank account, investment account, stock, and order functionalities.
 */
@Configuration
class RouterConfig {

    /**
     * Main router bean that configures all the endpoints related to bank account, investment account,
     * stock, and order services.
     *
     * @param bankAccountHandler Handler for bank account related operations.
     * @param investmentAccountHandler Handler for investment account related operations.
     * @param stockHandler Handler for stock related operations.
     * @param orderHandler Handler for order related operations.
     * @return Configured main router with all nested routes.
     */
    @Bean
    fun mainRouter(bankAccountHandler: BankAccountHandler, investmentAccountHandler: InvestmentAccountHandler, stockHandler: StockHandler, orderHandler: OrderHandler) = router {
        add(bankAccountRouter(bankAccountHandler))
        add(investmentAccountRouter(investmentAccountHandler))
        add(stockRouter(stockHandler))
        add(orderRouter(orderHandler))
    }

    /**
     * Defines the routes for bank account related functionalities.
     *
     * @param bankAccountHandler Handler for bank account operations.
     * @return Router with bank account endpoints.
     */
    @Bean
    fun bankAccountRouter(bankAccountHandler: BankAccountHandler) = router {
        "/api".nest {
            GET("/get/balance", bankAccountHandler::getBalance)
        }
    }

    /**
     * Defines the routes for investment account related functionalities.
     *
     * @param investmentAccountHandler Handler for investment account operations.
     * @return Router with investment account endpoints.
     */
    @Bean
    fun investmentAccountRouter(investmentAccountHandler: InvestmentAccountHandler) = router {
        "/api".nest {
            GET("/get/portfolio", investmentAccountHandler::getPortfolio)
            POST("/post/stock/buy", investmentAccountHandler::buyStock)
            POST("/post/stock/sell", investmentAccountHandler::sellStock)
        }
    }

    /**
     * Defines the routes for stock related functionalities.
     *
     * @param stockHandler Handler for stock operations.
     * @return Router with stock endpoints.
     */
    @Bean
    fun stockRouter(stockHandler: StockHandler) = router {
        "/api".nest {
            GET("/get/stock-details/by/symbol", stockHandler::getStockDetailsBySymbol)
            GET("/get/stock-details/by/name", stockHandler::getStockDetailsByName)
            GET("/get/stock/by/symbol", stockHandler::getStockBySymbol)
            GET("/get/stock/by/name", stockHandler::getStockByName)
            GET("/get/current/stock/value", stockHandler::getCurrentStockValue) // by stock symbol
            GET("/get/stock/day-low", stockHandler::getStockDayLow)
            GET("/get/stock/day-high", stockHandler::getStockDayHigh)
            GET("/get/stock/history/by-symbol", stockHandler::getStockHistoryBySymbol)
            GET("/get/stock/history/by-name", stockHandler::getStockHistoryByName)
            GET("/get/stock/average-price", stockHandler::getStockAveragePrice)
        }
    }

    /**
     * Defines the routes for order related functionalities such as placing buy, sell orders or getting all orders of an investment account.
     *
     * @param orderHandler Handler for order operations.
     * @return Router with order endpoints.
     */
    @Bean
    fun orderRouter(orderHandler: OrderHandler) = router {
        "/api".nest {
            POST("/post/buy/stock", orderHandler::postBuyStock)
            POST("/post/sell/stock", orderHandler::postSellStock)
            GET("/get/orders", orderHandler::getOrders)
        }
    }
}