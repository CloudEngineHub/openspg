package com.antgroup.openspg.builder.model.pipeline.config;

import com.antgroup.openspg.builder.model.pipeline.config.fusing.BaseFusingConfig;
import com.antgroup.openspg.builder.model.pipeline.enums.NodeTypeEnum;
import java.util.List;
import lombok.Getter;

@Getter
public class SubGraphMappingNodeConfig extends BaseMappingNodeConfig {

  private final String spgType;

  private final List<MappingFilter> mappingFilters;

  private final List<MappingConfig> mappingConfigs;

  private final BaseFusingConfig subjectFusingConfig;

  private final List<PredicatingConfig> predicatingConfigs;

  public SubGraphMappingNodeConfig(
      String spgType,
      List<MappingFilter> mappingFilters,
      List<MappingConfig> mappingConfigs,
      BaseFusingConfig subjectFusingConfig,
      List<PredicatingConfig> predicatingConfigs) {
    super(NodeTypeEnum.SUBGRAPH_MAPPING);
    this.spgType = spgType;
    this.mappingFilters = mappingFilters;
    this.mappingConfigs = mappingConfigs;
    this.subjectFusingConfig = subjectFusingConfig;
    this.predicatingConfigs = predicatingConfigs;
  }
}
