package com.lureb.client.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class IndexController {
    @GetMapping("/")
    public String index(final Model model) {
        return "index";
    }
}
