package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Favorite
import de.hsrm.mi.stacs.pepjekt.repositories.IFavoriteRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service class to handle operations related to managing favorite stocks.
 * Provides methods to add, remove, check, and retrieve favorites associated with investment accounts.
 *
 * @property operator The [TransactionalOperator] for managing reactive transactions.
 * @property favoriteRepository Repository interface for accessing favorite entities.
 * @property stockService Service for stock-related operations.
 * @property investmentAccountService Service for investment account operations.
 */
@Service
class FavoriteService(
    val operator: TransactionalOperator, // injected by spring
    val favoriteRepository: IFavoriteRepository,
    val stockService: IStockService,
    val investmentAccountService: IInvestmentAccountService,
) : IFavoriteService {

    private val logger = LoggerFactory.getLogger(FavoriteService::class.java)

    /**
     * Retrieves all favorites associated with a specific investment account.
     *
     * @param investmentAccountId The ID of the investment account.
     * @return A [Flux] emitting all [Favorite] entities for the investment account.
     */
    override fun getFavoritesOfInvestmentAccount(investmentAccountId: Long): Flux<Favorite> {
        logger.debug("Fetching all favorites for investment account ID: $investmentAccountId")
        return favoriteRepository.findAllByInvestmentAccountId(investmentAccountId)
    }

    /**
     * Adds a favorite stock to a specific investment account.
     *
     * @param investmentAccountId The ID of the investment account.
     * @param stockSymbol The symbol of the stock to be added as a favorite.
     * @return A [Mono] indicating completion or emitting an error if the operation fails.
     * @throws NoSuchElementException If the stock or investment account does not exist.
     * @throws IllegalStateException If the favorite already exists.
     */
    override fun addFavoriteToInvestmentAccount(investmentAccountId: Long, stockSymbol: String): Mono<Void> {
        logger.debug("Adding favorite stock $stockSymbol to investment account ID: $investmentAccountId")

        return stockService.getStockBySymbol(stockSymbol)
            .switchIfEmpty(Mono.error(NoSuchElementException("Stock not found by stockSymbol $stockSymbol")))
            .flatMap { investmentAccountService.getInvestmentAccount(investmentAccountId) }
            .switchIfEmpty(Mono.error(NoSuchElementException("Investment account not found by investmentAccountId $investmentAccountId")))
            .flatMap {
                favoriteRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
                    .flatMap {
                        Mono.error<Void>(IllegalStateException("Favorite already exists for this investment account and stock"))
                    }
                    .switchIfEmpty(
                        favoriteRepository.save(
                            Favorite(
                                investmentAccountId = investmentAccountId,
                                stockSymbol = stockSymbol
                            )
                        )
                            .`as`(operator::transactional)
                            .then()
                    )
            }
    }

    /**
     * Removes a favorite stock from a specific investment account.
     *
     * @param investmentAccountId The ID of the investment account.
     * @param stockSymbol The symbol of the stock to be removed from favorites.
     * @return A [Mono] indicating completion or emitting an error if the operation fails.
     * @throws NoSuchElementException If the favorite does not exist.
     */
    override fun removeFavoriteFromInvestmentAccount(investmentAccountId: Long, stockSymbol: String): Mono<Void> {
        logger.debug("Removing favorite stock $stockSymbol from investment account ID: $investmentAccountId")

        return favoriteRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
            .switchIfEmpty(Mono.error(NoSuchElementException("Favorite not found by stock symbol $stockSymbol and investmentAccountId $investmentAccountId")))
            .flatMap { favoriteRepository.removeByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol) }
            .then()
    }

    /**
     * Checks if a stock is marked as a favorite for a specific investment account.
     *
     * @param investmentAccountId The ID of the investment account.
     * @param stockSymbol The symbol of the stock to check.
     * @return A [Mono] emitting `true` if the stock is a favorite, or `false` otherwise.
     */
    override fun isFavorite(investmentAccountId: Long, stockSymbol: String): Mono<Boolean> {
        logger.debug("Checking if stock $stockSymbol is a favorite for investment account ID: $investmentAccountId")

        return favoriteRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
            .map { true }
            .defaultIfEmpty(false)
    }
}
