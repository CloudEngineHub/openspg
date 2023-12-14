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
package com.antgroup.openspg.server.core.scheduler.service.api.impl;

import com.antgroup.openspg.server.common.model.exception.OpenSPGException;
import com.antgroup.openspg.server.common.model.scheduler.SchedulerEnum.InstanceStatus;
import com.antgroup.openspg.server.common.model.scheduler.SchedulerEnum.LifeCycle;
import com.antgroup.openspg.server.common.model.scheduler.SchedulerEnum.Status;
import com.antgroup.openspg.server.common.model.scheduler.SchedulerEnum.TaskStatus;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerInstance;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerJob;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerTask;
import com.antgroup.openspg.server.core.scheduler.service.api.SchedulerService;
import com.antgroup.openspg.server.core.scheduler.service.common.SchedulerCommonService;
import com.antgroup.openspg.server.core.scheduler.service.engine.SchedulerExecuteService;
import com.antgroup.openspg.server.core.scheduler.service.metadata.SchedulerInstanceService;
import com.antgroup.openspg.server.core.scheduler.service.metadata.SchedulerJobService;
import com.antgroup.openspg.server.core.scheduler.service.metadata.SchedulerTaskService;
import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/** Scheduler Service implementation class */
@Service
public class SchedulerServiceImpl implements SchedulerService {

  private static final int corePoolSize = 1;
  private static final int maximumPoolSize = 20;
  private static final int keepAliveTime = 30;
  private static final int capacity = 100;
  public static final String DEFAULT_VERSION = "V3";

