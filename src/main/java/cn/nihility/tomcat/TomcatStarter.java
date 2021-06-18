package cn.nihility.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TomcatStarter {

    private static final int TOMCAT_STARTER_PORT = 52000;
    private static final String TOMCAT_HOST_NAME = "10.0.41.80";
    private static final String TOMCAT_WEBAPP_PATH = "src/main";

    public static void main(String[] args) throws LifecycleException {
        final Tomcat tomcat = new Tomcat();

        tomcat.setPort(TOMCAT_STARTER_PORT);
        tomcat.setHostname(TOMCAT_HOST_NAME);
        // 保存信息的目录, 创建临时目录
        String basePath = TomcatStarter.class.getResource("").getPath();
        tomcat.setBaseDir(basePath);

        StandardContext webappContext = (StandardContext) tomcat.addWebapp("/tomcat", basePath);

        /*
         * true：相关 classes | jar 修改时，会重新加载资源，不过资源消耗很大
         *
         */
        webappContext.setReloadable(false);
        // 上下文监听器
        AprLifecycleListener lifecycleListener = new AprLifecycleListener();
        webappContext.addLifecycleListener(lifecycleListener);

        // 注册 servlet
        tomcat.addServlet(webappContext, "servlet_demo", new ServletDemo());
        // servlet mapping
        webappContext.addServletMappingDecoded("/servlet", "servlet_demo");

        tomcat.getConnector();
        tomcat.start();
        tomcat.getServer().await();

    }

    static class ServletDemo extends HttpServlet {
        private static final long serialVersionUID = 1234806754838411706L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("Hello! Access Success!");
        }
    }

}
