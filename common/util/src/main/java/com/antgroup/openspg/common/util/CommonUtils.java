/**
 * Alipay.com Inc. Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.antgroup.openspg.common.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yangjin
 * @version : CommonUtils.java, v 0.1 2023年12月01日 14:19 yangjin Exp $
 */
public class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * merge two bean by discovering differences
     *
     * @param dest
     * @param orig
     * @param <M>
     * @throws Exception
     */
    public static <M> M merge(M dest, M orig) {
        if (dest == null) {
            return orig;
        }
        if (orig == null) {
            return dest;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(dest.getClass());
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                if (descriptor.getWriteMethod() == null) {
                    continue;
                }
                Object originalValue = descriptor.getReadMethod().invoke(orig);
                if (originalValue == null) {
                    continue;
                }
                descriptor.getWriteMethod().invoke(dest, originalValue);
            }
        } catch (Exception e) {
            LOGGER.error("merge bean exception", e);
        }
        return dest;
    }

    /**
     * Exception to String
     *
     * @param e
     * @return
     */
    public static String getExceptionToString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter stringWriter = null;
        PrintWriter printWriter = null;
        try {
            stringWriter = new StringWriter();
            printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
        } finally {
            close(stringWriter);
            close(printWriter);
        }
        return stringWriter.toString();
    }

    /**
     * close Closeable
     *
     * @param closeable
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOGGER.error("Unable to close ", e);
            }
        }
    }

    /**
     * Limit remark
     *
     * @param oldRemark
     * @param appendRemark
     * @return
     */
    public static String setRemarkLimit(String oldRemark, StringBuffer appendRemark) {
        return subStringToLength(appendRemark.append(oldRemark), 100000, "...");
    }

    /**
     * sub String To Length
     *
     * @param str
     * @param length
     * @param fill
     * @return
     */
    public static String subStringToLength(StringBuffer str, Integer length, String fill) {
        if (str == null) {
            return "";
        }
        if (length == null || length >= str.length()) {
            return str.toString();
        }
        if (fill == null) {
            return str.substring(0, length - 3) + "...";
        }
        return str.substring(0, length - fill.length()) + fill;
    }

    /**
     * get Cron Execution Dates By Today
     *
     * @param cron
     * @return
     */
    public static List<Date> getCronExecutionDatesByToday(String cron) {
        CronExpression expression = null;
        try {
            expression = new CronExpression(cron);
        } catch (ParseException e) {
            new RuntimeException("Cron ParseException", e);
        }
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date startDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endDate = calendar.getTime();

        if (expression.isSatisfiedBy(startDate)) {
            dates.add(startDate);
        }
        Date nextDate = expression.getNextValidTimeAfter(startDate);

        while (nextDate != null && nextDate.before(endDate)) {
            dates.add(nextDate);
            nextDate = expression.getNextValidTimeAfter(nextDate);
        }

        return dates;
    }

    /**
     * get Previous ValidTime
     *
     * @param cron
     * @param specifiedTime
     * @return
     */
    public static Date getPreviousValidTime(String cron, Date specifiedTime) {
        CronExpression expression = null;
        try {
            expression = new CronExpression(cron);
        } catch (ParseException e) {
            new RuntimeException("Cron ParseException", e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(specifiedTime);

        Date endDate = expression.getNextValidTimeAfter(expression.getNextValidTimeAfter(specifiedTime));
        Long time = 2 * specifiedTime.getTime() - endDate.getTime();

        Date start = new Date(time);
        Date nextDate = expression.getNextValidTimeAfter(start);
        Date preDate = nextDate;
        while (nextDate != null && nextDate.before(specifiedTime)) {
            preDate = nextDate;
            nextDate = expression.getNextValidTimeAfter(nextDate);
        }
        return preDate;
    }

    /**
     * content contains key
     *
     * @param content
     * @param key
     * @return
     */
    public static boolean contains(String content, String key) {
        if (StringUtils.isBlank(key)) {
            return true;
        }
        if (StringUtils.isBlank(content)) {
            return false;
        }
        if (content.contains(key)) {
            return true;
        }
        return false;
    }

    /**
     * content equals key
     *
     * @param content
     * @param key
     * @return
     */
    public static boolean equals(Object content, Object key) {
        if (key == null) {
            return true;
        }
        if (content == null) {
            return false;
        }
        if (content.equals(key)) {
            return true;
        }
        return false;
    }

    /**
     * content Date after key Date
     *
     * @param content
     * @param key
     * @return
     */
    public static boolean after(Date content, Date key) {
        if (key == null) {
            return true;
        }
        if (content == null) {
            return false;
        }
        if (content.after(key)) {
            return true;
        }
        return false;
    }

    /**
     * content Date before key Date
     *
     * @param content
     * @param key
     * @return
     */
    public static boolean before(Date content, Date key) {
        if (key == null) {
            return true;
        }
        if (content == null) {
            return false;
        }
        if (content.before(key)) {
            return true;
        }
        return false;
    }

}
