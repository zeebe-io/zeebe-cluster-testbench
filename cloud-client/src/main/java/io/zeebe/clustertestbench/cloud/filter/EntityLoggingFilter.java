package io.zeebe.clustertestbench.cloud.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Response filter to log received entities based on https://stackoverflow.com/a/36677808/3442860
 */
@Priority(Integer.MIN_VALUE)
public final class EntityLoggingFilter
    implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityLoggingFilter.class);
  private static final String ENTITY_STREAM_PROPERTY = "EntityLoggingFilter.entityStream";
  private static final int MAX_ENTITY_SIZE = 1024 * 1024;

  @Override
  public void filter(final ClientRequestContext requestContext) {
    if (requestContext.hasEntity()) {
      final var stream = new LoggingStream(requestContext.getEntityStream());
      requestContext.setEntityStream(stream);
      requestContext.setProperty(ENTITY_STREAM_PROPERTY, stream);
    }
  }

  @Override
  public void filter(
      final ClientRequestContext requestContext, final ClientResponseContext responseContext)
      throws IOException {
    if (responseContext.hasEntity()) {
      final var stream = logInboundEntitySafely(requestContext, responseContext);
      responseContext.setEntityStream(stream);
    }
  }

  @Override
  public void aroundWriteTo(final WriterInterceptorContext context)
      throws IOException, WebApplicationException {
    context.proceed();
  }

  /** Logs the entity and resets the stream it read it from */
  private InputStream logInboundEntitySafely(
      final ClientRequestContext requestContext, final ClientResponseContext responseContext)
      throws IOException {
    var stream = responseContext.getEntityStream();
    if (!stream.markSupported()) {
      stream = new BufferedInputStream(stream);
    }
    stream.mark(MAX_ENTITY_SIZE + 1);
    logEntityFromStream(stream, requestContext, responseContext);
    stream.reset();
    return stream;
  }

  private void logEntityFromStream(
      final InputStream stream,
      final ClientRequestContext requestContext,
      final ClientResponseContext responseContext)
      throws IOException {
    final String method = requestContext.getMethod();
    final var uri = Objects.toString(requestContext.getUri(), null);
    final var requestEntity = Objects.toString(requestContext.getEntity(), null);
    final String request =
        Stream.of(method, uri, requestEntity)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
    final int status = responseContext.getStatus();
    final var responseEntity = IOUtils.toString(stream, StandardCharsets.UTF_8);
    final var response = String.format("%d %s", status, responseEntity);
    log(String.format("%s => %s%n", request, response));
  }

  private void log(final String entry) {
    LOGGER.debug(entry);
  }

  private static final class LoggingStream extends FilterOutputStream {

    final StringBuilder sb = new StringBuilder();
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    LoggingStream(final OutputStream out) {
      super(out);
    }

    StringBuilder getStringBuilder() {
      // write entity to the builder
      final byte[] entity = baos.toByteArray();

      sb.append(new String(entity, 0, entity.length, StandardCharsets.UTF_8));
      sb.append('\n');

      return sb;
    }

    @Override
    public void write(final int i) throws IOException {
      baos.write(i);
      out.write(i);
    }
  }
}
