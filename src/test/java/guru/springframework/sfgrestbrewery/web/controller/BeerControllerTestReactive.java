package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.config.DatabaseConfig;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import io.r2dbc.spi.ConnectionFactory;
import org.h2.engine.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest({BeerController.class, DatabaseConfig.class})
class BeerControllerTestReactive {


    @Autowired
    WebTestClient webTestClient;

    @MockBean
    BeerService beerService;

    BeerDto validBeerDto;
    @BeforeEach
    void setUp() {

        validBeerDto= BeerDto.builder().beerName("TEST_BEER").beerStyle("PALE_ALE").price(new BigDecimal("10.99")).upc(BeerLoader.BEER_1_UPC).build();
    }

    @Test
    void getBeerById() {

        Integer beerId= 1;
        given(beerService.getById(any(),any())).willReturn(Mono.just(validBeerDto));
        webTestClient.get().uri("/api/v1/beer/"+ beerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(),equalTo(validBeerDto.getBeerName()));
    }

    @Test
    void getBeerByUPC() {
        given(beerService.getByUpc(any())).willReturn(Mono.just(validBeerDto));

        webTestClient.get()
                .uri("/api/v1/beerUpc/" + validBeerDto.getUpc())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(validBeerDto.getBeerName()));
    }

    @Test
    void listBeers() {
        List<BeerDto> beerList = Arrays.asList(validBeerDto);

        BeerPagedList beerPagedList = new BeerPagedList(beerList, PageRequest.of(1,1), beerList.size());

        given(beerService.listBeers(any(), any(), any(), any())).willReturn(Mono.just(beerPagedList));

        webTestClient.get()
                .uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerPagedList.class);
    }
}