package com.son.CapstoneProject.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class CommonTest {

    public static RestTemplate restTemplate = new RestTemplate();

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public static HttpHeaders getHeaders(String method, String frontEndUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Request-Method", method);
        headers.add("Origin", frontEndUrl);
        headers.add("Content-Type", "application/json;charset=UTF-8");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public static String readStringFromFile(String filePath) {
        String contents = null;
        try {
            contents = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    public static String createURL(int port, String path) {
        return "http://localhost:" + port + path;
    }
}
