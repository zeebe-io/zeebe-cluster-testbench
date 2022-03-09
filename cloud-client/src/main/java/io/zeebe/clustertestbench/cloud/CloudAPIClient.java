package io.zeebe.clustertestbench.cloud;

import io.zeebe.clustertestbench.cloud.response.ClusterInfo;
import io.zeebe.clustertestbench.cloud.response.CreateClusterResponse;
import io.zeebe.clustertestbench.cloud.response.CreateZeebeClientResponse;
import io.zeebe.clustertestbench.cloud.response.ParametersResponse;
import io.zeebe.clustertestbench.cloud.response.ZeebeClientConnectiontInfo;
import io.zeebe.clustertestbench.cloud.response.ZeebeClientInfo;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.Collections;
import java.util.List;

@Path("clusters")
public interface CloudAPIClient {

  @GET
  @Path("parameters")
  @Consumes("application/json")
  @Produces("application/json")
  ParametersResponse getParameters();

  @GET
  @Consumes("application/json")
  @Produces("application/json")
  List<ClusterInfo> listClusterInfos();

  @POST
  @Path("/")
  @Consumes("application/json")
  @Produces("application/json")
  CreateClusterResponse createCluster(CreateClusterRequest request);

  @GET
  @Path("{clusterId}")
  @Consumes("application/json")
  @Produces("application/json")
  ClusterInfo getClusterInfo(@PathParam("clusterId") String clusterId);

  @DELETE
  @Path("{clusterId}")
  @Consumes("application/json")
  void deleteCluster(@PathParam("clusterId") String clusterId);

  @GET
  @Path("{clusterId}/clients")
  @Consumes("application/json")
  @Produces("application/json")
  List<ZeebeClientInfo> listZeebeClientInfos(@PathParam("clusterId") String clusterId);

  @POST
  @Path("{clusterId}/clients")
  @Consumes("application/json")
  @Produces("application/json")
  CreateZeebeClientResponse createZeebeClient(
      @PathParam("clusterId") String clusterId, CreateZeebeClientRequest request);

  @GET
  @Path("{clusterId}/clients/{clientId}/")
  @Consumes("application/json")
  @Produces("application/json")
  ZeebeClientConnectiontInfo getZeebeClientInfo(
      @PathParam("clusterId") String clusterId, @PathParam("clientId") String zeebeClientId);

  @DELETE
  @Path("{clusterId}/clients/{clientId}")
  @Consumes("application/json")
  void deleteZeebeClient(
      @PathParam("clusterId") String clusterId, @PathParam("clientId") String zeebeClientId);

  record CreateClusterRequest(
      String name, String planTypeId, String channelId, String generationId, String regionId) {}

  record CreateZeebeClientRequest(String clientName) {

    private static final List<String> PERMISSIONS = Collections.singletonList("zeebe");

    public List<String> getPermissions() {
      return PERMISSIONS;
    }
  }
}
