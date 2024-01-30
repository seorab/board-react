package com.example.board.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class HomeController {

	@GetMapping({"/", "/home"})
	public String index() {
		return "index";
	}
}
