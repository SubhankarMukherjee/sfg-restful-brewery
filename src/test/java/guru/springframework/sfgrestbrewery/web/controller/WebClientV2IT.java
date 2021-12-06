package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.CheckedOutputStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientV2IT {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String BEER_V2_PATH = "/api/v2/beer";
    private static final String BEER_V2_UPC_PATH = "/api/v2/beerUpc";

    WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder().baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .build();
    }

    @Test
    void getBeerById() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webClient.get().uri(BEER_V2_PATH + "/" + 1)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BeerDto.class)
                .subscribe(beerDto -> {
                    assertThat(beerDto).isNotNull();
                    assertThat(beerDto.getBeerName()).isNotNull();
                    countDownLatch.countDown();
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);

    }

    @Test
    void BeerByIdNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webClient.get().uri(BEER_V2_PATH + "/" + 200)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BeerDto.class)
                .subscribe(beerDto -> {

                }, throwable -> {
                    countDownLatch.countDown();
                });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void getBeerByUpc() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webClient.get().uri(BEER_V2_UPC_PATH + "/" + BeerLoader.BEER_2_UPC)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BeerDto.class)
                .subscribe(beerDto -> {
                    assertThat(beerDto).isNotNull();
                    assertThat(beerDto.getBeerName()).isNotNull();
                    assertThat(beerDto.getUpc()).isEqualTo(BeerLoader.BEER_2_UPC);
                    countDownLatch.countDown();
                });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }


    @Test
    void BeerByUpcNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webClient.get().uri(BEER_V2_UPC_PATH + "/" + "12343545")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BeerDto.class)
                .subscribe(beerDto -> {

                }, throwable -> {
                    countDownLatch.countDown();
                });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testSaveBeer() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto beerDto = BeerDto.builder()
                .beerName("JTs Beer")
                .upc("1233455")
                .beerStyle("PALE_ALE")
                .price(new BigDecimal("8.99"))
                .build();

        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post().uri(BEER_V2_PATH)
                .accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(beerDto))
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).subscribe(responseEntity -> {

            assertThat(responseEntity.getStatusCode().is2xxSuccessful());

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testSaveBeerBadRequest() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto beerDto = BeerDto.builder()
                .price(new BigDecimal("8.99"))
                .build();

        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post().uri(BEER_V2_PATH)
                .accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(beerDto))
                .retrieve().toBodilessEntity();

        beerResponseMono.subscribe(responseEntity -> {

        }, throwable -> {
            if (throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest")) {
                WebClientResponseException ex = (WebClientResponseException) throwable;

                if (ex.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                    countDownLatch.countDown();
                }
            }
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void updateBeer() throws InterruptedException {
        final Integer beerID = 1;
        final String beerName = "Foster";
        CountDownLatch countDownLatch = new CountDownLatch(2);
        webClient.put().uri(BEER_V2_PATH + "/" + beerID)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(BeerDto.builder()
                        .beerName(beerName)
                        .beerStyle("IPA")
                        .upc("123456789")
                        .price(new BigDecimal("10.99"))
                        .build()

                )).retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {
                    assertThat(responseEntity.getStatusCode().is2xxSuccessful());
                    countDownLatch.countDown();
                });
        // Wait for Update Thread to finish

        countDownLatch.await(500, TimeUnit.MILLISECONDS);
        // Get the beer and check if it is same as update or not

        webClient.get().uri(BEER_V2_PATH + "/" + beerID)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class)
                .subscribe(beerDto -> {
                    assertThat(beerDto).isNotNull();
                    assertThat(beerDto.getBeerName()).isNotNull();
                    assertThat(beerDto.getBeerName()).isEqualTo(beerName);

                    countDownLatch.countDown();
                });
        //Wait for get thread to finish

        countDownLatch.await(500, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);

    }

    @Test
    void updateBeerNotFound() throws InterruptedException {
        final Integer beerID = 999;
        final String beerName = "Foster";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webClient.put().uri(BEER_V2_PATH + "/" + beerID)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(BeerDto.builder()
                        .beerName(beerName)
                        .beerStyle("IPA")
                        .upc("123456789")
                        .price(new BigDecimal("10.99"))
                        .build()

                )).retrieve().toBodilessEntity()
                .subscribe(responseEntity -> {
                    assertThat(responseEntity.getStatusCode().is2xxSuccessful());

                }, throwable -> {
                    countDownLatch.countDown();
                });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void deleteBeerById() {
        Integer beerId = 3;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webClient.delete().uri(BEER_V2_PATH + "/" + beerId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity()
                .flatMap(responseEntity -> {
                    countDownLatch.countDown();
                   return webClient.get().uri(BEER_V2_PATH + "/" + beerId)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve().bodyToMono(BeerDto.class);
                }).subscribe(beerDto -> {

                }, throwable -> {
                    countDownLatch.countDown();
                });

    }

    @Test
    void deleteBeerNotFound() {
        Integer beerId=4;

        webClient.delete().uri(BEER_V2_PATH+"/"+beerId)
                .retrieve().toBodilessEntity().block();
        // do same operation to again to generate exception
        assertThrows(WebClientResponseException.NotFound.class,()->{
            webClient.delete().uri(BEER_V2_PATH+"/"+beerId)
                    .retrieve().toBodilessEntity().block();
        });
    }


}

