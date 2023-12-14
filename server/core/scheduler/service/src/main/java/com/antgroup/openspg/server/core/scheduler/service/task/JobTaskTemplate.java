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
package com.antgroup.openspg.server.core.scheduler.service.task;

import com.antgroup.openspg.common.util.CommonUtils;
import com.antgroup.openspg.common.util.DateTimeUtils;
import com.antgroup.openspg.server.common.model.exception.OpenSPGException;
import com.antgroup.openspg.server.common.model.scheduler.SchedulerEnum.InstanceStatus;
import com.antgroup.openspg.server.common.model.scheduler.SchedulerEnum.TaskStatus;
import com.antgroup.openspg.server.core.scheduler.model.common.TaskDag;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerInstance;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerTask;
import com.antgroup.openspg.server.core.scheduler.service.common.SchedulerCommonService;
import com.antgroup.openspg.server.core.scheduler.service.metadata.SchedulerTaskService;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** JobTask Template class. execute before,process,finally and other functions */
@Slf4j
public abstract class JobTaskTemplate implements JobTask {

  /** lock max time */
  public static final Integer LOCK_TIME_MINUTES = 15;

  @Autowired SchedulerTaskService schedulerTaskService;
  @Autowired SchedulerCommonService schedulerCommonService;

  @Override
  public void executeEntry(JobTaskContext context) {
    TaskStatus status = null;
    boolean lock = true;
    try {
      lock = lockTask(context);
      if (lock) {
        // Pre-process
        before(context);
        // Core process
        status = process(context);
        context.getTask().setStatus(status);
      }
    } catch (Throwable e) {
      context.getTask().setStatus(TaskStatus.ERROR);
      context.addTraceLog("Scheduling execute exception：%s", CommonUtils.getExceptionToString(e));
      log.error("JobTask process error uniqueId:{}", context.getInstance().getUniqueId(), e);
    }

    processStatus(context, status, lock);
  }

  @Transactional
  public void processStatus(JobTaskContext context, TaskStatus status, boolean lock) {
    try {
      if (status == null) {
        status = TaskStatus.WAIT;
      }
      switch (status) {
        case FINISH:
          finish(context);
          break;
        case SKIP:
          skip(context);
          break;
        case ERROR:
        default:
          break;
      }
    } catch (Throwable e) {
      context.addTraceLog("Scheduling save status error：%s", CommonUtils.getExceptionToString(e));
      log.error("process status error uniqueId:{}", context.getInstance().getUniqueId(), e);
    } finally {
      unlockTask(context, lock);
      finallyFunc(context);
    }
  }

  private boolean lockTask(JobTaskContext context) {
    SchedulerTask task = context.getTask();
    if (task.getLockTime() == null) {
      int count = schedulerTaskService.updateLock(task.getId());
      if (count < 1) {
        context.addTraceLog("Failed to preempt lock, the lock is already occupied!");
        return false;
      }
      context.addTraceLog("Lock preempt successful!");
      return true;
    }
    Date now = new Date();
    Date unLockTime = DateUtils.addMinutes(task.getLockTime(), LOCK_TIME_MINUTES);
    if (now.before(unLockTime)) {
      context.addTraceLog(
          "Last lock preempt time：%s,The threshold was not exceeded. Wait for the execution to complete",
          DateTimeUtils.getDate2LongStr(task.getLockTime()));
      return false;
    }
    context.addTraceLog(
        "Last lock preempt time：%s, The threshold was exceeded. The current process is executed directly",
        DateTimeUtils.getDate2LongStr(task.getLockTime()));
    unlockTask(context, true);
    int count = schedulerTaskService.updateLock(task.getId());
    if (count < 1) {
      context.addTraceLog("Failed to re-preempt lock!");
      return false;
    }
    context.addTraceLog("Re-preempt lock successfully!");
    return true;
  }

  private void unlockTask(JobTaskContext context, boolean lock) {
    if (!lock) {
      return;
    }
    schedulerTaskService.updateUnlock(context.getTask().getId());
    context.addTraceLog("Lock released successfully!");
  }

  public void before(JobTaskContext context) {
    context.addTraceLog("Start process task!");
  }

  public void finish(JobTaskContext context) {
    setTaskFinish(context);
  }

  public void skip(JobTaskContext context) {
    setTaskFinish(context);
  }

