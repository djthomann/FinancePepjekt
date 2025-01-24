package de.hsrm.mi.stacs.pepjekt.router

import de.hsrm.mi.stacs.pepjekt.handler.*
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

    fun mainRouter(
        bankAccountHandler: BankAccountHandler,
        investmentAccountHandler: InvestmentAccountHandler,
        stockHandler: StockHandler,
        orderHandler: OrderHandler,
        cryptoHandler: CryptoHandler,
        metalHandler: MetalHandler,
        favoriteHandler: FavoriteHandler)
    = router {
        add(bankAccountRouter(bankAccountHandler))
        add(investmentAccountRouter(investmentAccountHandler))
        add(stockRouter(stockHandler))
        add(orderRouter(orderHandler))
        add(cryptoRouter(cryptoHandler))
        add(metalRouter(metalHandler))
        add(favoriteRouter(favoriteHandler))
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
            GET("/balance", bankAccountHandler::getBalance)
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
            GET("/portfolio", investmentAccountHandler::getPortfolio)
            GET("/portfolio/totalValue", investmentAccountHandler::getPortfolioTotalValue)
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
            GET("/stocks", stockHandler::getStocks)
            GET("/stock-details/symbol", stockHandler::getStockDetailsBySymbol)
            GET("/stock/by/symbol", stockHandler::getStockBySymbol)
            //GET("/stock/by/name", stockHandler::getStockByName)
            GET("/stock/current-value", stockHandler::getCurrentStockValue) // by stock symbol
            GET("/stock/day-low", stockHandler::getStockDayLow)
            GET("/stock/day-high", stockHandler::getStockDayHigh)
            GET("/stock/history/symbol", stockHandler::getStockHistoryBySymbol)
            GET("/stock/average-price", stockHandler::getStockAveragePrice)
        }
    }

    /**
     * Defines the router for crypto related functionalities
     *
     * @param cryptoHandler Handler for crypto operations
     * @return Router with Crypto Endpoints
     */
    @Bean
    fun cryptoRouter(cryptoHandler: CryptoHandler) = router {
        "/api".nest {
            GET("/crypto") { request ->
                when {
                    request.queryParam("symbol").isPresent -> cryptoHandler.getCryptoBySymbol(request)
                    else -> cryptoHandler.getCryptos(request)
                }
            }
        }
    }

    /**
     * Defines the router for metal related functionalities
     *
     * @param metalHandler Handler for metal operations
     * @return Router with Metal Endpoints
     */
    @Bean
    fun metalRouter(metalHandler: MetalHandler) = router {
        "/api".nest {
            GET("/metal") { request ->
                when {
                    request.queryParam("symbol").isPresent -> metalHandler.getMetalBySymbol(request)
                    else -> metalHandler.getMetals(request)
                }
            }
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
            POST("/placeBuyOrder", orderHandler::placeBuyOrder)
            POST("/placeSellOrder", orderHandler::placeSellOrder)
            GET("/orders", orderHandler::getOrders)
        }
    }

    /**
     * Defines the routes for saving and editing favorite stocks for an investment account.
     *
     * @param favoriteHandler Handler for favorite stock operations.
     * @return Router with favorite endpoints.
     */
    fun favoriteRouter(favoriteHandler: FavoriteHandler) = router {
        "/api".nest {
            GET("/favorites", favoriteHandler::getFavorites)
            POST("/add-favorites", favoriteHandler::addFavorites)
            POST("/delete-favorites", favoriteHandler::deleteFavorites)
        }
    }
}