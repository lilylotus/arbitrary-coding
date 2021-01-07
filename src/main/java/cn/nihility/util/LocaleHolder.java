package cn.nihility.util;


import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 语言工具类
 *

 */
public class LocaleHolder {

    /**
     * 获取当前语言
     * @return
     */
    public static String getLang() {
        return LocaleContextHolder.getLocale().toString();
    }

    /**
     * 获取当前语言
     * @return
     */
    public static Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
