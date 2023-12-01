/**
 * Alipay.com Inc. Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.antgroup.openspg.server.core.scheduler.service.metadata.impl.local;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.antgroup.openspg.common.util.CommonUtils;
import com.antgroup.openspg.common.util.StringUtils;
import com.antgroup.openspg.server.common.model.base.Page;
import com.antgroup.openspg.server.common.model.scheduler.TaskStatus;
import com.antgroup.openspg.server.core.scheduler.model.service.SchedulerTask;
import com.antgroup.openspg.server.core.scheduler.service.metadata.SchedulerTaskService;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author yangjin
 * @version : SchedulerTaskServiceImpl.java, v 0.1 2023年11月30日 14:11 yangjin Exp $
 */
@Service
public class LocalSchedulerTaskServiceImpl implements SchedulerTaskService {

    private static ConcurrentHashMap<Long, SchedulerTask> tasks = new ConcurrentHashMap<>();

    @Override
    public Long insert(SchedulerTask record) {
        Long max = tasks.keySet().stream().max(Comparator.comparing(x -> x)).orElse(null);
        Long id = ++max;
        record.setId(id);
        record.setGmtModified(new Date());
        tasks.put(id, record);
        return id;
    }

    @Override
    public int deleteById(Long id) {
        SchedulerTask record = tasks.remove(id);
        return record == null ? 0 : 1;
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        int flag = 0;
        for (Long id : ids) {
            SchedulerTask record = tasks.remove(id);
            if (record != null) {
                flag++;
            }
        }
        return flag;
    }

    @Override
    public Long update(SchedulerTask record) {
        Long id = record.getId();
        SchedulerTask oldRecord = tasks.get(id);
        record = CommonUtils.merge(oldRecord, record);
        record.setGmtModified(new Date());
        tasks.put(id, record);
        return id;
    }

    @Override
    public Long replace(SchedulerTask record) {
        if (record.getId() == null) {
            return insert(record);
        } else {
            return update(record);
        }
    }

    @Override
    public SchedulerTask getById(Long id) {
        SchedulerTask oldTask = tasks.get(id);
        SchedulerTask task = new SchedulerTask();
        BeanUtils.copyProperties(oldTask, task);
        return task;
    }

    @Override
    public Page<List<SchedulerTask>> query(SchedulerTask record) {
        Page<List<SchedulerTask>> page = new Page<>();
        List<SchedulerTask> taskList = Lists.newArrayList();
        page.setData(taskList);
        for (Long key : tasks.keySet()) {
            boolean flag = false;
            SchedulerTask task = tasks.get(key);
            if (record.getId() != null && record.getId().equals(task.getId())) {
                flag = true;
            }
            if (StringUtils.isNotBlank(record.getCreateUser()) && record.getCreateUser().equals(task.getCreateUser())) {
                flag = true;
            }
            if (StringUtils.isNotBlank(record.getType()) && record.getType().equals(task.getType())) {
                flag = true;
            }
            if (StringUtils.isNotBlank(record.getTitle()) && StringUtils.isNotBlank(task.getTitle()) && task.getTitle().contains(
                    record.getTitle())) {
                flag = true;
            }
            if (record.getJobId() != null && record.getJobId().equals(task.getJobId())) {
                flag = true;
            }
            if (record.getInstanceId() != null && record.getInstanceId().equals(task.getInstanceId())) {
                flag = true;
            }
            if (StringUtils.isNotBlank(record.getTaskStatus()) && record.getTaskStatus().equals(task.getTaskStatus())) {
                flag = true;
            }
            if (StringUtils.isNotBlank(record.getExtension()) && StringUtils.isNotBlank(task.getExtension()) && task.getExtension().contains(
                    record.getExtension())) {
                flag = true;
            }

            if (flag) {
                SchedulerTask target = new SchedulerTask();
                BeanUtils.copyProperties(task, target);
                taskList.add(target);
            }
        }
        page.setPageNo(1);
        page.setPageSize(taskList.size());
        page.setTotal(Long.valueOf(taskList.size()));
        return page;
    }

