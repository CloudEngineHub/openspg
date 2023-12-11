package com.antgroup.openspg.builder.core.physical.process;

import com.antgroup.openspg.builder.core.physical.operator.OperatorFactory;
import com.antgroup.openspg.builder.core.physical.operator.PythonOperatorFactory;
import com.antgroup.openspg.builder.core.physical.operator.protocol.InvokeResult;
import com.antgroup.openspg.builder.core.physical.operator.protocol.InvokeResultWrapper;
import com.antgroup.openspg.builder.core.runtime.BuilderContext;
import com.antgroup.openspg.builder.model.exception.BuilderException;
import com.antgroup.openspg.builder.model.pipeline.config.UserDefinedExtractNodeConfig;
import com.antgroup.openspg.builder.model.record.BaseRecord;
import com.antgroup.openspg.builder.model.record.BuilderRecord;
import com.antgroup.openspg.core.schema.model.identifier.SPGTypeIdentifier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

@SuppressWarnings("unchecked")
public class UserDefinedExtractProcessor
    extends BaseExtractProcessor<UserDefinedExtractNodeConfig> {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final OperatorFactory operatorFactory;

  public UserDefinedExtractProcessor(String id, String name, UserDefinedExtractNodeConfig config) {
    super(id, name, config);
    this.operatorFactory = PythonOperatorFactory.getInstance();
  }

  @Override
  public void doInit(BuilderContext context) throws BuilderException {
    super.doInit(context);
    this.operatorFactory.init(context);
    this.operatorFactory.loadOperator(config.getOperatorConfig());
  }

  @Override
  public List<BaseRecord> process(List<BaseRecord> inputs) {
    List<BaseRecord> results = new ArrayList<>();
    for (BaseRecord record : inputs) {
      BuilderRecord builderRecord = (BuilderRecord) record;
      Map<String, Object> result =
          (Map<String, Object>)
              operatorFactory.invoke(config.getOperatorConfig(), builderRecord.getProps());

      InvokeResultWrapper<List<InvokeResult>> invokeResultWrapper =
          mapper.convertValue(
              result, new TypeReference<InvokeResultWrapper<List<InvokeResult>>>() {});
      if (invokeResultWrapper == null || CollectionUtils.isEmpty(invokeResultWrapper.getData())) {
        continue;
      }

      for (InvokeResult data : invokeResultWrapper.getData()) {
        results.add(
            new BuilderRecord(
                null, SPGTypeIdentifier.parse(data.getSpgTypeName()), data.getProperties()));
      }
    }
    return results;
  }

  @Override
  public void close() throws Exception {}
}
