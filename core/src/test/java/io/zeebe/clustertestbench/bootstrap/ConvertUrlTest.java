package io.zeebe.clustertestbench.bootstrap;

import static io.zeebe.clustertestbench.bootstrap.Launcher.convertZeebeUrlToOperateUrl;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ConvertUrlTest {

  @Test
  public void shouldConvertToValidOperateURI() {
    // given
    final var endpoint = "https://wvdf8734-fr32-47a8-a3ed-5fi13265e523.bru-2.zeebe.camunda.io/";

    // when
    final String operateEndpoint = convertZeebeUrlToOperateUrl(endpoint);

    // then
    assertThat(operateEndpoint)
        .isEqualTo("https://bru-2.operate.camunda.io/wvdf8734-fr32-47a8-a3ed-5fi13265e523");
  }

  @Test
  public void shouldConvertToValidOperateURIAndHandleMissingProtocol() {
    // given
    final var endpoint = "wvdf8734-fr32-47a8-a3ed-5fi13265e523.bru-2.zeebe.camunda.io/";

    // when
    final String operateEndpoint = convertZeebeUrlToOperateUrl(endpoint);

    // then
    assertThat(operateEndpoint)
        .isEqualTo("https://bru-2.operate.camunda.io/wvdf8734-fr32-47a8-a3ed-5fi13265e523");
  }

  @Test
  public void shouldConvertToValidOperateURIAndHandleMissingSlash() {
    // given
    final var endpoint = "wvdf8734-fr32-47a8-a3ed-5fi13265e523.bru-2.zeebe.camunda.io";

    // when
    final String operateEndpoint = convertZeebeUrlToOperateUrl(endpoint);

    // then
    assertThat(operateEndpoint)
        .isEqualTo("https://bru-2.operate.camunda.io/wvdf8734-fr32-47a8-a3ed-5fi13265e523");
  }
}
