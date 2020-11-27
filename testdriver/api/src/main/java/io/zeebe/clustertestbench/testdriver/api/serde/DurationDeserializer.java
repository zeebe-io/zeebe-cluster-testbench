package io.zeebe.clustertestbench.testdriver.api.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Duration;

public class DurationDeserializer extends StdDeserializer<Duration> {
  private static final long serialVersionUID = 1L;

  public DurationDeserializer() {
    this(null);
  }

  public DurationDeserializer(final Class<?> vc) {
    super(vc);
  }

  @Override
  public Duration deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    return Duration.parse(p.getText());
  }
}
