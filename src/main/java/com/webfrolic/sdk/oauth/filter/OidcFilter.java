package com.webfrolic.sdk.oauth.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.webfrolic.sdk.oauth.DefaultSessionStoreImpl;
import com.webfrolic.sdk.oauth.SessionStore;
import com.webfrolic.sdk.oauth.model.IdpSession;
import com.webfrolic.sdk.oauth.model.OAuth2TokenResponse;
import com.webfrolic.sdk.oauth.model.PairBean;
import com.webfrolic.sdk.oauth.service.SdkUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This filter checks if the requested path requires authentication 1. If yes
 * then it checks if there is an existing session a. If session is present then
 * check for IP address match, if no match then delete the session b. If there
 * is no session then c. Check if its a OIDC callback URL in which case parse
 * the user from idToken d. Otherwise craft a OIDC URL for authentication to the
 * IDP server
 * 
 * Continuously send signals to IDP for session usage if this is configured as
 * primary application
 * 
 *
 */
@SuppressWarnings("deprecation")
public class OidcFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(OidcFilter.class.getName());
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String AUTHORIZATION = "Authorization";
	public static final String ACCEPT = "Accept";
	public static final String APPLICATION_JSON_VALUE = "application/json";
	private static final HttpClient httpClient = HttpClient.newHttpClient();
	public static final String SESSION_CODE_VERIFIER = "pkce_code_verifier";
	public static final String SESSION_STATE = "pkce_state";
	public static final String URL_TO_CONTINUE = "url_to_continue";
	static final String OIDC_CALLBACK_URI = "/oauth2/callback";
	static final String OIDC_LOGOUT_CALLBACK_URI = "/oauth2/logout/callback";
	static final String LOGOUT = "/logout";
	private final ObjectMapper mapper = new ObjectMapper();

	private final IdTokenValidator tokenValidator;
	private final String authUrl;
	private final String tokenUrl;
	private final String clientId;
	private String clientSecret;
	private String sessionEndpointUrl;
	private final List<String> excludedPaths = new LinkedList<>();
	private String scopes = "openid profile email";
	private String serverUrl = null;
	private String serverLogout = null;
	private boolean enableLogout = false;
	private String logoutUrl = null;
	private SessionStore sessionStore = new DefaultSessionStoreImpl();

	/**
	 * This is for OIDC or OAUth2 Client
	 * 
	 * @param serverUrl
	 * @param issuer
	 * @param jwkUrl
	 * @param authUrl
	 * @param tokenUrl
	 * @param clientId
	 * @throws Exception
	 */
	public OidcFilter(String serverUrl, String issuer, String jwkUrl, String authUrl, String tokenUrl, String clientId,
			List<String> excludedPaths) throws Exception {
		this.tokenValidator = new IdTokenValidator(issuer, clientId, jwkUrl);
		this.authUrl = authUrl;
		this.tokenUrl = tokenUrl;
		this.clientId = clientId;

		this.serverUrl = URLEncoder.encode(serverUrl + OIDC_CALLBACK_URI, StandardCharsets.UTF_8.toString());
		this.excludedPaths.add("/error");
		if (excludedPaths != null) {
			this.excludedPaths.addAll(excludedPaths);
		}
		if (StringUtils.isBlank(clientId)) {
			throw new IllegalArgumentException("client id is missing");
		}

		if (StringUtils.isBlank(authUrl)) {
			throw new IllegalArgumentException("Auth URL is missing");
		}
		if (StringUtils.isBlank(tokenUrl)) {
			throw new IllegalArgumentException("Token URL is missing");
		}
	}
	
	public void setScope(String scope) {
		this.scopes = scope;
	}

	public OidcFilter(String serverUrl, String issuer, String jwkUrl, String authUrl, String tokenUrl, String clientId,
			String clientSecret, List<String> excludedPaths) throws Exception {
		this.tokenValidator = new IdTokenValidator(issuer, clientId, jwkUrl);
		this.authUrl = authUrl;
		this.tokenUrl = tokenUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.serverUrl = URLEncoder.encode(serverUrl + OIDC_CALLBACK_URI, StandardCharsets.UTF_8.toString());
		this.excludedPaths.add("/error");
		if (excludedPaths != null) {
			this.excludedPaths.addAll(excludedPaths);
		}
		if (StringUtils.isBlank(clientId)) {
			throw new IllegalArgumentException("client id is missing");
		}
		if (StringUtils.isBlank(clientSecret)) {
			throw new IllegalArgumentException("clientSecret is missing");
		}
		if (StringUtils.isBlank(authUrl)) {
			throw new IllegalArgumentException("Auth URL is missing");
		}
		if (StringUtils.isBlank(tokenUrl)) {
			throw new IllegalArgumentException("Token URL is missing");
		}
	}

	public OidcFilter(String serverUrl, String issuer, String jwkUrl, String authUrl, String tokenUrl, String clientId,
			String clientSecret, List<String> excludedPaths, boolean enableLogout, String logoutUrl, String sessionUrl)
			throws Exception {
		this.tokenValidator = new IdTokenValidator(issuer, clientId, jwkUrl);
		this.authUrl = authUrl;
		this.tokenUrl = tokenUrl;
		this.clientId = clientId;
		this.logoutUrl = logoutUrl;
		this.enableLogout = enableLogout;
		this.clientSecret = clientSecret;
		this.serverUrl = URLEncoder.encode(serverUrl + OIDC_CALLBACK_URI, StandardCharsets.UTF_8.toString());
		this.serverLogout = URLEncoder.encode(serverUrl + OIDC_LOGOUT_CALLBACK_URI, StandardCharsets.UTF_8.toString());
		this.excludedPaths.add("/error");
		if (excludedPaths != null) {
			this.excludedPaths.addAll(excludedPaths);
		}

		this.sessionEndpointUrl = sessionUrl;

		// Run validation of properties
		if (this.enableLogout && StringUtils.isBlank(this.serverLogout)) {
			throw new IllegalArgumentException("logout url not provided");
		}
		if (StringUtils.isBlank(clientId)) {
			throw new IllegalArgumentException("client id is missing");
		}
		if (StringUtils.isBlank(clientSecret)) {
			throw new IllegalArgumentException("clientSecret is missing");
		}
		if (StringUtils.isBlank(authUrl)) {
			throw new IllegalArgumentException("Auth URL is missing");
		}
		if (StringUtils.isBlank(tokenUrl)) {
			throw new IllegalArgumentException("Token URL is missing");
		}

	}

	public void setSessionStore(SessionStore store) {
		this.sessionStore = store;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String path = req.getRequestURI().substring(req.getContextPath().length());

		boolean isExcluded = excludedPaths.stream().anyMatch(path::startsWith);
		if (isExcluded) {
			chain.doFilter(request, response);
			return;
		}

		if (enableLogout && LOGOUT.equals(path)) {

			IdpSession userRequest = sessionStore.getUserSession(req, res);
			// If its logout request and session exists
			if (userRequest != null) {
				sessionStore.deleteUserSession(req, res);
				sendOidcLogout(req, res, userRequest.getSessionId());
			} else {
				chain.doFilter(request, response);
			}
			return;
		} else if (enableLogout && OIDC_LOGOUT_CALLBACK_URI.equals(path)) {
			res.sendRedirect(LOGOUT);
			return;
		}

		// process the request for oidc callback
		else if (OIDC_CALLBACK_URI.equals(path)) {

			try {
				processCallback(req, res);
				logger.info("processed the oidc callback correctly");
				res.sendRedirect(this.sessionStore.getAttribute(req, res, URL_TO_CONTINUE));
				return;
			} catch (Exception ex) {
				logger.warn("Error with processing oidc request", ex.getMessage());
				res.sendRedirect("/error?type=oidc&message=" + ex.getMessage());
				return;
			}

		}

		IdpSession userRequest = sessionStore.getUserSession(req, res);

		if (userRequest != null) {

			if (this.sessionEndpointUrl != null) {
				PairBean<String> ipPair = SdkUtil.getIpAddressFromRequest(req, false);
				if (!(userRequest.getClientIpAddress().equals(ipPair.getFirst())
						&& userRequest.getProxyIpAddress().equals(ipPair.getSecond()))) {
					userRequest = null;
				}
				// If its been more than 5 minutes since last check
				if (StringUtils.isNotBlank(userRequest.getSession())
						&& (System.currentTimeMillis() - userRequest.getLastUpdated()) / 1000 > 300) {
					boolean validSession = updateSession(req, res, userRequest);
					if (!validSession) {
						userRequest = null;
					}
				}
			}
		}

		if (userRequest == null) {
			logger.info("User not found in the session");
			sendOidcRequest(req, res);
			return;
		}

		chain.doFilter(request, response);

	}

	private void sendOidcRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String codeVerifier = PkceUtil.generateCodeVerifier();
		String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);
		String state = PkceUtil.generateState();

		// Persist in server-side session — the browser never sees code_verifier
		this.sessionStore.addAttribute(req, res, URL_TO_CONTINUE,
				req.getRequestURI() + (StringUtils.isBlank(req.getQueryString()) ? "" : "?" + req.getQueryString()));
		this.sessionStore.addAttribute(req, res, SESSION_CODE_VERIFIER, codeVerifier);
		this.sessionStore.addAttribute(req, res, SESSION_STATE, state);

		StringBuilder builder = new StringBuilder(this.authUrl).append("?");
		builder.append("response_type=").append("code");
		builder.append("&redirect_uri=").append(URLEncoder.encode(this.serverUrl, Charset.defaultCharset()));
		builder.append("&client_id=").append(this.clientId);
		builder.append("&scope=").append(URLEncoder.encode(scopes, Charset.defaultCharset()));
		builder.append("&state=").append(state);
		builder.append("&code_challenge=").append(codeChallenge);
		builder.append("&code_challenge_method=").append("S256");
		builder.append("&nonce=").append("state");

		res.sendRedirect(URI.create(builder.toString()).toString());
	}

	private void sendOidcLogout(HttpServletRequest req, HttpServletResponse res, String sessionId) throws IOException {
		StringBuilder builder = new StringBuilder(this.logoutUrl).append("?");
		builder.append("client_id=").append(this.clientId);
		builder.append("&redirect_uri=").append(URLEncoder.encode(this.serverLogout, Charset.defaultCharset()));
		builder.append("&sessionId=").append(sessionId);

		res.sendRedirect(URI.create(builder.toString()).toString());
	}

	private void processCallback(HttpServletRequest req, HttpServletResponse rep) throws Exception {
		String code = req.getParameter("code");
		String state = req.getParameter("state");
		String error = req.getParameter("error");
		String errorDescription = req.getParameter("error_description");
		if (error != null) {
			logger.warn("Error {} in oidc response", error);
			throw new OidcException(
					"Authorization error: " + error + (errorDescription != null ? " — " + errorDescription : ""));
		}

		String expectedState = this.sessionStore.getAttribute(req, rep, SESSION_STATE);
		if (expectedState == null || !expectedState.equals(state)) {
			logger.warn("State mismatch — possible CSRF attack, expected {} and found {}", expectedState, state);
			throw new OidcException("State mismatch — possible CSRF attack");
		}

		String codeVerifier = this.sessionStore.getAttribute(req, rep, SESSION_CODE_VERIFIER);

		if (codeVerifier == null) {
			logger.warn("No code_verifier in session — login flow not initiated correctly");
			throw new OidcException("No code_verifier in session — login flow not initiated correctly");
		}
		// Clean up one-time session values
		this.sessionStore.removeAttribute(req, rep, SESSION_STATE);
		this.sessionStore.removeAttribute(req, rep, SESSION_CODE_VERIFIER);

		logger.info("Exchange for tokens");
		// Exchange authorization code for tokens ────────────────────────────
		OAuth2TokenResponse tokens = exchangeCodeForTokens(code, codeVerifier);

		logger.info("Validate JWT token");
		// Validate the id_token JWT ─────────────────────────────────────────
		Map<String, Object> claims = tokenValidator.validate(tokens.getIdToken());

		logger.info("recieved the following claims {}", claims);
		IdpSession userRequest = new IdpSession();
		userRequest.setFirstName((String) claims.get("first_name"));
		userRequest.setLastName((String) claims.get("last_name"));
		userRequest.setEmail((String) claims.get("email"));
		userRequest.setSubjectId((String) claims.get("sub")); // This is the userId
		userRequest.setTenantId((String) claims.get("tenant"));
		if (StringUtils.isNotBlank(userRequest.getTenantId())) {
			userRequest.setTenantRole((String) claims.get("role"));
		} else {
			userRequest.setRole((String) claims.get("role"));
		}
		userRequest.setAccessToken(tokens.getAccessToken());
		userRequest.setSessionId((String) claims.get("sessionId"));
		PairBean<String> ipPair = SdkUtil.getIpAddressFromRequest(req, false);
		userRequest.setClientIpAddress(ipPair.getFirst());
		userRequest.setProxyIpAddress(ipPair.getSecond());
		userRequest.setLastUpdated(System.currentTimeMillis());

		this.sessionStore.createUserSession(req, rep, userRequest);
		// We are using convention here, if the access token is encrypted then this is
		// first party main application
		if (this.sessionEndpointUrl != null && StringUtils.isNotBlank(userRequest.getAccessToken())
				&& userRequest.getAccessToken().startsWith("enc")) {
			getUserSession(req, rep);
		}

	}

	/**
	 * Call the OAuth session end point to exchange the oAuth session cookie
	 * 
	 * @param req
	 * @param res
	 * @param userRequest
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private boolean updateSession(HttpServletRequest req, HttpServletResponse res, IdpSession userRequest) {

		try {
			Map<String, String> body = new HashMap<>();
			body.put("ipAddress", userRequest.getClientIpAddress());
			body.put("session", userRequest.getSession());
			body.put("client_id", this.clientId);

			StringBuilder formBodyBuilder = new StringBuilder();
			for (Map.Entry<String, String> singleEntry : body.entrySet()) {
				if (formBodyBuilder.length() > 0) {
					formBodyBuilder.append("&");
				}
				formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
				formBodyBuilder.append("=");
				formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
			}

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.sessionEndpointUrl))
					.header(CONTENT_TYPE, "application/x-www-form-urlencoded").header("Accept", "application/json")
					.header(AUTHORIZATION, "Bearer " + userRequest.getAccessToken())

					.POST(BodyPublishers.ofString(formBodyBuilder.toString())).build();

			HttpResponse<String> tokenResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			logger.info("token response status {}", tokenResponse.statusCode());

			if (tokenResponse.statusCode() != 200) {
				logger.warn("Response code for token end point is " + tokenResponse.statusCode());
				return false;
			}
			String sessionString = tokenResponse.body();
			userRequest.setSession(sessionString);
			this.sessionStore.saveUserSession(req, res, userRequest);
			return true;
		} catch (Exception e) {
			logger.warn("Error {} retriving session cookie", e.getMessage());
		}
		return false;

	}

	/**
	 * This method retrieves user session for primary application
	 * 
	 * @param req
	 * @param rep
	 * @throws Exception
	 */
	private void getUserSession(HttpServletRequest req, HttpServletResponse rep) throws Exception {

		IdpSession userRequest = this.sessionStore.getUserSession(req, rep);

		Map<String, String> body = new HashMap<>();
		body.put("ipAddress", userRequest.getClientIpAddress());
		body.put("sessionId", userRequest.getSessionId());
		body.put("client_id", this.clientId);

		StringBuilder formBodyBuilder = new StringBuilder();
		for (Map.Entry<String, String> singleEntry : body.entrySet()) {
			if (formBodyBuilder.length() > 0) {
				formBodyBuilder.append("&");
			}
			formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
			formBodyBuilder.append("=");
			formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(this.sessionEndpointUrl + "?" + formBodyBuilder.toString()))
				.header(CONTENT_TYPE, "application/x-www-form-urlencoded").header("Accept", "application/json")
				.header(AUTHORIZATION, "Bearer " + userRequest.getAccessToken()).GET().build();

		HttpResponse<String> tokenResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		logger.info("token response status {}", tokenResponse.statusCode());

		if (tokenResponse.statusCode() != 200) {
			throw new OidcException("Response code for token end point is " + tokenResponse.statusCode());
		}
		String sessionString = tokenResponse.body();
		userRequest.setSession(sessionString);
		this.sessionStore.saveUserSession(req, rep, userRequest);

	}

	private OAuth2TokenResponse exchangeCodeForTokens(String code, String codeVerifier) throws Exception {

		Map<String, String> body = new HashMap<>();
		body.put("grant_type", "authorization_code");
		body.put("code", code);
		body.put("redirect_uri", serverUrl);
		body.put("client_id", this.clientId);
		body.put("code_verifier", codeVerifier); // PKCE verification value

		// Include client_secret if configured (confidential client)
		if (this.clientSecret != null) {
			body.put("client_secret", this.clientSecret);
		}

		StringBuilder formBodyBuilder = new StringBuilder();
		for (Map.Entry<String, String> singleEntry : body.entrySet()) {
			if (formBodyBuilder.length() > 0) {
				formBodyBuilder.append("&");
			}
			formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
			formBodyBuilder.append("=");
			formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
		}

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(this.tokenUrl))
				.header(CONTENT_TYPE, "application/x-www-form-urlencoded").header("Accept", "application/json")

				.POST(BodyPublishers.ofString(formBodyBuilder.toString())).build();

		HttpResponse<String> tokenResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		logger.info("token response status {}", tokenResponse.statusCode());

		if (tokenResponse.statusCode() != 200) {
			throw new OidcException("Response code for token end point is " + tokenResponse.statusCode());
		}

		OAuth2TokenResponse response = mapper.readValue(tokenResponse.body(), OAuth2TokenResponse.class);

		if (response == null || response.getIdToken() == null) {
			throw new OidcException("Token endpoint returned no id_token");
		}

		return response;

	}

	@Override
	public void destroy() {
	}

	static class OidcException extends RuntimeException {
		private static final long serialVersionUID = -6397971827092596528L;

		OidcException(String msg) {
			super(msg);
		}
	}

	private class IdTokenValidator {

		private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

		private String clientId;

		public IdTokenValidator(String issuer, String clientId, String jwkUri) {

			this.clientId = clientId;

			try {
				// Fetch public keys from IdP's JWKS endpoint
				JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(URI.create(jwkUri).toURL());

				// Accept RS256 and ES256 — the two most common IdP signing algorithms
				JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
						new HashSet<>(Arrays.asList(JWSAlgorithm.RS256, JWSAlgorithm.ES256)), jwkSource);

				DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
				processor.setJWSKeySelector(keySelector);

				// Verify required claims are present
				processor.setJWTClaimsSetVerifier(
						new DefaultJWTClaimsVerifier<>(new JWTClaimsSet.Builder().issuer(issuer).build(),
								new HashSet<>(Arrays.asList("sub", "iat", "exp", "aud"))));

				this.jwtProcessor = processor;

			} catch (Exception e) {
				throw new IllegalStateException("Failed to initialize JWT processor", e);
			}
		}

		/**
		 * Validates the id_token and returns its claims. Throws if the token is
		 * invalid, expired, or has wrong issuer/audience.
		 */
		public Map<String, Object> validate(String idToken) {
			try {
				JWTClaimsSet claims = jwtProcessor.process(idToken, null);

				// Verify audience contains our client_id
				if (!claims.getAudience().contains(clientId)) {
					throw new IllegalArgumentException("id_token audience does not contain client_id: " + clientId);
				}

				return claims.getClaims();

			} catch (Exception e) {
				throw new IllegalArgumentException("id_token validation failed: " + e.getMessage(), e);
			}
		}
	}

	public final class PkceUtil {

		private static final SecureRandom RANDOM = new SecureRandom();

		private PkceUtil() {
		}

		/**
		 * Generates a cryptographically random code_verifier. Length is 64 characters —
		 * well within the 43-128 range required by RFC 7636.
		 */
		public static String generateCodeVerifier() {
			byte[] bytes = new byte[48];
			RANDOM.nextBytes(bytes);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		}

		/**
		 * Derives the code_challenge from the verifier using S256 (SHA-256). S256 is
		 * mandatory for server-side apps; plain is only permitted when S256 is not
		 * possible.
		 */
		public static String generateCodeChallenge(String codeVerifier) {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(codeVerifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
				return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
			} catch (java.security.NoSuchAlgorithmException e) {
				throw new IllegalStateException("SHA-256 not available", e);
			}
		}

		/**
		 * Generates a cryptographically random state parameter. Stored in session and
		 * validated on callback to prevent CSRF.
		 */
		public static String generateState() {
			byte[] bytes = new byte[24];
			RANDOM.nextBytes(bytes);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		}
	}

}
