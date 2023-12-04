package com.antgroup.openspg.server.common.model.scheduler;

/**
 * Instance Status
 *
 * @author yangjin
 * @Title: InstanceStatus.java
 * @Description:
 */
public enum InstanceStatus {
    /**
     * WAITING
     */
    WAITING,
    /**
     * running
     */
    RUNNING,
    /**
     * skip
     */
    SKIP,
    /**
     * finish
     */
    FINISH,
    /**
     * terminate
     */
    TERMINATE,
    /**
     * set finish
     */
    SET_FINISH,
    /**
     * all done
     */
    ALL_DONE;

    /**
     * status is Finish
     *
     * @param status
     * @return
     */
    public static boolean isFinish(String status) {
        return InstanceStatus.FINISH.name().equalsIgnoreCase(status) || InstanceStatus.TERMINATE.name().equalsIgnoreCase(status)
                || InstanceStatus.SET_FINISH.name().equalsIgnoreCase(status) || InstanceStatus.ALL_DONE.name().equalsIgnoreCase(status)
                || InstanceStatus.SKIP.name().equalsIgnoreCase(status);
    }
}
