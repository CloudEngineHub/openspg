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
package com.antgroup.openspg.server.core.scheduler.service.metadata.impl.local;

import com.antgroup.openspg.common.util.CommonUtils;
import com.antgroup.openspg.server.common.model.base.Page;
import com.antgroup.openspg.server.core.scheduler.model.query.SchedulerJobQuery;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerJob;
import com.antgroup.openspg.server.core.scheduler.service.metadata.SchedulerJobService;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/** Scheduler Job Service implementation class: Add, delete, update, and query Jobs */
@Service
public class LocalSchedulerJobServiceImpl implements SchedulerJobService {

  private static ConcurrentHashMap<Long, SchedulerJob> jobs = new ConcurrentHashMap<>();
  private static AtomicLong maxId = new AtomicLong(0L);

  @Override
  public synchronized Long insert(SchedulerJob record) {
    Long id = maxId.incrementAndGet();
    record.setId(id);
    record.setGmtModified(new Date());
    jobs.put(id, record);
    return id;
  }

  @Override
  public synchronized int deleteById(Long id) {
    SchedulerJob record = jobs.remove(id);
    return record == null ? 0 : 1;
  }

  @Override
  public synchronized Long update(SchedulerJob record) {
    Long id = record.getId();
    SchedulerJob oldRecord = getById(id);
    if (record.getGmtModified() != null
        && !oldRecord.getGmtModified().equals(record.getGmtModified())) {
      return 0L;
    }
    record = CommonUtils.merge(oldRecord, record);
    record.setGmtModified(new Date());
    jobs.put(id, record);
    return id;
  }

  @Override
  public SchedulerJob getById(Long id) {
    SchedulerJob oldJob = jobs.get(id);
    if (oldJob == null) {
      throw new RuntimeException("not find id:" + id);
    }
    SchedulerJob job = new SchedulerJob();
    BeanUtils.copyProperties(oldJob, job);
    return job;
  }

  @Override
  public Page<List<SchedulerJob>> query(SchedulerJobQuery record) {
    Page<List<SchedulerJob>> page = new Page<>();
    List<SchedulerJob> jobList = Lists.newArrayList();
    page.setData(jobList);
    for (Long key : jobs.keySet()) {
      SchedulerJob job = jobs.get(key);
      if (!CommonUtils.equals(job.getId(), record.getId())
          || !CommonUtils.equals(job.getCreateUser(), record.getCreateUser())
          || !CommonUtils.equals(job.getTranslateType(), record.getTranslateType())
          || !CommonUtils.equals(job.getLifeCycle(), record.getLifeCycle())
          || !CommonUtils.equals(job.getStatus(), record.getStatus())
          || !CommonUtils.equals(job.getMergeMode(), record.getMergeMode())
          || !CommonUtils.contains(job.getName(), record.getName())) {
        continue;
      }

      SchedulerJob target = new SchedulerJob();
      BeanUtils.copyProperties(job, target);
      jobList.add(target);
    }
    page.setPageNo(1);
    page.setPageSize(jobList.size());
    page.setTotal(Long.valueOf(jobList.size()));
    return page;
  }

}
