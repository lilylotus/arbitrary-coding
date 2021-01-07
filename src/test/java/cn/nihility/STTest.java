package cn.nihility;

import com.oscar.jdbc.OscarStatement;

import java.sql.*;

public class STTest {
    private Connection con;
    public String DBUSER = "";
    public String DBPASSWD = "";
    public String DBURL = "";
    public String DBDRIVER = "";
    String mark = "047F000001050500000000000000";   //仅仅用于测试

    public void init() {
        //从配置文件读取
        DBUSER = "KIAM_3";
//        DBUSER = "sysdba";
        //DBUSER = prop.getProperty("DBUSER");
//        DBPASSWD = "szoscar55";
        DBPASSWD = "Aa12345678";
        //DBPASSWD = prop.getProperty("DBPASSWD");
//        DBURL = "jdbc:oscar://utools.club:36121/OSRDB?LOGLEVEL=4";
        DBURL = "jdbc:oscar://10.0.90.231:2003/osrdb";
        //DBURL = prop.getProperty("DBURL");
        DBDRIVER = "com.oscar.Driver";
        //DBDRIVER = prop.getProperty("DBDRIVER");

        try {
            Class.forName(DBDRIVER);
            System.out.println("connection begin.");
            System.out.println("DBUSER: [" + DBUSER + "]");
            System.out.println("DBPASSWD: [" + DBPASSWD + "]");
            System.out.println("URL: [" + DBURL + "]");
            System.out.println("DBDRIVER: [" + DBDRIVER + "]");
            con = DriverManager.getConnection(DBURL, DBUSER, DBPASSWD);
            System.out.println("connection success.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void execute() {
        init();
        Statement stmt;
        PreparedStatement pstmt;
        String createSql = "CREATE TABLE EMPLOYEE(ID INT,NAME VARCHAR(20))";
        String insertSql = "INSERT INTO EMPLOYEE VALUES(?,?)";
        String updateSql = "UPDATE EMPLOYEE SET NAME = ? WHERE ID = ?";
        String querySql = "SELECT ID, NAME FROM EMPLOYEE";
        String deleteSql = "DELETE FROM EMPLOYEE";
        String dropSql = "DROP TABLE EMPLOYEE";

        try {
            stmt = con.createStatement();
            ((OscarStatement) stmt).setMark(mark); //设置当前语句操作的标识。
            // 创建表
            stmt.execute(createSql);
            // 开始一个事务，将自动提交模式关闭
             con.setAutoCommit(false);
            // 插入十条记录
            pstmt = con.prepareStatement(insertSql);
            for (int i = 1; i <= 10; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "name[" + i + "]");
                pstmt.execute();
            }
            // 全部一起提交
            con.commit();


            // 更新其中的几条记录
            pstmt = con.prepareStatement(updateSql);
            ((OscarStatement) pstmt).setMark(mark); //设置当前语句操作的标识。

            int[] ids = {1, 3, 5, 9};
            for (int i = 0; i < ids.length; i++) {
                pstmt.setString(1, "name[" + (ids[i] + 10) + "]");
                pstmt.setInt(2, ids[i]);
                pstmt.executeUpdate();
                // 更新完每条记录就提交
                con.commit();
            }
            // 改变为自动提交模式
            con.setAutoCommit(true);
            // 释放资源
            pstmt.close();

            // 获取结果集
            ResultSet rs = stmt.executeQuery(querySql);
            while (rs.next()) {
                String name = rs.getString("NAME");
                int id = rs.getInt("ID");
                System.out.println("id:" + id + " name:" + name + "mark:" + mark);
            }
            // 删除所有记录
            stmt.executeUpdate(deleteSql);
            // 删除表
            stmt.executeUpdate(dropSql);
            // 释放资源
            stmt.close();
            // 释放连接
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (con != null) {
                try {
                    System.err.print("Transaction is being ");
                    System.err.println("rolled back");
                    con.rollback();
                } catch (SQLException excep) {
                    System.err.print("SQLException: ");
                    System.err.println(excep.getMessage());
                }
            }
        }
    }

    public static void main(String args[]) {
        new STTest().execute();
    }
}
