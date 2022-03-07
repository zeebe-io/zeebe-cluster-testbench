package io.zeebe.clustertestbench.internal.cloud;

import io.zeebe.clustertestbench.internal.cloud.request.CreateGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.request.UpdateChannelRequest;
import io.zeebe.clustertestbench.internal.cloud.response.ChannelInfo;
import io.zeebe.clustertestbench.internal.cloud.response.ChannelInfo.GenerationInfo;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.List;

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
}
