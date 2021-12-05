package guru.springframework.sfgrestbrewery;

import io.netty.util.internal.StringUtil;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import javax.swing.*;

@SpringBootApplication
public class SfgRestfulBreweryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfgRestfulBreweryApplication.class, args);
    }

    @Value("classpath:/schema.sql")
    Resource resource;

    //Spring boot will provide connetion factory since H2  is in the class path
    @Bean
    ConnectionFactoryInitializer initializer (ConnectionFactory connectionFactory){
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(resource));

        return initializer;
    }


}
