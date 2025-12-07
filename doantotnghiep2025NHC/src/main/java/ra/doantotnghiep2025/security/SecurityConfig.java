package ra.doantotnghiep2025.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Đừng quên import HttpMethod
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import ra.doantotnghiep2025.security.jwt.CustomAccessDeniedHandler;
import ra.doantotnghiep2025.security.jwt.JwtAuthTokenFilter;
import ra.doantotnghiep2025.security.jwt.JwtEntryPoint;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private JwtAuthTokenFilter jwtAuthTokenFilter;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private JwtEntryPoint jwtEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("Configuring Spring Security...");
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setExposedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> {
                    // 1. CÁC API CÔNG KHAI HOÀN TOÀN (Không cần login)
                    auth.requestMatchers(
                            "/api/v1/auth/**",
                            "/api/v1/account/forgot-password",
                            "/api/v1/account/reset-password",
                            "/api/v1/user/cart/checkout/success",
                            "/api/v1/user/cart/checkout/cancel",
                            "/api/paypal/**",
                            "/uploads/**",
                            "/images/**"
                    ).permitAll();

                    // 2. [QUAN TRỌNG] CÁC API CHO PHÉP KHÁCH XEM (CHỈ GET)
                    // Đây là đoạn code sửa lỗi 401 của bạn
                    auth.requestMatchers(HttpMethod.GET,
                            "/api/v1/brands",
                            "/api/v1/brands/**",
                            "/api/v1/categories",
                            "/api/v1/categories/**",
                            "/api/v1/products",
                            "/api/v1/products/**",
                            "/api/v1/products/search",
                            "/api/v1/comments/product/**"
                    ).permitAll();

                    // 3. CÁC API CẦN ĐĂNG NHẬP
                    auth.requestMatchers("/api/v1/auth/logout").authenticated();
                    auth.requestMatchers("/api/v1/user/**").hasAuthority("USER");
                    auth.requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN");

                    auth.anyRequest().authenticated();
                })
                .sessionManagement(auth -> auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(auth -> auth
                        .authenticationEntryPoint(jwtEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}