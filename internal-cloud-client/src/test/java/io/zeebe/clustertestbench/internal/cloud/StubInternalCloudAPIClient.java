package io.zeebe.clustertestbench.internal.cloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.zeebe.clustertestbench.cloud.response.ChannelInfo;
import io.zeebe.clustertestbench.cloud.response.GenerationInfo;
import io.zeebe.clustertestbench.internal.cloud.request.CreateGenerationRequest;
import io.zeebe.clustertestbench.internal.cloud.request.UpdateChannelRequest;

public class StubInternalCloudAPIClient implements InternalCloudAPIClient {
	
	public static final String DEFAULT_GENERATION_NAME = "default-generation";
	public static final String DEFAULT_GENERATION_UUID = "1a2b3c4f56789-1234-abcd-1a2b3c4f5678";
	public static final String DEFAULT_ZEEBE_IMAGE = "camunda/zeebe:0.23.7";
	public static final String DEFAULT_OPERATE_IMAGE = "camunda/operate:0.23.2";
	public static final String DEFAULT_ELASTIC_CURATOR_IMAGE = "quay.io/pires/docker-elasticsearch-curator:5.5.1";
	public static final String DEFAULT_ELASTIC_OSS_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch-oss:6.8.8";
	
	public static final String DEFAULT_CHANNEL_NAME = "default-channel";
	public static final String DEFAULT_CHANNEL_UUID = "1a2b3c4f56789-abcd-1234-1a2b3c4f5678";
	
	public static final String KEY_ZEEBE_IMAGE = "zeebe";
	public static final String KEY_OPERATE_IMAGE = "operate";
	public static final String KEY_ELASTIC_CURATOR_IMAGEE = "elasticSearchCurator";
	public static final String KEY_ELASTIC_OSS_IMAGE = "elasticSearchOss";

	private final List<GenerationInfo> generationInfos = new ArrayList<>();
	private final List<ChannelInfo> channelInfos = new ArrayList<>();
	
	private final boolean broken;

	public StubInternalCloudAPIClient(boolean broken) {
		this.broken = broken;
		
		GenerationInfo generationInfo = new GenerationInfo();
		generationInfo.setName(DEFAULT_GENERATION_NAME);
		generationInfo.setUuid(DEFAULT_GENERATION_UUID);

		Map<String, String> versions = new HashMap<>();
		
		versions.put(KEY_ZEEBE_IMAGE, DEFAULT_ZEEBE_IMAGE);
		versions.put(KEY_OPERATE_IMAGE, DEFAULT_OPERATE_IMAGE);
		versions.put(KEY_ELASTIC_CURATOR_IMAGEE, DEFAULT_ELASTIC_CURATOR_IMAGE);
		versions.put(KEY_ELASTIC_OSS_IMAGE, DEFAULT_ELASTIC_OSS_IMAGE);

		generationInfo.setVersions(versions);
		
		generationInfos.add(generationInfo);
		
		ChannelInfo channelInfo = new ChannelInfo();
		channelInfo.setName(DEFAULT_CHANNEL_NAME);
		channelInfo.setUuid(DEFAULT_CHANNEL_UUID);
		channelInfo.setDefaultGeneration(generationInfo);
		channelInfo.getAllowedGenerations().add(generationInfo);
		
		channelInfos.add(channelInfo);
		
	}
	
	@Override
	public List<GenerationInfo> listGenerationInfos() {
		return Collections.unmodifiableList(generationInfos);
	}

	@Override
	public void createGeneration(CreateGenerationRequest request) {
		if (!broken) {
			GenerationInfo generationInfo = new GenerationInfo();
			generationInfo.setName(request.getName());
			generationInfo.setVersions(request.getVersions());
			generationInfo.setUuid(UUID.randomUUID().toString());
			
			generationInfos.add(generationInfo);			
		}
	}

	@Override
	public void deleteGeneration(String generationUUID) {
		if (!broken) {
			generationInfos.removeIf(gi -> generationUUID.equals(gi.getUuid()));
		}
	}

	@Override
	public List<ChannelInfo> listChannelInfos() {
		return Collections.unmodifiableList(channelInfos);
	}

	@Override
	public void updateChannel(String channelUUID, UpdateChannelRequest request) {

	}

}
