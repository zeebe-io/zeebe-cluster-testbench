package io.zeebe.clustertestbench.testdriver.sequential.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> {

  private static final long serialVersionUID = 1L;

  public DurationSerializer() {
    this(null);
  }

  public DurationSerializer(final Class<Duration> t) {
    super(t);
  }

  @Override
  public void serialize(
      final Duration value, final JsonGenerator gen, final SerializerProvider provider)
      throws IOException {
    gen.writeString(value.toString());
  }
}
