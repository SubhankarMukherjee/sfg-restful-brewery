package guru.springframework.sfgrestbrewery.repositories;


import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.web.model.v2.BeerStyleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface BeerRepository extends ReactiveCrudRepository<Beer, Integer> {
//    Page<Beer> findAllByBeerName(String beerName, Pageable pageable);
//
//    Page<Beer> findAllByBeerStyle(BeerStyleEnum beerStyle, Pageable pageable);
//
//    Page<Beer> findAllByBeerNameAndBeerStyle(String beerName, BeerStyleEnum beerStyle, Pageable pageable);

    Mono<Beer> findByUpc(String upc);

    //these DO NOT WORK, but MAY in future
//    Flux<Page<Beer>> findAllByBeerName(String beerName, Pageable pageable);
//
//    Flux<Page<Beer>> findAllByBeerStyle(BeerStyleEnum beerStyle, Pageable pageable);
//
//    Flux<Page<Beer>> findAllByBeerNameAndBeerStyle(String beerName, BeerStyleEnum beerStyle, Pageable pageable);
//
//    Flux<Page<Beer>> findBeerBy(Pageable pageable);
}
