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
package com.antgroup.openspg.server.common.model.statistics;

import lombok.Data;

@Data
public class StatisticsQuery {

  private Long id;
  private String resourceTag;
  private String resourceId;
  private String statisticsType;
  private String statisticsDate;
  private Long num;
  private String startTime;
  private String endTime;

  private String sort;
  private String order;
}
