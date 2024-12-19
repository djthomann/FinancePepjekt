package de.hsrm.mi.stacs.pepjekt.router

import de.hsrm.mi.stacs.pepjekt.handler.BankAccountHandler
import de.hsrm.mi.stacs.pepjekt.handler.InvestmentAccountHandler
import de.hsrm.mi.stacs.pepjekt.handler.OrderHandler
import de.hsrm.mi.stacs.pepjekt.handler.StockHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfig {

    /*
    - eigene bank account balance anzeigen                  x
    - anzeige eigener wertpapiere + werte                  x
    - wertpapier aktuelle werte nach symbol / name                  x
    - finde wertpapier nach symbol / name                  x
    - aktueller wertpapier wert                  x
    - tagestief + tageshoch wertpapier                  x
    - neues wertpapiert kaufen (anzahl, zeitpunkt, summe)/ verkaufen (wertpapier symbol)                  x
     */

    @Bean
    fun mainRouter(bankAccountHandler: BankAccountHandler, investmentAccountHandler: InvestmentAccountHandler, stockHandler: StockHandler, orderHandler: OrderHandler) = router {
        add(bankAccountRouter(bankAccountHandler))
        add(investmentAccountRouter(investmentAccountHandler))
        add(stockRouter(stockHandler))
        add(orderRouter(orderHandler))
    }

    @Bean
    fun bankAccountRouter(bankAccountHandler: BankAccountHandler) = router {
        "/api".nest {
            GET("/get/balance", bankAccountHandler::getBalance)
        }
    }

    @Bean
    fun investmentAccountRouter(investmentAccountHandler: InvestmentAccountHandler) = router {
        "/api".nest {
            GET("/get/portfolio", investmentAccountHandler::getPortfolio)
        }
    }

    @Bean
    fun stockRouter(stockHandler: StockHandler) = router {
        "/api".nest {
            GET("/get/stock-details/by/symbol", stockHandler::getStockDetailsBySymbol)
            GET("/get/stock-details/by/name", stockHandler::getStockDetailsByName)
            GET("/get/stock/by/symbol", stockHandler::getStockBySymbol)
            GET("/get/stock/by/name", stockHandler::getStockByName)
            GET("/get/current/stock/value", stockHandler::getCurrentStockValue) // by stock symbol
            GET("get/stock/day-low", stockHandler::getStockDayLow)
            GET("get/stock/day-high", stockHandler::getStockDayHigh)
            GET("get/stock/history/by-symbol", stockHandler::getStockHistoryBySymbol)
            GET("get/stock/history/by-name", stockHandler::getStockHistoryByName)
        }
    }

    @Bean
    fun orderRouter(orderHandler: OrderHandler) = router {
        "/api".nest {
            POST("post/buy/stock", orderHandler::postBuyStock)
            POST("post/sell/stock", orderHandler::postSellStock)
        }
    }
}