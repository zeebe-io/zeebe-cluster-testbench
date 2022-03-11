package io.zeebe.clustertestbench.internal.cloud;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.List;
import java.util.Map;

@Path("api")
public interface InternalCloudAPIClient {

  @GET
  @Path("generations")
  @Consumes("application/json")
  List<GenerationInfo> listGenerationInfos();

  @POST
  @Path("generations")
  @Produces("application/json")
  @Consumes("application/json")
  void createGeneration(CreateGenerationRequest request);

  @DELETE
  @Path("generations/{generationUUID}")
  @Produces("application/json")
  @Consumes("application/json")
  void deleteGeneration(@PathParam("generationUUID") String generationUUID);

  @GET
  @Path("channels")
  @Produces("application/json")
  @Consumes("application/json")
  List<ChannelInfo> listChannelInfos();

  @PATCH
  @Path("channels/{channelUUID}")
  @Produces("application/json")
  @Consumes("application/json")
  void updateChannel(@PathParam("channelUUID") String channelUUID, UpdateChannelRequest request);

  record CreateGenerationRequest(
      String name, Map<String, String> versions, List<String> upgradeableFrom) {}

  record UpdateChannelRequest(
      String name,
      @JsonProperty("isDefault") boolean isDefault,
      String defaultGenerationId,
      List<String> allowedGenerationIds) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record ChannelInfo(
      String uuid,
      String name,
      List<GenerationInfo> allowedGenerations,
      GenerationInfo defaultGeneration,
      @JsonAlias("isDefault") boolean isDefault) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record GenerationInfo(String uuid, String name, Map<String, String> versions) {}
}
