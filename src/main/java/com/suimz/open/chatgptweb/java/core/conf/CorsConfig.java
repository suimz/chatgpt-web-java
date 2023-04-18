package com.suimz.open.chatgptweb.java.core.conf;

import com.suimz.open.chatgptweb.java.core.properties.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * HTTP CORS Configuration
 *
 * @author https://github.com/suimz
 */
@Configuration
public class CorsConfig {
	@Bean
	@ConditionalOnProperty(name = "app.cors-allowed-origin")
	public FilterRegistrationBean corsFilter(AppProperties properties) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setMaxAge(3600L);
		config.addAllowedOriginPattern(properties.getCorsAllowedOrigin());

		// Apply all path
		UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
		configSource.registerCorsConfiguration("/**", config);

		FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(configSource));
		bean.setOrder(0);
		return bean;
	}
	
}
