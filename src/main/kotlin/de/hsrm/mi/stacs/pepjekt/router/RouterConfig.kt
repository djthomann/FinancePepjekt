package de.hsrm.mi.stacs.pepjekt.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class RouterConfig {

    /*
    - anzeige eigener wertpapiere + werte
    - wertpapier aktuelle werte nach symbol / name
    - finde wertpapier nach symbol / name
    - aktueller wertpapier wert
    - tagestief wertpapier
    - tageshoch wertpapier
    - neues wertpapiert kaufen (anzahl, zeitpunkt, summe)/ verkaufen (wertpapier symbol)
     */

    @Bean
    fun routerFunction() = router {

    }
}