package com.example;

import java.util.Calendar;

public class MyClass {
    public static void main(String args[]) {
        Calendar calendar = Calendar.getInstance();
        System.out.println("hour is " + calendar.get(Calendar.HOUR_OF_DAY));
        System.out.println("minute is " + Calendar.MINUTE);
    }
}
