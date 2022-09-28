package cn.nihility.local.schedule.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuanzx
 * @date 2022/09/26 14:25
 */
@ConfigurationProperties(prefix = QuartzSchedulerProperties.PREFIX)
@Getter
@Setter
public class QuartzSchedulerProperties {

    public static final String PREFIX = "quartz.scheduler";

    private List<QuartzScheduleJob> jobs = new ArrayList<>();

    private String threadCount = "10";

}
