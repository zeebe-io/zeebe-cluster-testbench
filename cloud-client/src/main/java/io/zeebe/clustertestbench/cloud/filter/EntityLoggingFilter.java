package io.zeebe.clustertestbench.cloud.filter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
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
  private static final int MAX_ENTITY_SIZE = 1024 * 1024; // original was 1024 * 8 but was too small

  @Override
  public void filter(ClientRequestContext requestContext) {
    if (requestContext.hasEntity()) {
      final var stream = new LoggingStream(requestContext.getEntityStream());
      requestContext.setEntityStream(stream);
      requestContext.setProperty(ENTITY_STREAM_PROPERTY, stream);
    }
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
      throws IOException {
    if (responseContext.hasEntity()) {
      final var entityStream = responseContext.getEntityStream();
      responseContext.setEntityStream(logInboundEntitySafely(requestContext, entityStream));
    }
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext context)
      throws IOException, WebApplicationException {
    final var stream = (LoggingStream) context.getProperty(ENTITY_STREAM_PROPERTY);
    context.proceed();
    if (stream != null) {
      log(stream.getStringBuilder().toString());
    }
  }

  /** Logs the entity and resets the stream it read it from */
  private InputStream logInboundEntitySafely(
      final ClientRequestContext requestContext, final InputStream entityStream)
      throws IOException {
    var stream = entityStream;
    if (!stream.markSupported()) {
      stream = new BufferedInputStream(stream);
    }
    stream.mark(MAX_ENTITY_SIZE + 1);
    logEntityFromStream(stream, requestContext);
    stream.reset();
    return stream;
  }

  private void logEntityFromStream(
      final InputStream stream, final ClientRequestContext requestContext) throws IOException {
    final var method = requestContext.getMethod();
    final var uri = requestContext.getUri();
    final var entity = readEntityFromStream(stream);
    log(String.format("%s %s => %s%n", method, uri, entity));
  }

  private String readEntityFromStream(final InputStream stream) throws IOException {
    final var sb = new StringBuilder();
    var totalRead = 0;
    var numRead = 0;
    var entity = new byte[MAX_ENTITY_SIZE + 1];
    while ((numRead = stream.read(entity, totalRead, MAX_ENTITY_SIZE)) > 0) {
      sb.append(new String(entity, 0, Math.min(numRead, MAX_ENTITY_SIZE), StandardCharsets.UTF_8));
      totalRead += numRead;
      entity = new byte[MAX_ENTITY_SIZE + 1];
    }
    return sb.toString();
  }

  private void log(String entry) {
    LOGGER.debug("{}", entry);
  }

  private static final class LoggingStream extends FilterOutputStream {

    final StringBuilder sb = new StringBuilder();
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    LoggingStream(OutputStream out) {
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
