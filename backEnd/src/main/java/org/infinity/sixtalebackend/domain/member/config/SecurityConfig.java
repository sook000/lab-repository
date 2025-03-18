package org.infinity.sixtalebackend.domain.member.config;

import lombok.AllArgsConstructor;
import org.infinity.sixtalebackend.domain.member.filter.JWTTokenFilter;
import org.infinity.sixtalebackend.global.filter.XFrameOptionsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
         http
//                 .csrf().disable()  // 필요에 따라 CSRF 비활성화
                 .addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter.class) // CORS 필터 추가
                 .addFilterBefore(new XFrameOptionsFilter(), CorsFilter.class)  // XFrameOptionsFilter 추가
                 .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/", "/members/auth/login", "/api/v1/**").permitAll()
                                .anyRequest().permitAll()
                )
                .addFilterBefore(new JWTTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .loginPage("/members/auth/login").permitAll()
                                .defaultSuccessUrl("/").permitAll()
                                .failureUrl("/").permitAll()
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/members/logout")
                                .logoutSuccessUrl("/")
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
//        config.addAllowedOrigin("https://i11d108.p.ssafy.io");
//        config.addAllowedOrigin("http://localhost:8083");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/api/v1/**", config);
        return new CorsFilter(source);
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
