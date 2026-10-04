package com.webfrolic.sdk.oauth.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webfrolic.sdk.oauth.exception.SdkException;
import com.webfrolic.sdk.oauth.model.ResponsePayLoad;

public class ApiClient {

	public static final String CONTENT_TYPE = "Content-Type";
	public static final String AUTHORIZATION = "Authorization";
	public static final String ACCEPT = "Accept";
	public static final String APPLICATION_JSON_VALUE = "application/json";
	private final static ObjectMapper mapper = new ObjectMapper();
	private static Logger logger = LoggerFactory.getLogger(ApiClient.class);
	protected static HttpClient httpClient = HttpClient.newHttpClient();

	private static HttpRequest buildClientRequest(String param, String accessToken) throws Exception {
		String requestUri = SdkUtil.idpServer + param;
		logger.info("query url {} with authorization {} for get request", requestUri, accessToken);
		return HttpRequest.newBuilder().uri(URI.create(requestUri))
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(ACCEPT, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, accessToken).GET().build();
	}

	private static HttpRequest buildClientPostRequest(String param, String body, String accessToken) throws Exception {
		String requestUri = SdkUtil.idpServer + param;
		logger.info("query url {} with authorization {} for post request", requestUri, accessToken);
		return HttpRequest.newBuilder().uri(URI.create(requestUri))
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(ACCEPT, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, accessToken)
				.POST(body != null ? BodyPublishers.ofString(body) : BodyPublishers.noBody()).build();
	}

	private static String DELIMITTER = "?";
	private static String DELIMITTER_EXTENDED = "&";

	public static <T> List<T> getQueryListIteratively(TypeReference<ResponsePayLoad<T>> typeRef, String path,
			String accessToken) throws Exception {
		List<T> finalList = new LinkedList<>();
		Integer nextPageNumber = 1;

		String delimitter = DELIMITTER;
		if (path.contains("?")) {
			delimitter = DELIMITTER_EXTENDED;
		}
		while (nextPageNumber != null) {
			ResponsePayLoad<T> response = queryList(typeRef, path + delimitter + "pageNumber=" + nextPageNumber,
					accessToken);
			finalList.addAll(response.getResults());
			nextPageNumber = response.getNextPageNumber();
		}
		return finalList;
	}

	public static <T> ResponsePayLoad<T> queryList(TypeReference<ResponsePayLoad<T>> typeRef, String inputPath,
			String accessToken) throws Exception {
		HttpResponse<String> response = httpClient.send(buildClientRequest(inputPath, accessToken),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {

			return mapper.readValue(response.body(), typeRef);
		} else {
			logger.warn("Error {} fetching the profile Property {}", response.statusCode(), response.toString());
		}
		return null;

	}

	public static <T> ResponsePayLoad<T> post(TypeReference<ResponsePayLoad<T>> typeRef, String inputPath, Object body,
			String accessToken) throws Exception {
		HttpResponse<String> response = httpClient.send(
				buildClientPostRequest(inputPath, mapper.writeValueAsString(body), accessToken),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200 || response.statusCode() == 201) {
			return mapper.readValue(response.body(), typeRef);
		} else {
			logger.warn("Error {} fetching the profile Property {}", response.statusCode(), response.toString());
		}
		return null;

	}

	public static <T> T post(Class<T> outputClass, String inputPath, Object body, String accessToken) throws Exception {
		HttpResponse<String> response = httpClient.send(
				buildClientPostRequest(inputPath, mapper.writeValueAsString(body), accessToken),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200 || response.statusCode() == 201) {
			return mapper.readValue(response.body(), outputClass);
		} else {
			logger.warn("Error {} fetching the profile Property {}", response.statusCode(), response.toString());
		}
		return null;

	}

	public static void post(String inputPath, Object body, String accessToken) throws Exception {
		HttpResponse<String> response = httpClient.send(buildClientPostRequest(inputPath,
				body != null ? mapper.writeValueAsString(body) : null, accessToken),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200 || response.statusCode() == 201) {
			return;
		} else {
			logger.warn("Error {} with post request {}", response.statusCode(), response.toString());
			throw new SdkException("error.post");
		}

	}

	public static void delete(String inputPath, String accessToken) throws Exception {
		HttpResponse<String> response = httpClient.send(buildClientDeleteRequest(inputPath, accessToken),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200 || response.statusCode() == 204) {
			return;
		} else {
			logger.warn("Error {} deleting {}", response.statusCode(), response.toString());
			throw new SdkException("error.post");
		}

	}

	private static HttpRequest buildClientDeleteRequest(String inputPath, String accessToken) {
		String requestUri = SdkUtil.idpServer + inputPath;
		logger.info("query url {} with authorization {} for delete request", requestUri, accessToken);
		return HttpRequest.newBuilder().uri(URI.create(requestUri))
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(ACCEPT, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, accessToken).DELETE().build();
	}

	public static <T> T query(Class<T> classInput, String inputPath, String accessToken) throws Exception {
		HttpResponse<String> response = httpClient.send(buildClientRequest(inputPath, accessToken),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {

			return mapper.readValue(response.body(), classInput);
		} else {
			logger.warn("Error {} fetching the profile Property {}", response.statusCode(), response.toString());
		}
		return null;

	}
}
