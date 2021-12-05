package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jt on 3/7/21.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientIT {

    public static final String BASE_URL = "http://localhost:8080";

    WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .build();
    }

    @Test
    void testListBeers() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerPagedList.class);


//        BeerPagedList pagedList = beerPagedListMono.block();
//        pagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));
        beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

            beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    void testListBeersByName() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get().
                uri(uriBuilder -> uriBuilder.path("/api/v1/beer")
                        .queryParam("beerName", "Mango Bobs").build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerPagedList.class);


//        BeerPagedList pagedList = beerPagedListMono.block();
//        pagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));
        beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

            beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testListBeersPageSize5() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get().uri(uriBuilder -> {
                    return uriBuilder.path("/api/v1/beer").queryParam("pageSize", "5").build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerPagedList.class);

        beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

            beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void getBeerById() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri("/api/v1/beer/1")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beer -> {
            assertThat(beer).isNotNull();
            assertThat(beer.getBeerName()).isNotNull();
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void getBeerByUPC() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri("api/v1/beerUpc/" + BeerLoader.BEER_2_UPC)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beer -> {
            assertThat(beer).isNotNull();
            assertThat(beer.getBeerName()).isNotNull();

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void createBeerBadRequest() throws InterruptedException {
        BeerDto beerDto = BeerDto.builder().price(new BigDecimal("10.99")).build();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<ResponseEntity<Void>> responseEntityMono = webClient.post().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(beerDto))
                .retrieve()
                .toBodilessEntity();

        responseEntityMono.publishOn(Schedulers.parallel()).doOnError(throwable -> {
                    countDownLatch.countDown();
                })
                .subscribe(responseEntity -> {

                });

        countDownLatch.await(1000,TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);


    }

    @Test
    void createBeerRequest() throws InterruptedException {
        BeerDto validBeerDto= BeerDto.builder().beerName("TEST_BEER").beerStyle("PALE_ALE").price(new BigDecimal("10.99")).upc(BeerLoader.BEER_1_UPC).build();;
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<ResponseEntity<Void>> responseEntityMono = webClient.post().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(validBeerDto))
                .retrieve()
                .toBodilessEntity();

        responseEntityMono.publishOn(Schedulers.parallel()).subscribe(responeEnttity->{
            assertThat(responeEnttity.getStatusCode().is2xxSuccessful());
            countDownLatch.countDown();
        });

        countDownLatch.await(1000,TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);


    }
    @Test
    void updateBeerRequest() throws InterruptedException {
        AtomicReference atomicReference = new AtomicReference();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri("/api/v1/beer/1")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {

            atomicReference.set(beerDto);

            countDownLatch.countDown();
        });

        countDownLatch.await(1000,TimeUnit.MILLISECONDS);

        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        BeerDto beerDto = (BeerDto) atomicReference.get();

        BeerDto validBeerDtoToBeUpdated= BeerDto.builder().beerName("KingFisher").beerStyle(beerDto.getBeerStyle()).price(beerDto.getPrice()).upc(beerDto.getUpc()).build();;


       webClient.put().uri("/api/v1/beer/"+ beerDto.getId())
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(validBeerDtoToBeUpdated))
                .retrieve()
                .toBodilessEntity()
                        .flatMap(responseEntity->{
                          countDownLatch.countDown();
                          return webClient.get().uri("/api/v1/beer/"+ beerDto.getId())
                                  .accept(MediaType.APPLICATION_JSON)
                                  .retrieve()
                                  .bodyToMono(BeerDto.class);
                        })
                                .subscribe(updatedBeerDto->{
                                    assertThat(updatedBeerDto.getBeerName()).isEqualTo("KingFisher");
                                    countDownLatch2.countDown();
                                });

        countDownLatch2.await(1000,TimeUnit.MILLISECONDS);
        assertThat(countDownLatch2.getCount()).isEqualTo(0);






    }

    @Test
    void updateBeerNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        BeerDto updateBeerPayload= BeerDto.builder().beerName("TEST_BEER").beerStyle("PALE_ALE").
                price(new BigDecimal("10.99")).upc(BeerLoader.BEER_1_UPC).build();

        webClient.put().uri("/api/v1/beer/"+ 200)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateBeerPayload))
                .retrieve()
                .toBodilessEntity()
               .subscribe(responseEntity-> {
               },throwable->{

                       if(throwable.getClass().getName().equals("org.springframework.web.reactive.function.client.WebClientResponseException$NotFound")){
                           WebClientResponseException ex= (WebClientResponseException) throwable;
                           if(ex.getStatusCode().equals(HttpStatus.NOT_FOUND))
                           {
                               countDownLatch.countDown();
                           }

                   }
               });
        countDownLatch.countDown();

        countDownLatch.await(1000,TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testDeleteBeer() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(3);

        webClient.get().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BeerPagedList.class)
                .publishOn(Schedulers.single())
                .subscribe(pagedList -> {
                    countDownLatch.countDown();

                    BeerDto beerDto = pagedList.getContent().get(0);

                    webClient.delete().uri("/api/v1/beer/" + beerDto.getId() )
                            .retrieve().toBodilessEntity()
                            .flatMap(responseEntity -> {
                                countDownLatch.countDown();

                                return webClient.get().uri("/api/v1/beer/" + beerDto.getId())
                                        .accept(MediaType.APPLICATION_JSON)
                                        .retrieve().bodyToMono(BeerDto.class);
                            }) .subscribe(savedDto -> {

                            }, throwable -> {
                                countDownLatch.countDown();
                            });
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

}
