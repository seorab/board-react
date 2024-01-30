package com.example.board.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.board.interceptor.SignInCheckInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
  @Autowired
  private SignInCheckInterceptor signInCheckInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
      .addInterceptor(signInCheckInterceptor)
      .addPathPatterns("/board/**");
      // .addPathPatterns("/**").excludePathPatterns("/signin");
    WebMvcConfigurer.super.addInterceptors(registry);
  }
}