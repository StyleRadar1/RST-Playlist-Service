package edu.dosw.styleradar.playlist.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "edu.dosw.styleradar.playlist.infrastructure.adapter.out.external")
public class FeignConfig {
}
