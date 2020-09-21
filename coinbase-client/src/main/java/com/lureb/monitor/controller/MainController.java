package com.lureb.monitor.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class MainController {

  @GetMapping("/")
  public String index(final Model model) {
    return "index";
  }

  @GetMapping(path = "/sse/profiles", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Mono<>
}
