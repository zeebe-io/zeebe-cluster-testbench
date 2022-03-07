package io.zeebe.clustertestbench.cloud;

import io.zeebe.clustertestbench.cloud.request.CreateClusterRequest;
import io.zeebe.clustertestbench.cloud.request.CreateZeebeClientRequest;
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
import java.util.List;

@Path("clusters")
public interface CloudAPIClient {

  @GET
  @Path("parameters")
  @Consumes("application/json")
  @Produces("application/json")
  public ParametersResponse getParameters();

  @GET
  @Consumes("application/json")
  @Produces("application/json")
  public List<ClusterInfo> listClusterInfos();

  @POST
  @Path("/")
  @Consumes("application/json")
  @Produces("application/json")
  public CreateClusterResponse createCluster(CreateClusterRequest request);

  @GET
  @Path("{clusterId}")
  @Consumes("application/json")
  @Produces("application/json")
  public ClusterInfo getClusterInfo(@PathParam("clusterId") String clusterId);

  @DELETE
  @Path("{clusterId}")
  @Consumes("application/json")
  public void deleteCluster(@PathParam("clusterId") String clusterId);

  @GET
  @Path("{clusterId}/clients")
  @Consumes("application/json")
  @Produces("application/json")
  public List<ZeebeClientInfo> listZeebeClientInfos(@PathParam("clusterId") String clusterId);

  @POST
  @Path("{clusterId}/clients")
  @Consumes("application/json")
  @Produces("application/json")
  public CreateZeebeClientResponse createZeebeClient(
      @PathParam("clusterId") String clusterId, CreateZeebeClientRequest request);

  @GET
  @Path("{clusterId}/clients/{clientId}/")
  @Consumes("application/json")
  @Produces("application/json")
  public ZeebeClientConnectiontInfo getZeebeClientInfo(
      @PathParam("clusterId") String clusterId, @PathParam("clientId") String zeebeClientId);

  @DELETE
  @Path("{clusterId}/clients/{clientId}")
  @Consumes("application/json")
  public void deleteZeebeClient(
      @PathParam("clusterId") String clusterId, @PathParam("clientId") String zeebeClientId);
}
