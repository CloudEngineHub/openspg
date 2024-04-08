/*
 * Copyright 2023 OpenSPG Authors
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

package com.antgroup.openspg.reasoner.recorder;

import java.util.List;
import java.util.Map;

import com.antgroup.openspg.reasoner.common.graph.vertex.IVertexId;

public interface IExecutionRecorder {
  /** get readable string */
  String toReadableString();

  /** call when entry a new rdg */
  void entryRDG(String rdg);

  /** call when leave a rdg */
  void leaveRDG();

  /** record result num, like filer, expendInto */
  void stageResult(String stage, long result);

  /** finish */
  void stageResultWithDesc(String stage, long result, String finishDescribe);

  /** record result num, like filer, expendInto */
  void stageResultWithDetail(String stage, long result, Map<String, List<IVertexId>> runtimeDetail);

  /** finish */
  void stageResultWithDescAndDetail(String stage, long result, String finishDescribe, Map<String, List<IVertexId>> runtimeDetail);
}
