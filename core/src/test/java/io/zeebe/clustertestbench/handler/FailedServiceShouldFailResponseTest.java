package io.zeebe.clustertestbench.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;

import com.sun.net.httpserver.HttpServer;
import io.zeebe.clustertestbench.cloud.filter.BadResponseFilter;
import io.zeebe.clustertestbench.internal.cloud.InternalCloudAPIClient;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.camunda.community.zeebe.testutils.stubs.ActivatedJobStub;
import org.camunda.community.zeebe.testutils.stubs.JobClientStub;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FailedServiceShouldFailResponseTest {

  InternalCloudAPIClient spyInternalApiClient;

  CreateGenerationInCamundaCloudHandler sutCreateGenerationHandler;

  JobClientStub jobClientStub = new JobClientStub();

  ActivatedJobStub activatedJobStub;
  private HttpServer localhost;
  private Client client;

  @BeforeEach
  public void setUp() throws IOException {
    client = ClientBuilder.newBuilder().register(BadResponseFilter.class).build();
    final var target = (ResteasyWebTarget) client.target("http://localhost:8081");
    final var cloudAPIClient = target.proxy(InternalCloudAPIClient.class);

    spyInternalApiClient = spy(cloudAPIClient);
    localhost = HttpServer.create(new InetSocketAddress("localhost", 8081), 1);
    localhost.start();
    activatedJobStub = jobClientStub.createActivatedJob();
    sutCreateGenerationHandler = new CreateGenerationInCamundaCloudHandler(spyInternalApiClient);
  }

  @AfterEach
  void teardown() {
    localhost.stop(0);
    client.close();
  }

  @Test
  public void shouldFailedToCreateGenerationWithDefaultVersions() {
    // given
    // when - then throw
    assertThatThrownBy(() -> sutCreateGenerationHandler.handle(jobClientStub, activatedJobStub))
        .hasCauseInstanceOf(IOException.class)
        .hasMessageContaining("404 Not Found");
  }
}
