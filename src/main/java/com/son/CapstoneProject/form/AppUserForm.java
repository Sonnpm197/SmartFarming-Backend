package com.son.CapstoneProject.form;

import lombok.*;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.api.Facebook;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CustomSocialUser {
    private String id;
    private String email;
    private String first_name;
    private String last_name;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AppUserForm {
    private String email;
    private String userName;

    private String firstName;
    private String lastName;
    private String password;
    private String role;
    private String signInProvider;
    private String providerUserId;

    // Fields for anonymous users
    private boolean anonymous;
    private String ipAddress;

    public AppUserForm(Connection<?> connection) {
        String provider = connection.getKey().getProviderId();

        // Fixing "(#12) bio field is deprecated for versions v2.8 and higher"
        // connection.fetchUserProfile() does not work with new facebook version
        if (provider != null && provider.equalsIgnoreCase("facebook")) {
            Facebook facebook = (Facebook) connection.getApi();

            // Facebook fields available
            // { "id", "about", "age_range", "birthday", "context", "cover",
            // "currency", "devices", "education", "email", "favorite_athletes",
            // "favorite_teams", "first_name", "gender", "hometown", "inspirational_people",
            // "installed", "install_type", "is_verified", "languages", "last_name", "link",
            // "locale", "location", "meeting_for", "middle_name", "name", "name_format",
            // "political", "quotes", "payment_pricepoints", "relationship_status", "religion",
            // "security_settings", "significant_other", "sports", "test_group", "timezone",
            // "third_party_id", "updated_time", "verified", "video_upload_limits", "viewer_can_send_gift",
            // "website", "work"}
            String[] fields = {"id", "email", "first_name", "last_name"};
            CustomSocialUser customSocialUser = facebook.fetchObject("me", CustomSocialUser.class, fields);

            this.email = customSocialUser.getEmail();
            this.userName = customSocialUser.getId();
            this.firstName = customSocialUser.getFirst_name();
            this.lastName = customSocialUser.getLast_name();
        }

        // Login with Google
        else if (provider != null && provider.equalsIgnoreCase("google")) {
            UserProfile socialUserProfile = connection.fetchUserProfile();
            this.email = socialUserProfile.getEmail();
            this.userName = socialUserProfile.getUsername();
            this.firstName = socialUserProfile.getFirstName();
            this.lastName = socialUserProfile.getLastName();
        }

        ConnectionKey key = connection.getKey();
        // google, facebook, twitter
        this.signInProvider = key.getProviderId();

        // ID of User on google, facebook, twitter.
        this.providerUserId = key.getProviderUserId();
    }

}