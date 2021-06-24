package io.zeebe.clustertestbench.internal.cloud;

import io.zeebe.clustertestbench.internal.cloud.request.CreateGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.request.UpdateChannelRequest;
import io.zeebe.clustertestbench.internal.cloud.response.ChannelInfo;
import io.zeebe.clustertestbench.internal.cloud.response.ChannelInfo.GenerationInfo;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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
