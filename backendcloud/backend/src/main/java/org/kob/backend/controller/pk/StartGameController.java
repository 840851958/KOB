package org.kob.backend.controller.pk;

import org.kob.backend.service.pk.StartGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class StartGameController {
    @Autowired
    private StartGameService startGameService;

    @PostMapping("/pk/start/game/")
    public String startGame(@RequestParam MultiValueMap<String,String> data){
        Integer aId = Integer.valueOf(Objects.requireNonNull(data.getFirst("a_id")));
        Integer bId = Integer.valueOf(Objects.requireNonNull(data.getFirst("b_id")));
        return startGameService.startGame(aId, bId);
    }
}
