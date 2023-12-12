/*
 * Copyright 2023 Ant Group CO., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 */

/** Alipay.com Inc. Copyright (c) 2004-2022 All Rights Reserved. */
package com.antgroup.openspg.server.core.scheduler.service.translate;

import com.antgroup.openspg.server.core.scheduler.model.common.WorkflowDag;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerJob;

/**
 * 调度DAG 翻译层
 *
 * @author 庄舟 @Title: DagTranslate.java @Description:
 * @date 2022/3/11 16:41
 */
public interface Translate {

  /**
   * translate to workflow Dag
   *
   * @param job
   * @return
   */
  WorkflowDag translate(SchedulerJob job);
}
