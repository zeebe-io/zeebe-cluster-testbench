package io.zeebe.clustertestbench.cloud.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import org.apache.commons.io.IOUtils;

public class BadRequestResponseFilter implements ClientResponseFilter {

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
      throws IOException {

    if (responseContext.getStatus() == 400) {
      String requestBody = new ObjectMapper().writeValueAsString(requestContext.getEntity());
      String responseBody =
          IOUtils.toString(responseContext.getEntityStream(), StandardCharsets.UTF_8);

      throw new IOException(
          "BAD REQUEST returned from URI: "
              + requestContext.getUri()
              + ", requestBody:"
              + requestBody
              + ", responseBody: "
              + responseBody);
    }
  }
}
