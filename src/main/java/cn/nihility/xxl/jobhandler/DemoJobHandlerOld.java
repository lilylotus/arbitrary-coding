package cn.nihility.xxl.jobhandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;

/**
 * 要和 xxl-job-admin 的版本匹配，xxl-job 版本不兼容
 *  version -> 2.1.0
 */
/*@JobHandler(value="demoJobHandler")
@Component*/
public class DemoJobHandlerOld extends IJobHandler {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(DemoJobHandlerOld.class);

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("XXL-JOB, Hello World.");
        logger.info("XXL-JOB, Hello World........ [{}]", param);
		/*for (int i = 0; i < 5; i++) {
			XxlJobLogger.log("beat at:" + i);
			TimeUnit.SECONDS.sleep(2);
		}*/
        return SUCCESS;
    }

}
