package com.bezkoder.springjwt.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bezkoder.springjwt.security.jwt.AuthEntryPointJwt;
import com.bezkoder.springjwt.security.jwt.AuthTokenFilter;
import com.bezkoder.springjwt.security.services.UserDetailsServiceImpl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
// (securedEnabled = true,
// jsr250Enabled = true,
// prePostEnabled = true) // by default
public class WebSecurityConfig { // extends WebSecurityConfigurerAdapter {
  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

//  @Override
//  public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
//    authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
//  }
  
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
       
      authProvider.setUserDetailsService(userDetailsService);
      authProvider.setPasswordEncoder(passwordEncoder());
   
      return authProvider;
  }

//  @Bean
//  @Override
//  public AuthenticationManager authenticationManagerBean() throws Exception {
//    return super.authenticationManagerBean();
//  }
  
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http.cors().and().csrf().disable()
//      .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
//      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//      .authorizeRequests().antMatchers("/api/auth/**").permitAll()
//      .antMatchers("/api/test/**").permitAll()
//      .anyRequest().authenticated();
//
//    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/api/auth/**","/api/trainings/**", "/api/employee/**", "/api/policies/**","/api/perks/**","/api/expenses/**").permitAll()  // Authentification ouverte
                            .requestMatchers("/api/test/all").permitAll()
                            .requestMatchers("/api/trainings/**").permitAll()
                            .requestMatchers("/api/expenses/**").permitAll()  // accès public pour tester

                            .requestMatchers("/api/entreesDeTemps/**").permitAll()

                            .requestMatchers("/api/employee/**").permitAll()  // accès public pour tester
                            .requestMatchers("/api/policies/**").permitAll()   // accès public pour tester
                           .requestMatchers("/api/employee/department/**").permitAll()
                            .requestMatchers("/api/perks/**").permitAll()
                            .requestMatchers("/api/job-positions/**").authenticated()
                            .requestMatchers("/api/auth/admin/signup").hasRole("ADMIN")  // Seulement admin peut accéder aux endpoints sous /admin
                            .requestMatchers("/api/test/test-email").permitAll()
                           .requestMatchers("/api/employee/employee").hasAnyRole("ADMIN", "HR")  // Ajout d'un employé accessible à ADMIN et RH
                            .requestMatchers("/api/employee/{id}").hasAnyRole("ADMIN", "HR") // Toute autre requête sur employee nécessite une auth
                            .requestMatchers("/api/employee/all/**").authenticated() // Toute autre requête sur employee nécessite une auth
                            .requestMatchers("/admin/employees/edit-employee").hasAnyRole("ADMIN", "HR")  // Ajout d'un employé accessible à ADMIN et RH

                            .requestMatchers("/api/employees").hasAnyRole("ADMIN", "HR")  // Ajout d'un employé accessible à ADMIN et RH

                            .requestMatchers("/api/Department").hasAnyRole("ADMIN", "HR")  // Ajout d'un employé accessible à ADMIN et RH
                            .requestMatchers("/api/Contract").hasAnyRole("ADMIN", "HR")  // Ajout d'un employé accessible à ADMIN et RH
                            .requestMatchers("/api/trainings/with-participants").permitAll()
                            .requestMatchers("/api/payrolls/**").permitAll()




                            .anyRequest().authenticated()
            );

    http.authenticationProvider(authenticationProvider());
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }


  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*")); // utilisation de patterns
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}
