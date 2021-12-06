package guru.springframework.sfgrestbrewery.web.functional;

import guru.springframework.sfgrestbrewery.repositories.BeerRepository;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.controller.NotFoundException;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;



@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandlerV2 {
    private final BeerService beerService;
    private final Validator validator;

    public Mono<ServerResponse> getBeerById(ServerRequest request)
    {
        Integer beerId = Integer.valueOf(request.pathVariable("beerId"));
        Boolean showInventory= Boolean.valueOf(request.queryParam("showInventory").orElse("false"));
        return beerService.getById(beerId,showInventory)
                .flatMap(beerDto -> {
                   return ServerResponse.ok().bodyValue(beerDto);
                }).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getBeerByUpc(ServerRequest request)
    {
        String upc = request.pathVariable("upc");
        return beerService.getByUpc(upc)
                .flatMap(beerDto -> {
                    return ServerResponse.ok().bodyValue(beerDto);
                }).switchIfEmpty(ServerResponse.notFound().build());
    }
    public Mono<ServerResponse> updateBeer(ServerRequest request){
        return request.bodyToMono(BeerDto.class).doOnNext(this::validate)
                .flatMap(beerDto -> {
                   return beerService.updateBeer(Integer.valueOf(request.pathVariable("beerId")),beerDto);
                }).flatMap(updatedBeer->{
                    if(updatedBeer.getId()!=null) {
                        log.debug("beer Updated" + updatedBeer.getId());
                        return ServerResponse.noContent().build();
                    }
                    else
                    {
                        log.debug("beer is not updated becuase beer not found in find method");
                        return ServerResponse.notFound().build();
                    }
                });

    }

    public Mono<ServerResponse> saveNewBeer(ServerRequest request){
        Mono<BeerDto> beerDtoMono = request.bodyToMono(BeerDto.class).doOnNext(this::validate);

        return beerService.saveNewBeerMono(beerDtoMono)
                .flatMap(beerDto -> {
                    return ServerResponse.ok()
                            .header("location", "/api/v2/beer" + "/" + beerDto.getId())
                            .build();
                });
    }

    //Validation Step since MVC @Validated is not there in reactive

    private void validate(BeerDto beerDto)
    {
        Errors errors = new BeanPropertyBindingResult(beerDto,"beerDto");
        validator.validate(beerDto, errors);
        if(errors.hasErrors())
        {
            throw new ServerWebInputException(errors.toString());
        }

    }

    public Mono<ServerResponse> deleteById(ServerRequest request) {
        return beerService.reactiveDeleteById(Integer.valueOf(request.pathVariable("beerId")))
                .flatMap(voidMono->{
                    return ServerResponse.ok().build();
                }).onErrorResume(e-> e instanceof NotFoundException, e-> ServerResponse.notFound().build());
    }
}
