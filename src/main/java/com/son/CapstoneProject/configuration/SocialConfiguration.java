package com.son.CapstoneProject.configuration;

import com.son.CapstoneProject.repository.loginRepository.AppUserDAO;
import com.son.CapstoneProject.social.ConnectionSignUpImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.security.AuthenticationNameUserIdSource;

import javax.sql.DataSource;

@Configuration
@EnableSocial
@PropertySource("classpath:social-cfg.properties") // Load to addConnectionFactories(Environment env)
public class SocialConfiguration implements SocialConfigurer {

    private boolean autoSignUp = false;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AppUserDAO appUserDAO;

    // @env: read from social-cfg.properties file.
    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer cfConfig, Environment env) {

        try {
            this.autoSignUp = Boolean.parseBoolean(env.getProperty("social.auto-signup"));
        } catch (Exception e) {
            this.autoSignUp = false;
        }

        // Facebook
        FacebookConnectionFactory ffactory = new FacebookConnectionFactory(//
                env.getProperty("facebook.app.id"), //
                env.getProperty("facebook.app.secret"));

        ffactory.setScope(env.getProperty("facebook.scope"));

        // Google
        GoogleConnectionFactory gfactory = new GoogleConnectionFactory(//
                env.getProperty("google.client.id"), //
                env.getProperty("google.client.secret"));

        gfactory.setScope(env.getProperty("google.scope"));

        cfConfig.addConnectionFactory(ffactory);
        cfConfig.addConnectionFactory(gfactory);
    }

    // Strategy interface used to determine the account ID of the current user.
    @Override
    public UserIdSource getUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

    // UsersConnectionRepository is a singleton data store for connections across all users
    // When restoring connections, it will use the ConnectionFactoryLocator to locate ConnectionFactory instances.
    // TextEncryptor to encrypt credentials when persisting connections
    @Override
    public UsersConnectionRepository getUsersConnectionRepository(
            ConnectionFactoryLocator connectionFactoryLocator) {

        JdbcUsersConnectionRepository usersConnectionRepository =
                new JdbcUsersConnectionRepository(
                        dataSource,
                        connectionFactoryLocator,
                        Encryptors.noOpText());

        // After logging in to social networking. Automatically creates corresponding APP_USER
        // if it does not exist. To enable implicit sign up, you must create an implementation of
        // the ConnectionSignUp interface and inject an instance of that
        // ConnectionSignUp to the connection repository.
        if (autoSignUp) {
            ConnectionSignUp connectionSignUp = new ConnectionSignUpImpl(appUserDAO);
            usersConnectionRepository.setConnectionSignUp(connectionSignUp);
        } else {
            // After logging in to social networking.
            // If the corresponding APP_USER record is not found.
            // Navigate to registration page.
            usersConnectionRepository.setConnectionSignUp(null);
        }
        return usersConnectionRepository;
    }

    // This bean manages the connection flow between the account provider and the example application.
    /**
     * ConnectController will obtain the appropriate OAuth operations interface from one
     * of the provider connection factories registered with ConnectionFactoryRegistry.
     * It will select a specific ConnectionFactory to use by matching the connection factory's ID
     * with the URL path. The path pattern that ConnectController handles is "/connect/{providerId}"
     * . Therefore, if ConnectController is handling a request for "/connect/twitter", then the
     * ConnectionFactory whose getProviderId() returns "twitter" will be used. (As configured in the
     * previous section, TwitterConnectionFactory will be chosen.)
     */
    @Bean
    public ConnectController connectController(ConnectionFactoryLocator connectionFactoryLocator,
                                               ConnectionRepository connectionRepository) {
        ConnectController connectController = new
                ConnectController(connectionFactoryLocator, connectionRepository);
        connectController.setApplicationUrl("/userInfo");
        return connectController;
    }

}