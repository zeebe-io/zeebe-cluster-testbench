package io.zeebe.clustertestbench.internal.cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.List;
import java.util.Map;

@Path("external")
public interface ExternalConsoleAPIClient {

  @GET
  @Path("generations")
  @Consumes("application/json")
  List<GenerationInfo> listGenerationInfos();

  @POST
  @Path("generations/{generationUUID}")
  @Produces("application/json")
  @Consumes("application/json")
  GenerationInfo cloneGeneration(
      @PathParam("generationUUID") String generationUUID, CloneGenerationRequest request);

  @DELETE
  @Path("generations/{generationUUID}")
  @Produces("application/json")
  @Consumes("application/json")
  void deleteGeneration(@PathParam("generationUUID") String generationUUID);

  record CloneGenerationRequest(String name, String zeebeVersion, String operateVersion) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record GenerationInfo(String uuid, String name, Map<String, String> versions) {}
}
