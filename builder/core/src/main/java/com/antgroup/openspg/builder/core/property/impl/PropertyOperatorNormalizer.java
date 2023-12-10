package com.antgroup.openspg.builder.core.property.impl;

import com.antgroup.openspg.builder.core.physical.operator.OperatorFactory;
import com.antgroup.openspg.builder.core.physical.operator.PythonOperatorFactory;
import com.antgroup.openspg.builder.core.physical.operator.protocol.EvalResult;
import com.antgroup.openspg.builder.core.physical.operator.protocol.Vertex;
import com.antgroup.openspg.builder.core.property.PropertyNormalizer;
import com.antgroup.openspg.builder.core.runtime.BuilderContext;
import com.antgroup.openspg.builder.model.exception.BuilderException;
import com.antgroup.openspg.builder.model.exception.PropertyNormalizeException;
import com.antgroup.openspg.builder.model.pipeline.config.OperatorPropertyNormalizerConfig;
import com.antgroup.openspg.builder.model.record.property.BasePropertyRecord;
import com.antgroup.openspg.core.schema.model.type.OperatorKey;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.apache.commons.collections4.CollectionUtils;

@SuppressWarnings("unchecked")
public class PropertyOperatorNormalizer implements PropertyNormalizer {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final OperatorKey operatorKey;
  private final OperatorPropertyNormalizerConfig normalizerConfig;
  private final OperatorFactory operatorFactory;

  public PropertyOperatorNormalizer(OperatorPropertyNormalizerConfig config) {
    this.normalizerConfig = config;
    this.operatorKey = config.getConfig().toKey();
    this.operatorFactory = PythonOperatorFactory.getInstance();
  }

  @Override
  public void init(BuilderContext context) throws BuilderException {
    operatorFactory.init(context);
    operatorFactory.loadOperator(normalizerConfig.getConfig());
  }

  @Override
  public void propertyNormalize(BasePropertyRecord record) throws PropertyNormalizeException {
    List<String> rawValues = record.getRawValues();

    List<String> ids = new ArrayList<>(rawValues.size());
    for (String rawValue : rawValues) {
      Map<String, Object> result =
          (Map<String, Object>) operatorFactory.invoke(operatorKey, rawValue, new HashMap<>(0));
      EvalResult<List<Vertex>> evalResult =
          mapper.convertValue(result, new TypeReference<EvalResult<List<Vertex>>>() {});

      if (evalResult == null || CollectionUtils.isEmpty(evalResult.getData())) {
        throw new PropertyNormalizeException("property={} normalize failed", rawValue);
      }

      if (evalResult.getData().size() > 1) {
        throw new PropertyNormalizeException("property={} normalize failed", rawValue);
      }

      ids.add(evalResult.getData().get(0).getBizId());
    }
    record.getValue().setStds(Collections.singletonList(ids));
    record.getValue().setIds(ids);
  }
}
