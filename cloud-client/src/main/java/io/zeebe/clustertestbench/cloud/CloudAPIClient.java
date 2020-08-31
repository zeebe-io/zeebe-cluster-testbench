package io.zeebe.clustertestbench.cloud;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import io.zeebe.clustertestbench.cloud.request.CreateZeebeClientRequest;
import io.zeebe.clustertestbench.cloud.request.CreateClusterRequest;
import io.zeebe.clustertestbench.cloud.response.ZeebeClientConnectiontInfo;
import io.zeebe.clustertestbench.cloud.response.ZeebeClientInfo;
import io.zeebe.clustertestbench.cloud.response.ClusterInfo;
import io.zeebe.clustertestbench.cloud.response.CreateZeebeClientResponse;
import io.zeebe.clustertestbench.cloud.response.CreateClusterResponse;
import io.zeebe.clustertestbench.cloud.response.ParametersResponse;

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
	public CreateZeebeClientResponse createZeebeClient(@PathParam("clusterId") String clusterId, CreateZeebeClientRequest request);
	
	@GET
	@Path("{clusterId}/clients/{clientId}/")
	@Consumes("application/json")
	@Produces("application/json")
	public ZeebeClientConnectiontInfo getZeebeClientInfo(@PathParam("clusterId") String clusterId, @PathParam("clientId") String zeebeClientId);
	
	@DELETE
	@Path("{clusterId}/clients/{clientId}")
	@Consumes("application/json")
	public void deleteZeebeClient(@PathParam("clusterId") String clusterId, @PathParam("clientId") String zeebeClientId);

}