  private ThreadPoolExecutor instanceExecutor =
      new ThreadPoolExecutor(
          corePoolSize,
          maximumPoolSize,
          keepAliveTime,
          TimeUnit.MINUTES,
          new LinkedBlockingQueue<>(capacity),
          runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("instanceExecutor" + thread.getId());
            return thread;
          },
          new ThreadPoolExecutor.DiscardOldestPolicy());

  @Autowired SchedulerJobService schedulerJobService;
  @Autowired SchedulerInstanceService schedulerInstanceService;
  @Autowired SchedulerTaskService schedulerTaskService;
  @Autowired SchedulerCommonService schedulerCommonService;
  @Autowired SchedulerExecuteService schedulerExecuteService;

  @Override
  public SchedulerJob submitJob(SchedulerJob job) {
    setJobPropertyDefaultValue(job);
    checkJobPropertyValidity(job);
    Long id = job.getId();

    if (id == null || id < 0) {
      schedulerJobService.insert(job);
    } else {
      schedulerJobService.update(job);
    }

    this.executeJob(job.getId());

    return job;
  }

  /** set Job Property Default Value */
  private void setJobPropertyDefaultValue(SchedulerJob job) {
    job.setGmtModified(new Date());
    if (job.getGmtCreate() == null) {
      job.setGmtCreate(new Date());
    }

    job.setStatus(Status.ONLINE);
    job.setVersion(DEFAULT_VERSION);
  }

  /** check Job Property is validity */
  private void checkJobPropertyValidity(SchedulerJob job) {
    Assert.notNull(job, "job not null");
    Assert.notNull(job.getProjectId(), "ProjectId not null");
    Assert.hasText(job.getName(), "Name not null");
    Assert.hasText(job.getCreateUser(), "CreateUser not null");
    Assert.notNull(job.getLifeCycle(), "LifeCycle not null");
    Assert.notNull(job.getTranslateType(), "TranslateType not null");
    Assert.notNull(job.getMergeMode(), "MergeMode not null");

    if (LifeCycle.PERIOD.equals(job.getLifeCycle())) {
      String cron = job.getSchedulerCron();
      Assert.hasText(cron, "SchedulerCron not null");
      try {
        new CronExpression(cron);
      } catch (ParseException e) {
        throw new OpenSPGException("Cron {} ParseException:{}", cron, e.getMessage());
      }
    }
  }

  @Override
  public Boolean executeJob(Long id) {
    SchedulerInstance instance = null;
    List<SchedulerInstance> instances = Lists.newArrayList();
    SchedulerJob job = schedulerJobService.getById(id);
    Assert.notNull(job, String.format("job not find id:%s", id));

    switch (job.getLifeCycle()) {
      case REAL_TIME:
        stopJobAllInstance(id);
        instance = schedulerCommonService.generateRealTimeInstance(job);
        break;
      case PERIOD:
        instances.addAll(schedulerCommonService.generatePeriodInstance(job));
        break;
      case ONCE:
        instance = schedulerCommonService.generateOnceInstance(job);
        break;
      default:
        break;
    }
    if (instance != null) {
      instances.add(instance);
    }
    if (CollectionUtils.isEmpty(instances)) {
      return false;
    }

    for (SchedulerInstance ins : instances) {
      Long instanceId = ins.getId();
      Runnable instanceRunnable = () -> schedulerExecuteService.executeInstance(instanceId);
      instanceExecutor.execute(instanceRunnable);
    }
    return true;
  }

  private void stopJobAllInstance(Long jobId) {
    SchedulerInstance query = new SchedulerInstance();
    query.setJobId(jobId);
    List<SchedulerInstance> instances = schedulerInstanceService.getNotFinishInstance(query);
    if (CollectionUtils.isEmpty(instances)) {
      return;
    }

    for (SchedulerInstance instance : instances) {
      stopInstance(instance.getId());
    }
  }

  @Override
  public Boolean enableJob(Long id) {
    SchedulerJob job = schedulerJobService.getById(id);
    Assert.notNull(job, String.format("job not find id:%s", id));
    SchedulerJob updateJob = new SchedulerJob();
    updateJob.setId(id);
    updateJob.setStatus(Status.ONLINE);
    Long flag = schedulerJobService.update(updateJob);
    if (flag <= 0) {
      return false;
    }

    if (LifeCycle.REAL_TIME.equals(job.getLifeCycle())) {
      this.executeJob(id);
    }
    return true;
  }

  @Override
  public Boolean disableJob(Long id) {
    SchedulerJob job = schedulerJobService.getById(id);
    Assert.notNull(job, String.format("job not find id:%s", id));
    SchedulerJob updateJob = new SchedulerJob();
    updateJob.setId(id);
    updateJob.setStatus(Status.OFFLINE);
    Long flag = schedulerJobService.update(updateJob);
    if (flag <= 0) {
      return false;
    }

    stopJobAllInstance(id);
    return true;
  }

  @Override
  public Boolean deleteJob(Long id) {
    stopJobAllInstance(id);
    Integer flag = schedulerJobService.deleteById(id);
    if (flag <= 0) {
      return false;
    }

    schedulerInstanceService.deleteByJobId(id);
    schedulerTaskService.deleteByJobId(id);
    return true;
  }

  @Override
  public boolean updateJob(SchedulerJob job) {
    Long id = schedulerJobService.update(job);
    return id > 0;
  }

  @Override
  public SchedulerJob getJobById(Long id) {
    return schedulerJobService.getById(id);
  }

  @Override
  public List<SchedulerJob> searchJobs(SchedulerJob query) {
    return schedulerJobService.query(query);
  }

  @Override
  public SchedulerInstance getInstanceById(Long id) {
    return schedulerInstanceService.getById(id);
  }

  @Override
  public Boolean stopInstance(Long id) {
    SchedulerInstance instance = schedulerInstanceService.getById(id);
    Assert.notNull(instance, String.format("instance not find id:%s", id));
    schedulerCommonService.setInstanceFinish(
        instance, InstanceStatus.TERMINATE, TaskStatus.TERMINATE);
    return true;
  }

  @Override
  public Boolean setFinishInstance(Long id) {
    SchedulerInstance instance = schedulerInstanceService.getById(id);
    Assert.notNull(instance, String.format("instance not find id:%s", id));
    schedulerCommonService.setInstanceFinish(
        instance, InstanceStatus.SET_FINISH, TaskStatus.SET_FINISH);
    return true;
  }

  @Override
  public Boolean restartInstance(Long id) {
    SchedulerInstance instance = schedulerInstanceService.getById(id);
    Assert.notNull(instance, String.format("instance not find id:%s", id));
    SchedulerJob job = schedulerJobService.getById(instance.getJobId());
    Assert.notNull(job, String.format("job not find id:%s", id));
    SchedulerInstance reRunInstance = schedulerCommonService.generateOnceInstance(job);
    if (reRunInstance == null) {
      return false;
    }

    Long instanceId = reRunInstance.getId();
    Runnable instanceRunnable = () -> schedulerExecuteService.executeInstance(instanceId);
    instanceExecutor.execute(instanceRunnable);
    return true;
  }

  @Override
  public Boolean triggerInstance(Long id) {
    SchedulerInstance instance = schedulerInstanceService.getById(id);
    Assert.notNull(instance, String.format("instance not find id:%s", id));
    if (InstanceStatus.isFinished(instance.getStatus())) {
      throw new OpenSPGException("The instance has been finished");
    }
    schedulerExecuteService.executeInstance(id);
    return true;
  }

  @Override
  public List<SchedulerInstance> searchInstances(SchedulerInstance query) {
    return schedulerInstanceService.query(query);
  }

  @Override
  public List<SchedulerTask> searchTasks(SchedulerTask query) {
    return schedulerTaskService.query(query);
  }
}
