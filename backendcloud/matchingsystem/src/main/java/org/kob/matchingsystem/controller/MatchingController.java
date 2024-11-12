package org.kob.matchingsystem.controller;

import org.kob.matchingsystem.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class MatchingController {
    @Autowired
    private MatchingService matchingService;

    @PostMapping("/player/add/")
    public String addPlayer(@RequestParam MultiValueMap<String,String> data){ // MutiValueMap 是一个key对应一个list（value）
        Integer userId = Integer.valueOf(Objects.requireNonNull(data.getFirst("userId")));
        Integer rating = Integer.valueOf(Objects.requireNonNull(data.getFirst("rating")));

        return matchingService.addPlayer(userId, rating);
    }
    @PostMapping("/player/remove/")
    public String removePlayer(@RequestParam MultiValueMap<String,String> data){
        Integer userId = Integer.valueOf(Objects.requireNonNull(data.getFirst("userId")));

        return matchingService.removePlayer(userId);
    }
}
