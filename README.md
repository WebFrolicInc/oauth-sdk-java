#The SDK makes it easier to enable OIDC/OAUTH Flow for your application.


The SDK allows you to integrate with almost any identity provider that supports OAuth2.1 or OIDC flows with PKCE 

It is specially designed to work with Webfrolic Identity provider to manage
  - Continuous Sessions
  - Tenant support
  - API access support

Integeration:

Pre-req:

Come up with Redirect URIs - OAuth2.1 and OIDC are browser based flows (except for client credential grant type) and one of the mechanisms to establish trust is to share a agreed upon application end point where the Identity Provider redirects the User after Authentication and Authorization
If your application server end point is https://myendpoint.com then the redirect URL would be https://myendpoint.com/oauth2/callback

Identity Provider steps

1. Create a OAuth2.1 or OIDC application with the Identity Provider
2. Enter your redirect URL with the provider
3. Provide a logout URL as well if this is primary application for Webfrolic Identity Provider, it will be https://myendpoint.com//oauth2/logout/callback
4. Retrieve the following properties from Application configuration
   1.  clientId
   2.  clientSecret (if applicable)
   3.  Authorization URL
   4.  Token URL
   5.  JWK URL
   6.  Issuer
   7.  (Optional) Session End Point URL (for better session management if this is the primary application)
   

Application steps

Create a instance of the Filter with the following parameters

1. Server URL - URL for your application
2. Issuer
3. JWK URL
4. Authorization URL
5. Token URL
6. ClientId
7. ClientSecret (if Applicable)
8. Logout URL
9. List of excluded paths from the filter (static or public resources)
                 

Customizations:

By default the SKD uses "openid email profile" for the scopes. You can always overwrite it to add/modify scopes

The SDK ships with default Session store which uses server session to store user data. You can plugin your own custom session store by implementing SessionStore interface and providing it to filter

##How it works
It is Servlet filter based, it intercepts every request to check if its authenticated or not. If its not authenticated then it redirect the user to Identity provider for authentication


