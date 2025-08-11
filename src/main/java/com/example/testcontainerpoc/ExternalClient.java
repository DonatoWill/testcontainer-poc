package com.example.testcontainerpoc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(name = "externalClient", url = "${external.api.url}")
public interface ExternalClient {

    @GetMapping("/external-api/data")
    String getExternalData();

}
