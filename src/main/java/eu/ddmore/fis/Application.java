/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;


import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@EnableAutoConfiguration
@Import(RepositoryRestMvcConfiguration.class)
@ImportResource("classpath:META-INF/application-context.xml")
public class Application {
        
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        if(args.length>0) {
            if("PRINT_BEANS".equalsIgnoreCase(args[0])) {
                printBeans(ctx);
            }
        }
    }
    private static void printBeans(ApplicationContext context) {
        System.out.println("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

} 