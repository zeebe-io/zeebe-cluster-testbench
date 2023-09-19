package io.zeebe.clustertestbench.cloud.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailedResponseFilter implements ClientResponseFilter {

  private static final Logger LOG = LoggerFactory.getLogger(FailedResponseFilter.class);

  @Override
  public void filter(
      final ClientRequestContext requestContext, final ClientResponseContext responseContext)
      throws IOException {
    if (responseContext.getStatus() > 399) {

      final String requestBody = new ObjectMapper().writeValueAsString(requestContext.getEntity());
      final String responseBody =
          IOUtils.toString(responseContext.getEntityStream(), StandardCharsets.UTF_8);

      final String errorMessage =
          responseContext.getStatusInfo().toEnum()
              + " returned from URI: "
              + requestContext.getUri()
              + ", requestBody:"
              + requestBody
              + ", responseBody: "
              + responseBody;

      LOG.error(errorMessage);
      throw new IOException(errorMessage);
    }
  }
}