    @Override
    public Long getCount(SchedulerTask record) {
        return query(record).getTotal();
    }

    @Override
    public List<SchedulerTask> getByIds(List<Long> ids) {
        List<SchedulerTask> taskList = Lists.newArrayList();
        for (Long id : ids) {
            SchedulerTask task = tasks.get(id);
            SchedulerTask target = new SchedulerTask();
            BeanUtils.copyProperties(task, target);
            taskList.add(target);
        }
        return taskList;
    }

    @Override
    public SchedulerTask queryByInstanceIdAndType(Long instanceId, String type) {
        for (Long key : tasks.keySet()) {
            SchedulerTask task = tasks.get(key);
            if (instanceId.equals(task.getInstanceId()) && type.equalsIgnoreCase(task.getType())) {
                SchedulerTask target = new SchedulerTask();
                BeanUtils.copyProperties(task, target);
                return target;
            }
        }
        return null;
    }

    @Override
    public List<SchedulerTask> queryByInstanceId(Long instanceId) {
        List<SchedulerTask> taskList = Lists.newArrayList();
        for (Long key : tasks.keySet()) {
            SchedulerTask task = tasks.get(key);
            if (instanceId.equals(task.getInstanceId())) {
                SchedulerTask target = new SchedulerTask();
                BeanUtils.copyProperties(task, target);
                taskList.add(target);
            }
        }
        return taskList;
    }

    @Override
    public List<SchedulerTask> queryBaseColumnByInstanceId(Long instanceId) {
        List<SchedulerTask> taskList = Lists.newArrayList();
        for (Long key : tasks.keySet()) {
            SchedulerTask task = tasks.get(key);
            if (instanceId.equals(task.getInstanceId())) {
                SchedulerTask target = new SchedulerTask();
                BeanUtils.copyProperties(task, target);
                target.setRemark(null);
                taskList.add(target);
            }
        }
        return taskList;
    }

    @Override
    public int setStatusByInstanceId(Long instanceId, TaskStatus status) {
        int flag = 0;
        for (Long key : tasks.keySet()) {
            SchedulerTask task = tasks.get(key);
            if (instanceId.equals(task.getInstanceId())) {
                task.setGmtModified(new Date());
                task.setTaskStatus(status.name());
                flag++;
            }
        }
        return flag;
    }

    @Override
    public int updateExtensionByLock(SchedulerTask record, String extension) {
        Long id = record.getId();
        SchedulerTask oldRecord = tasks.get(id);
        if (oldRecord == null) {
            return 0;
        }
        if (record.getGmtModified() != oldRecord.getGmtModified()) {
            throw new RuntimeException("modified time inconsistent,update extension failed");
        }
        oldRecord.setGmtModified(new Date());
        oldRecord.setExtension(extension);
        tasks.put(id, oldRecord);
        return 1;
    }

    @Override
    public int updateLock(Long id) {
        SchedulerTask oldRecord = tasks.get(id);
        if (oldRecord == null) {
            throw new RuntimeException(String.format("not find task id:%s", id));
        }
        if (oldRecord.getLockTime() != null) {
            throw new RuntimeException(String.format("lockTime is not nulls id:%s lockTime:%", id, oldRecord.getLockTime()));
        }
        oldRecord.setGmtModified(new Date());
        oldRecord.setLockTime(new Date());
        tasks.put(id, oldRecord);
        return 1;
    }

    @Override
    public int updateUnlock(Long id) {
        SchedulerTask oldRecord = tasks.get(id);
        if (oldRecord == null) {
            throw new RuntimeException(String.format("not find task id:%s", id));
        }
        oldRecord.setGmtModified(new Date());
        oldRecord.setLockTime(null);
        tasks.put(id, oldRecord);
        return 1;
    }
}
