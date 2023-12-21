package com.antgroup.openspg.builder.core.operator.fusing.impl;

import com.antgroup.openspg.builder.core.operator.OperatorFactory;
import com.antgroup.openspg.builder.core.operator.python.PythonRecordConvertor;
import com.antgroup.openspg.builder.core.operator.fusing.EntityFusing;
import com.antgroup.openspg.builder.core.operator.python.InvokeResultWrapper;
import com.antgroup.openspg.builder.core.operator.python.PythonOperatorFactory;
import com.antgroup.openspg.builder.core.operator.python.PythonRecord;
import com.antgroup.openspg.builder.core.runtime.BuilderContext;
import com.antgroup.openspg.builder.model.exception.BuilderException;
import com.antgroup.openspg.builder.model.exception.FusingException;
import com.antgroup.openspg.builder.model.pipeline.config.fusing.OperatorFusingConfig;
import com.antgroup.openspg.builder.model.record.BaseSPGRecord;
import com.antgroup.openspg.common.util.CollectionsUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
@SuppressWarnings("unchecked")
public class OperatorFusing implements EntityFusing {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final OperatorFusingConfig fusingConfig;
  private final OperatorFactory operatorFactory;

  public OperatorFusing(OperatorFusingConfig fusingConfig) {
    this.fusingConfig = fusingConfig;
    this.operatorFactory = PythonOperatorFactory.getInstance();
  }

  @Override
  public void init(BuilderContext context) throws BuilderException {
    operatorFactory.init(context);
    operatorFactory.loadOperator(fusingConfig.getOperatorConfig());
  }

  @Override
  public List<BaseSPGRecord> entityFusing(List<BaseSPGRecord> records) throws FusingException {
    List<PythonRecord> pythonRecords =
        CollectionsUtils.listMap(records, PythonRecordConvertor::toPythonRecord);
    InvokeResultWrapper<List<PythonRecord>> invokeResultWrapper = null;
    try {
      Map<String, Object> result =
          (Map<String, Object>)
              operatorFactory.invoke(fusingConfig.getOperatorConfig(), pythonRecords);

      invokeResultWrapper =
          mapper.convertValue(
              result, new TypeReference<InvokeResultWrapper<List<PythonRecord>>>() {});
    } catch (Exception e) {
      throw new FusingException(e, "fusing error");
    }

    if (invokeResultWrapper == null || CollectionUtils.isEmpty(invokeResultWrapper.getData())) {
      return Collections.emptyList();
    }
    return CollectionsUtils.listMap(invokeResultWrapper.getData(), PythonRecordConvertor::toSPGRecord);
  }
}
