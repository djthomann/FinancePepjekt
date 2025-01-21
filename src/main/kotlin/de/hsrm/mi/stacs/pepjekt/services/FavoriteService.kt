package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Favorite
import de.hsrm.mi.stacs.pepjekt.repositories.IFavoriteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class FavoriteService(
    val operator: TransactionalOperator, // injected by spring
    val favoriteRepository: IFavoriteRepository,
    val stockService: IStockService,
    val investmentAccountService: IInvestmentAccountService,
) : IFavoriteService {

    override fun getFavoritesOfInvestmentAccount(investmentAccountId: Long): Flux<Favorite> {
        return favoriteRepository.findAllByInvestmentAccountId(investmentAccountId)
    }

    override fun addFavoriteToInvestmentAccount(investmentAccountId: Long, stockSymbol: String): Mono<Void> {
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

    override fun removeFavoriteFromInvestmentAccount(investmentAccountId: Long, stockSymbol: String): Mono<Void> {
        return favoriteRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
            .switchIfEmpty(Mono.error(NoSuchElementException("Favorite not found by stock symbol $stockSymbol and investmentAccountId $investmentAccountId")))
            .flatMap { favoriteRepository.removeByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol) }
            .then()
    }

    override fun isFavorite(investmentAccountId: Long, stockSymbol: String): Mono<Boolean> {
        return favoriteRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
            .map { true }
            .defaultIfEmpty(false)
    }



}
