How spring social works:

1. After logging in with gg/fb, SocialAuthenticationProvider.authenticate will be called
first before going to any controller end points

2. The method above requires usersConnectionRepository to look up user_id,
which can be assigned in SocialConfig.getUsersConnectionRepository (must have)

3. After getting all the required tokens and params (redirected from gg/fb), it looks up in UserConnection table

4. If it returns an user => call SocialUserDetailsServiceImpl => call UserDetailsServiceImpl

5. If not => go to controller end points

========================================= Ans from SOF: ==============================================

Spring Social is rather Agnostic to the Sign in Providers when properly implemented. I believe you are confused
 on the flow of Spring Social. At the point you describe spring social has already looked up the connections
 table and presumably found a record, so it looks up your user table for the user matching with userId
  (as referenced in the connections table) This is usually associated with the username.

This connection <-> User matching is done in the SocialAuthenticationProvider
before calling the SocialUserDetails loadUserByUserId method. So the SocialAuthenticationProvider already
does what you ask for by querying the usersConnectionRepository and comparing the provider connection to find the appropriate user.
Now for your case you would can go ahead and override the user service that you have setup. As long as the userId used on
the doPostSignUp call matches the one you look up in the loadUserByUserId, the proper user will be retrieved.

This is a sample:

Wherever your signup logic is executed, you call the doPostSignup and pass the desired user id (Username or another uniquely identifiable String)

ProviderSignInUtils.doPostSignUp(variableForNewUserObject.getId().toString(), request);
Now you Override the loadUserByUserId in SimpleSocialUserDetailsService

@Autowired
private UserDetailsService userDetailsService;

@Override
public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException, DataAccessException {

    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

    return (SocialUserDetails) userDetails;
}