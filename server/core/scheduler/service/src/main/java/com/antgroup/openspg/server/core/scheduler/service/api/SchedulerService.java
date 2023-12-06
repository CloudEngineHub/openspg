/**
 * Alipay.com Inc. Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.antgroup.openspg.server.core.scheduler.service.api;

import java.util.List;

import com.antgroup.openspg.server.common.model.base.Page;
import com.antgroup.openspg.server.core.scheduler.model.query.SchedulerInstanceQuery;
import com.antgroup.openspg.server.core.scheduler.model.query.SchedulerJobQuery;
import com.antgroup.openspg.server.core.scheduler.model.query.SchedulerTaskQuery;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerInstance;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerJob;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerTask;

/**
 * @author yangjin
 * @version : SchedulerService.java, v 0.1 2023年11月30日 13:50 yangjin Exp $
 */
public interface SchedulerService {
    /**
     * submit job
     *
     * @param job
     * @return
     */
    SchedulerJob submitJob(SchedulerJob job);

    /**
     * execute Job
     *
     * @param id
     * @return
     */
    Boolean executeJob(Long id);

    /**
     * online Job
     *
     * @param id
     * @return
     */
    Boolean onlineJob(Long id);

    /**
     * offline Job
     *
     * @param id
     * @return
     */
    Boolean offlineJob(Long id);

    /**
     * delete Job
     *
     * @param id
     * @return
     */
    Boolean deleteJob(Long id);

    /**
     * update Job
     *
     * @param job
     * @return
     */
    boolean updateJob(SchedulerJob job);

    /**
     * get Job By id
     *
     * @param id
     * @return
     */
    SchedulerJob getJobById(Long id);

    /**
     * search Jobs
     *
     * @param query
     * @return
     */
    Page<List<SchedulerJob>> searchJobs(SchedulerJobQuery query);

    /**
     * get Instance By id
     *
     * @param id
     * @return
     */
    SchedulerInstance getInstanceById(Long id);

    /**
     * stop Instance
     *
     * @param id
     * @return
     */
    Boolean stopInstance(Long id);

    /**
     * set Finish Instance
     *
     * @param id
     * @return
     */
    Boolean setFinishInstance(Long id);

    /**
     * reRun Instance
     *
     * @param id
     * @return
     */
    Boolean reRunInstance(Long id);

    /**
     * trigger Instance
     *
     * @param id
     * @return
     */
    Boolean triggerInstance(Long id);

    /**
     * search Instances
     *
     * @param query
     * @return
     */
    Page<List<SchedulerInstance>> searchInstances(SchedulerInstanceQuery query);

    /**
     * search Tasks
     *
     * @param query
     * @return
     */
    Page<List<SchedulerTask>> searchTasks(SchedulerTaskQuery query);
}