  public void finallyFunc(JobTaskContext context) {
    context.addTraceLog(
        "Task scheduling completed. cost:%s ms !",
        System.currentTimeMillis() - context.getStartTime());
    // replace task
    SchedulerTask task = context.getTask();
    SchedulerTask oldTask = schedulerTaskService.getById(task.getId());
    if (TaskStatus.isFinished(oldTask.getStatus())) {
      context.addTraceLog(
          "The task has been completed by other threads, Current status:%s. The schedule does not change the data, only saves the log!!",
          oldTask.getStatus());
      task = oldTask;
    } else {
      task.setGmtModified(oldTask.getGmtModified());
    }

    task.setExecuteNum(oldTask.getExecuteNum() + 1);
    context.getTraceLog().insert(0, System.getProperty("line.separator"));
    task.setRemark(CommonUtils.setRemarkLimit(oldTask.getRemark(), context.getTraceLog()));
    task.setLockTime(null);

    if (schedulerTaskService.replace(task) <= 0) {
      log.error("finally replace task error task:{}", task);
      throw new OpenSPGException("finally replace task error task {}", task);
    }
  }

  public void setTaskFinish(JobTaskContext context) {
    SchedulerInstance instance = context.getInstance();
    SchedulerTask task = context.getTask();
    task.setFinishTime(new Date());

    TaskDag taskDag = instance.getTaskDag();

    List<TaskDag.Node> nextNodes = taskDag.getRelatedNodes(task.getNodeId(), true);

    if (CollectionUtils.isEmpty(nextNodes)) {
      checkAllNodesFinished(context);
      return;
    }

    for (TaskDag.Node nextNode : nextNodes) {
      startNextNode(context, taskDag, nextNode);
    }
  }

  private void startNextNode(JobTaskContext context, TaskDag taskDag, TaskDag.Node nextNode) {
    SchedulerInstance instance = context.getInstance();
    SchedulerTask task = context.getTask();

    List<TaskDag.Node> preNodes = taskDag.getRelatedNodes(nextNode.getId(), false);
    boolean allPreFinish = true;

    for (TaskDag.Node preNode : preNodes) {
      if (preNode.getId().equals(task.getNodeId())) {
        continue;
      }
      SchedulerTask preTask =
          schedulerTaskService.queryByInstanceIdAndType(task.getInstanceId(), preNode.getType());
      if (!TaskStatus.isFinished(preTask.getStatus())) {
        allPreFinish = false;
        break;
      }
    }

    if (!allPreFinish) {
      return;
    }
    SchedulerTask updateTask = new SchedulerTask();
    SchedulerTask nextTask =
        schedulerTaskService.queryByInstanceIdAndType(task.getInstanceId(), nextNode.getType());
    if (nextTask != null) {
      updateTask.setId(nextTask.getId());
    } else {
      updateTask = new SchedulerTask(instance, TaskStatus.WAIT, nextNode);
      nextTask = updateTask;
    }
    context.addTraceLog(
        "The execution of the current node is completed and subsequent nodes are triggered:%s",
        nextNode.getName());
    if (!TaskStatus.WAIT.equals(nextTask.getStatus())) {
      context.addTraceLog(
          "subsequent nodes:%s status is:%s,Only the WAIT state can be modified",
          nextNode.getName(), nextTask.getStatus());
      return;
    }
    updateTask.setStatus(TaskStatus.RUNNING);
    updateTask.setBeginTime(new Date());
    if (schedulerTaskService.replace(updateTask) <= 0) {
      task.setStatus(TaskStatus.ERROR);
      throw new OpenSPGException("replace task error task {}", updateTask);
    }
    context.setTaskFinish(true);
  }

  private boolean checkAllNodesFinished(JobTaskContext context) {
    boolean allFinish = true;
    List<SchedulerTask> taskList =
        schedulerTaskService.queryByInstanceId(context.getInstance().getId());
    for (SchedulerTask task : taskList) {
      if (task.getId().equals(task.getId())) {
        continue;
      }
      if (!TaskStatus.isFinished(task.getStatus())) {
        allFinish = false;
        break;
      }
    }
    if (allFinish) {
      setInstanceFinish(context, TaskStatus.FINISH, InstanceStatus.FINISH);
    }
    return allFinish;
  }

  public void setInstanceFinish(
      JobTaskContext context, TaskStatus taskStatus, InstanceStatus instanceStatus) {
    SchedulerInstance instance = context.getInstance();
    context.addTraceLog(
        "Complete instance,Subsequent task status will all be changed to:%s. instance status set to:%s",
        taskStatus.name(), instanceStatus.name());
    schedulerCommonService.setInstanceFinish(instance, instanceStatus, taskStatus);
  }
}
