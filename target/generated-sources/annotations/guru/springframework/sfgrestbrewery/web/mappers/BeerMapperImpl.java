package guru.springframework.sfgrestbrewery.web.mappers;

import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.domain.Beer.BeerBuilder;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerDto.BeerDtoBuilder;
import guru.springframework.sfgrestbrewery.web.model.v2.BeerStyleEnum;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-12-06T02:14:47+0530",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 16.0.1 (Oracle Corporation)"
)
@Component
public class BeerMapperImpl implements BeerMapper {

    @Override
    public BeerDto beerToBeerDto(Beer beer) {
        if ( beer == null ) {
            return null;
        }

        BeerDtoBuilder beerDto = BeerDto.builder();

        beerDto.id( beer.getId() );
        beerDto.beerName( beer.getBeerName() );
        if ( beer.getBeerStyle() != null ) {
            beerDto.beerStyle( beer.getBeerStyle().name() );
        }
        beerDto.upc( beer.getUpc() );
        beerDto.price( beer.getPrice() );
        beerDto.createdDate( beer.getCreatedDate() );

        return beerDto.build();
    }

    @Override
    public BeerDto beerToBeerDtoWithInventory(Beer beer) {
        if ( beer == null ) {
            return null;
        }

        BeerDtoBuilder beerDto = BeerDto.builder();

        beerDto.id( beer.getId() );
        beerDto.beerName( beer.getBeerName() );
        if ( beer.getBeerStyle() != null ) {
            beerDto.beerStyle( beer.getBeerStyle().name() );
        }
        beerDto.upc( beer.getUpc() );
        beerDto.price( beer.getPrice() );
        beerDto.quantityOnHand( beer.getQuantityOnHand() );
        beerDto.createdDate( beer.getCreatedDate() );

        return beerDto.build();
    }

    @Override
    public Beer beerDtoToBeer(BeerDto dto) {
        if ( dto == null ) {
            return null;
        }

        BeerBuilder beer = Beer.builder();

        beer.id( dto.getId() );
        beer.beerName( dto.getBeerName() );
        if ( dto.getBeerStyle() != null ) {
            beer.beerStyle( Enum.valueOf( BeerStyleEnum.class, dto.getBeerStyle() ) );
        }
        beer.upc( dto.getUpc() );
        beer.quantityOnHand( dto.getQuantityOnHand() );
        beer.price( dto.getPrice() );
        beer.createdDate( dto.getCreatedDate() );

        return beer.build();
    }
}
