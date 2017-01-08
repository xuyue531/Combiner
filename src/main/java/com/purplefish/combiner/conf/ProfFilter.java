/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.purplefish.combiner.conf;

import java.util.HashSet;
import java.util.Set;

/**
 * ����������,����ע����߲�ע���Package
 *
 * @author luqi
 * @since 2010-6-23
 */
public class ProfFilter {

    /**
     * ע���Package����
     */
    private static Set<String> includePackage = new HashSet<String>();
    /**
     * ��ע���Package����
     */
    private static Set<String> excludePackage = new HashSet<String>();
    /**
     * ��ע���ClassLoader����
     */
    private static Set<String> excludeClassLoader = new HashSet<String>();

    static {
        // Ĭ�ϲ�ע���Package
        excludePackage.add("java/");// ����javax
        excludePackage.add("sun/");// ����sunw
        excludePackage.add("com/sun/");
        excludePackage.add("org/");// ����org/xml org/jboss org/apache/xerces org/objectweb/asm
        // ��ע��profile����
        excludePackage.add("com/taobao/profile");
        excludePackage.add("com/taobao/hsf");
    }

    /**
     * @param className
     */
    public static void addIncludeClass(String className) {
        String icaseName = className.toLowerCase().replace('.', '/');
        includePackage.add(icaseName);
    }

    /**
     * @param className
     */
    public static void addExcludeClass(String className) {
        String icaseName = className.toLowerCase().replace('.', '/');
        excludePackage.add(icaseName);
    }

    /**
     * @param classLoader
     */
    public static void addExcludeClassLoader(String classLoader) {
        excludeClassLoader.add(classLoader);
    }

    /**
     * �Ƿ�����Ҫע�����
     *
     * @param className
     * @return
     */
    public static boolean isNeedInject(String className) {
        String icaseName = className.toLowerCase().replace('.', '/');
        for (String v : includePackage) {
            if (icaseName.startsWith(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * �Ƿ��ǲ���Ҫע�����
     *
     * @param className
     * @return
     */
    public static boolean isNotNeedInject(String className) {
        String icaseName = className.toLowerCase().replace('.', '/');
        for (String v : excludePackage) {
            if (icaseName.startsWith(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * �Ƿ��ǲ���Ҫע����������
     *
     * @param classLoader
     * @return
     */
    public static boolean isNotNeedInjectClassLoader(String classLoader) {
        for (String v : excludeClassLoader) {
            if (classLoader.equals(v)) {
                return true;
            }
        }
        return false;
    }
}
