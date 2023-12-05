/**
 * Alipay.com Inc. Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.antgroup.openspg.server.core.scheduler.service.common;

import java.util.Date;

import com.antgroup.openspg.server.common.model.scheduler.InstanceStatus;
import com.antgroup.openspg.server.common.model.scheduler.TaskStatus;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerInstance;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerJob;

/**
 * @author yangjin
 * @version : SchedulerCommonService.java, v 0.1 2023年12月04日 16:37 yangjin Exp $
 */
public interface SchedulerCommonService {

    /**
     * set Instance Finish
     *
     * @param instance
     * @param instanceStatus
     * @param taskStatus
     */
    void setInstanceFinish(SchedulerInstance instance, InstanceStatus instanceStatus, TaskStatus taskStatus);

    /**
     * generate Once Instance
     *
     * @param job
     * @return
     */
    SchedulerInstance generateOnceInstance(SchedulerJob job);

    /**
     * generate Period Instance
     *
     * @param job
     * @return
     */
    SchedulerInstance generatePeriodInstance(SchedulerJob job);

    /**
     * generate RealTime Instance
     *
     * @param job
     * @return
     */
    SchedulerInstance generateRealTimeInstance(SchedulerJob job);

    /**
     * generate Instance
     *
     * @param job
     * @param uniqueId
     * @param schedulerDate
     * @return
     */
    SchedulerInstance generateInstance(SchedulerJob job, String uniqueId, Date schedulerDate);
}