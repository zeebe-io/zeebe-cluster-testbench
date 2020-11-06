package io.zeebe.clustertestbench.cloud.request;

public class CreateClusterRequest {

  private final String name;
  private final String planTypeId;
  private final String channelId;
  private final String generationId;
  private final String regionId;

  public CreateClusterRequest(
      String name, String planTypeId, String channelId, String generationId, String regionId) {
    this.name = name;
    this.planTypeId = planTypeId;
    this.channelId = channelId;
    this.generationId = generationId;
    this.regionId = regionId;
  }

  public String getName() {
    return name;
  }

  public String getPlanTypeId() {
    return planTypeId;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getGenerationId() {
    return generationId;
  }

  public String getRegionId() {
    return regionId;
  }

  @Override
  public String toString() {
    return "CreateClusterRequest [name="
        + name
        + ", planTypeId="
        + planTypeId
        + ", channelId="
        + channelId
        + ", generationId="
        + generationId
        + ", regionId="
        + regionId
        + "]";
  }
}
