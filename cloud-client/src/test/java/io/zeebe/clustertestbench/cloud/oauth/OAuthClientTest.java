package io.zeebe.clustertestbench.cloud.oauth;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.camunda.zeebe.test.util.JsonUtil;
import io.zeebe.clustertestbench.cloud.oauth.OAuthClient.OAuthCredentials;
import io.zeebe.clustertestbench.cloud.oauth.OAuthClient.ServiceAccountTokenRequest;
import io.zeebe.clustertestbench.cloud.oauth.OAuthClient.UserAccountTokenRequest;
import org.junit.jupiter.api.Test;

@WireMockTest
class OAuthClientTest {

  private static final OAuthCredentials EXPECTED_RESPONSE =
      new OAuthCredentials("token", "testToken");
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void testSerializationForServiceAccountTokenRequestWithJackson() throws JsonProcessingException {
    // given
    final var request =
        new ServiceAccountTokenRequest("audience", "clientId", "clientSecret", "grantType");

    final var objectMapper = new ObjectMapper();

    // when
    final var jsonRepresentation = objectMapper.writeValueAsString(request);

    // then
    JsonUtil.assertEquality(
        jsonRepresentation,
        """
        {
          "client_id": "clientId",
          "client_secret": "clientSecret",
          "grant_type": "grantType",
          "audience":"audience"
        }
        """);
  }

  @Test
  void testSerializationForUserAccountTokenRequestWithJackson() throws JsonProcessingException {
    // given
    final var request =
        new UserAccountTokenRequest(
            "audience", "clientId", "clientSecret", "grantType", "username", "password");

    final var objectMapper = new ObjectMapper();

    // when
    final var jsonRepresentation = objectMapper.writeValueAsString(request);

    // then
    JsonUtil.assertEquality(
        jsonRepresentation,
        """
             {
                "client_id":"clientId",
                "client_secret":"clientSecret",
                "grant_type":"grantType",
                "audience":"audience",
                "username":"username",
                "password":"password"
             }
             """);
  }

  @Test
  void testDeserializationOfOAuthCredentialsWithJackson() throws JsonProcessingException {
    // given
    final String exampleResponse =
        """
            {
                "access_token": "lorem ipsum",
                "token_type": "Bearer"
            }
        """;

    // when
    final OAuthCredentials actual =
        OBJECT_MAPPER.readValue(exampleResponse, OAuthCredentials.class);

    // then
    assertThat(actual.tokenType()).isEqualTo("Bearer");
    assertThat(actual.accessToken()).isEqualTo("lorem ipsum");
  }

  @Test
  void testRoundTripForUserAccountTokenRequestAsPartOfRestEasyMarshalling(
      final WireMockRuntimeInfo wmRuntimeInfo) throws JsonProcessingException {
    // given
    stubFor(
        post("/")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(OBJECT_MAPPER.writeValueAsString(EXPECTED_RESPONSE))));

    final var url = wmRuntimeInfo.getHttpBaseUrl();

    final var sut = new OAuthClientFactory().createOAuthClient(url);

    // when
    final var userAccountTokenRequest =
        new UserAccountTokenRequest(
            "audience", "clientId", "clientSecret", "grantType", "username", "password");

    final var actualResponse = sut.requestToken(userAccountTokenRequest);

    // then
    verify(
        postRequestedFor(urlEqualTo("/"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("Accept", equalTo("application/json"))
            .withRequestBody(equalTo(OBJECT_MAPPER.writeValueAsString(userAccountTokenRequest))));
    assertThat(actualResponse).isEqualTo(EXPECTED_RESPONSE);
  }

  @Test
  void testRoundTripForServiceAccountTokenRequestAsPartOfRestEasyMarshalling(
      final WireMockRuntimeInfo wmRuntimeInfo) throws JsonProcessingException {
    // given
    stubFor(
        post("/")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(OBJECT_MAPPER.writeValueAsString(EXPECTED_RESPONSE))));

    final var url = wmRuntimeInfo.getHttpBaseUrl();

    final var sut = new OAuthClientFactory().createOAuthClient(url);

    // when
    final var serviceAccountTokenRequest =
        new ServiceAccountTokenRequest("audience", "clientId", "clientSecret", "grantType");

    final var actualResponse = sut.requestToken(serviceAccountTokenRequest);

    // then
    verify(
        postRequestedFor(urlEqualTo("/"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("Accept", equalTo("application/json"))
            .withRequestBody(
                equalTo(OBJECT_MAPPER.writeValueAsString(serviceAccountTokenRequest))));
    assertThat(actualResponse).isEqualTo(EXPECTED_RESPONSE);
  }
}
