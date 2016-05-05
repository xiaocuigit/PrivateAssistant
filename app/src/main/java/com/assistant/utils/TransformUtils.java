package com.assistant.utils;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/4/4
 * <p>
 * 功能描述 :
 */
public class TransformUtils {
    /**
     * 将一周的天数转换成String类型
     *
     * @param days
     * @return
     */
    public static String getStringDayOfWeek(int[] days) {
        String dayOfWeek = "";
        for (int i = 0; i < days.length; i++) {
            int day = days[i];
            if (i == days.length - 1) {
                dayOfWeek = dayOfWeek + day;
            } else {
                // 使用逗号将天数分开
                dayOfWeek = dayOfWeek + day + ",";
            }
        }
        return dayOfWeek;
    }

    /**
     * 将 String 类型的日期转换成 数组型
     *
     * @param days
     * @return
     */
    public static int[] getIntsDayOfWeek(String days) {
        String[] dayOfWeek = days.split(",");
        int[] day = new int[dayOfWeek.length];
        for (int i = 0; i < dayOfWeek.length; i++) {
            day[i] = Integer.parseInt(dayOfWeek[i]);
        }
        return day;
    }
}
