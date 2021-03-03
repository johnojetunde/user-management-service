package com.iddera.usermanagementservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private ResourceServerTokenServices tokenServices;

    @Value("${resource.id}")
    private String resourceId;

    public void configure(ResourceServerSecurityConfigurer configurer) {
        configurer.tokenServices(tokenServices);
        configurer.resourceId(resourceId);
    }

    public void configure(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity
//                .requestMatchers()
//                .and()
//                .authorizeRequests()
//                .antMatchers(
//                        "/actuator/**",
//                        "/v2/api-docs/**",
//                        "/swagger**",
//                        "/swagger-resources/**",
//                        "/webjars/**")
//                .permitAll()
//                .antMatchers(HttpMethod.POST,"/users","/companies")
//                .permitAll()
//                .antMatchers(HttpMethod.GET, "/security-questions/**")
//                .permitAll()
//                .anyRequest().authenticated();
        httpSecurity.authorizeRequests()
                .anyRequest()
                .permitAll()
                .and().csrf().disable();
    }
}
