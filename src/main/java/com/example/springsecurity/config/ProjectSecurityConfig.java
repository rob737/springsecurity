package com.example.springsecurity.config;

import com.example.springsecurity.filter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class ProjectSecurityConfig extends WebSecurityConfigurerAdapter {

    /* REQUIREMENT
     *  /myAccount - Secured
     *  /myBalance - Secured
     *  /myLoans - Secured
     *  /myCards - Secured
     *  /notices - Not Secured
     *  /contact - Not Secured
     *  */

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS). // This is to prevent storing JSessionId in browser's cache
                and().
                cors().configurationSource(new CorsConfigurationSource() {
                                               @Override
                                               public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                                   CorsConfiguration config = new CorsConfiguration();
                                                   config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                                                   config.setAllowedMethods(Collections.singletonList("*"));
                                                   config.setAllowCredentials(true);
                                                   config.setAllowedHeaders(Collections.singletonList("*"));
                                                   config.setExposedHeaders(Arrays.asList("Authorization"));
                                                   config.setMaxAge(3600L);
                                                   return config;
                                               }
                                           }
        ).and().csrf().disable(). // ignoringAntMatchers("/contact").csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class).
                addFilterAfter(new AuthoritiesLoggingAfterFilter(), BasicAuthenticationFilter.class).
                addFilterBefore(new JWTTokenValidatorFilter(), BasicAuthenticationFilter.class).
                addFilterAfter(new JWTTokenGeneratorFilter(), BasicAuthenticationFilter.class).
                addFilterAt(new AuthoritiesLoggingAtFilter(), BasicAuthenticationFilter.class).
                authorizeRequests()
                .mvcMatchers("/myAccount").hasRole("USER")
                .antMatchers("/myBalance").hasAnyRole("USER", "ADMIN")
                .antMatchers("/myLoans").authenticated()
                .antMatchers("/myCards").authenticated()
                .antMatchers("/notices").permitAll()
                .antMatchers("/contact").permitAll();

        http.formLogin().and()
                .httpBasic();
    }

    /*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password("12345").authorities("admin")
                .and()
                .withUser("user").password("12345").authorities("read")
                .and()
                .passwordEncoder(NoOpPasswordEncoder.getInstance());
    }*/

    /**
     * It is mandatory to declare Password Encoder instance
     **/
    /*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        UserDetails userAdmin = User.withUsername("admin").password("12345").authorities("admin").build();
        UserDetails userGeneral = User.withUsername("user").password("12345").authorities("read").build();

        userDetailsService.createUser(userAdmin);
        userDetailsService.createUser(userGeneral);

        auth.userDetailsService(userDetailsService);
    }*/

    /*@Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }*/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
