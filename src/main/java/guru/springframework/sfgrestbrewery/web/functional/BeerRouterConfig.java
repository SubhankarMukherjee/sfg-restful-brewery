package guru.springframework.sfgrestbrewery.web.functional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BeerRouterConfig {

    private static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;

    @Bean
    public RouterFunction<ServerResponse> beerRouterV2(BeerHandlerV2 handler) {
        return route().GET("/api/v2/beer/{beerId}",
                        accept(APPLICATION_JSON), handler::getBeerById)
                .GET("/api/v2/beerUpc/{upc}",
                        accept(APPLICATION_JSON), handler::getBeerByUpc)
                .POST("/api/v2/beer",accept(MediaType.APPLICATION_JSON),handler::saveNewBeer)
                .PUT("/api/v2/beer/{beerId}",accept(APPLICATION_JSON),handler::updateBeer)
                .DELETE("/api/v2/beer/{beerId}",accept(APPLICATION_JSON),handler::deleteById)
                .build();

    }


}
