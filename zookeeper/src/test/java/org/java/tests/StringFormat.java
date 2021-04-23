package org.java.tests;

public class StringFormat {
    public static void main(String[] args) {
        String s = String.format("%010d", 1);
        String s2 = String.format("%10d", 1);
        System.out.println(s);
        System.out.println(s2);
    }
}
