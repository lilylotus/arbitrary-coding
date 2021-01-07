package cn.nihility.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatetimeTest {

    public static void main(String[] args) throws ParseException {
        String datetime = "2020-09-01 13:20:30";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

        Date parse = sdf.parse(datetime);
        Date parse1 = sdf1.parse(datetime);

        System.out.println(parse);
        System.out.println(parse1);

    }
}
