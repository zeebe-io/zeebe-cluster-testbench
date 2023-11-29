package io.zeebe.clustertestbench.internal.cloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StubExternalConsoleAPIClient implements ExternalConsoleAPIClient {

  public static final String DEFAULT_GENERATION_NAME = "default-generation";
  public static final String DEFAULT_GENERATION_UUID = "1a2b3c4f56789-1234-abcd-1a2b3c4f5678";

  public static final String GENERATION_NAME_WITHOUT_UPGRADE_FROM = "generationWithoutUpgradeFrom";
  public static final String GENERATION_UUID_WITHOUT_UPGRADE_FROM =
      "4f4b3c4f56789-1234-abcd-1a2b3c4f5678";
  public static final String DEFAULT_ZEEBE_IMAGE = "camunda/zeebe:0.23.7";
  public static final String DEFAULT_OPERATE_IMAGE = "camunda/operate:0.23.2";

  public static final String KEY_ZEEBE_IMAGE = "zeebe";
  public static final String KEY_OPERATE_IMAGE = "operate";

  private final List<GenerationInfo> generationInfos = new ArrayList<>();

  private final boolean broken;

  public StubExternalConsoleAPIClient(final boolean broken) {
    this.broken = broken;

    final Map<String, String> versions = new HashMap<>();

    versions.put(KEY_ZEEBE_IMAGE, DEFAULT_ZEEBE_IMAGE);
    versions.put(KEY_OPERATE_IMAGE, DEFAULT_OPERATE_IMAGE);

    final GenerationInfo generationInfo =
        new GenerationInfo(
            DEFAULT_GENERATION_UUID,
            DEFAULT_GENERATION_NAME,
            versions,
            List.of(Map.of("uuid", "fake")));
    generationInfos.add(generationInfo);

    final GenerationInfo generationInfoWithoutUpgradeFrom =
        new GenerationInfo(
            GENERATION_UUID_WITHOUT_UPGRADE_FROM,
            GENERATION_NAME_WITHOUT_UPGRADE_FROM,
            versions,
            List.of());
    generationInfos.add(generationInfoWithoutUpgradeFrom);
  }

  @Override
  public List<GenerationInfo> listGenerationInfos() {
    return Collections.unmodifiableList(generationInfos);
  }

  @Override
  public GenerationInfo cloneGeneration(
      final String toClonedGenerationUUID, final CloneGenerationRequest request) {
    if (!broken) {
      final String zeebeVersion = Objects.requireNonNullElse(request.zeebeVersion(), "null");
      final String operateVersion = Objects.requireNonNullElse(request.operateVersion(), "null");

      final ExternalConsoleAPIClient.GenerationInfo generationInfo =
          new ExternalConsoleAPIClient.GenerationInfo(
              UUID.randomUUID().toString(),
              request.name(),
              Map.of("zeebe", zeebeVersion, "operate", operateVersion),
              List.of(Map.of("uuid", "fake")));

      generationInfos.add(generationInfo);
      return generationInfo;
    }
    throw new RuntimeException("Creation of generation unsuccessful");
  }

  @Override
  public void deleteGeneration(final String generationUUID) {
    if (!broken) {
      generationInfos.removeIf(gi -> generationUUID.equals(gi.uuid()));
    }
  }
}
