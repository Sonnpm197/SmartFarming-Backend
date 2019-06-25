package com.son.CapstoneProject.configuration;

import com.son.CapstoneProject.entity.login.AppRole;
import com.son.CapstoneProject.handler.CustomAuthenticationFailureHandler;
import com.son.CapstoneProject.handler.MyCustomLoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.social.security.SpringSocialConfigurer;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    DataSource dataSource;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    // This bean is load the user specific data when form login is used.
    @Override
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return new MyCustomLoginSuccessHandler("/userInfo");
    }

    // Enable jdbc authentication
    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select user_name, encryted_password"
                        + " from app_user where user_name=?");
    }

    private SpringSocialConfigurer getSpringSocialConfigurer() {
        SpringSocialConfigurer config = new SpringSocialConfigurer();
        config.alwaysUsePostLoginUrl(true);
        config.postLoginUrl("/userInfo");
        return config;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // CSRF protection is enabled by default with Java Configuration. If you would
        // like to disable CSRF, the corresponding Java configuration can be seen below.
        // Refer to the Javadoc of csrf() for additional customizations in how CSRF protection is configured.
        http.csrf().disable();

        // Pages do not require login
        http.authorizeRequests()
                .antMatchers("/", "/signup", "/login", "/logout")
                .permitAll();

//         For USER only
        http.authorizeRequests()
                .antMatchers("/user/**")
                .access("hasRole('" + AppRole.ROLE_USER + "')");

        // For ADMIN only.
        http.authorizeRequests()
                .antMatchers("/admin/**")
                .access("hasRole('" + AppRole.ROLE_ADMIN + "')");

        // When the user has logged in as XX.
        // But access a page that requires role YY,
        // AccessDeniedException will be thrown.
        http.authorizeRequests()
                .and()
                .exceptionHandling()
                .accessDeniedPage("/403");

        // Form Login config
        http.authorizeRequests()
                .and()
                .formLogin()
//                .loginProcessingUrl("/j_spring_security_check") // the url to submit the username and password to
                .loginPage("/login") // the custom login page
                .successForwardUrl("/userInfo") // the landing page after a successful login
                .failureUrl("/signin?param.error=bad_credentials") // the landing page after an unsuccessful login
                .failureHandler(new CustomAuthenticationFailureHandler());

//                .usernameParameter("username")
//                .passwordParameter("password")
//                .successHandler(successHandler())
//                .permitAll();

        // Logout Config
        http.authorizeRequests()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logoutSuccessful");

        // Customize post redirect Url and sign up page if no accounts presented in Userconnection
        http.apply(getSpringSocialConfigurer()).signupUrl("/signup");

    }

}