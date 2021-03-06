package io.icw.core.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;

/**
 * 日志打印级别过滤器
 * Log Printing Level Filter
 *
 * @author tag
 * 2018/12/17
 * */
public class LogFilter {
    /**
     * 通过level设置过滤器,小于该level的日志文件都打印
     * @param level
     * @return
     * */
    public ThresholdFilter getThresholdFilter(Level level){
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel(level.levelStr);
        return thresholdFilter;
    }
}
