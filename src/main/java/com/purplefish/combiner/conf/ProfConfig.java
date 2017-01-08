/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.purplefish.combiner.conf;

import java.io.*;
import java.util.Properties;

/**
 * ��ȡ����������
 *
 * @author xiaodu
 * @since 2010-6-22
 */
public class ProfConfig {

    /**
     * �����ļ���
     */
    private static final String CONFIG_FILE_NAME = "profile.properties";

    /**
     * Ĭ�ϵ������ļ�·����~/.tprofiler/profile.properties
     */
    private File DEFAULT_PROFILE_PATH = new File(System.getProperty("user.home"), "/.tprofiler/" + CONFIG_FILE_NAME);


    /**
     * ��ʼprofileʱ��
     */
    private String startProfTime;

    /**
     * ����profileʱ��
     */
    private String endProfTime;

    /**
     * log�ļ�·��
     */
    private String logFilePath;

    /**
     * method�ļ�·��
     */
    private String methodFilePath;

    /**
     * sampler�ļ�·��
     */
    private String samplerFilePath;

    /**
     * ��������ClassLoader
     */
    private String excludeClassLoader;

    /**
     * �����İ���
     */
    private String includePackageStartsWith;

    /**
     * �������İ���
     */
    private String excludePackageStartsWith;

    /**
     * ÿ��profile��ʱ
     */
    private int eachProfUseTime = -1;

    /**
     * ����profile���ʱ��
     */
    private int eachProfIntervalTime = -1;

    /**
     * ����sampler���ʱ��
     */
    private int samplerIntervalTime = -1;

    /**
     * �Ƿ�������ɼ�
     */
    private boolean needNanoTime;

    /**
     * �Ƿ����get/set����
     */
    private boolean ignoreGetSetMethod;

    /**
     * �Ƿ�������ģʽ
     */
    private boolean debugMode;

    /**
     * Socket�˿ں�����
     */
    private int port;

    /**
     * ���췽��
     */
    public ProfConfig() {

        //��ʱ�����ļ��е�debug������δ��ȡ�����ʹ��-Dtprofiler.debug=true����ȡ�����ڿ���ʱ����
        boolean debug = "true".equalsIgnoreCase(System.getProperty("tprofiler.debug"));
      /*
	   * ����˳��
	   * 1. ϵͳ����-Dprofile.properties=/path/profile.properties
	   * 2. ��ǰ�ļ����µ�profile.properties
	   * 3. �û��ļ���~/.tprofiler/profile.properties���磺/home/manlge/.tprofiler/profile.properties
	   * 4. Ĭ��jar���е�profile.properties
	   */
        String specifiedConfigFileName = System.getProperty(CONFIG_FILE_NAME);
        File configFiles[] = {
                specifiedConfigFileName == null ? null : new File(specifiedConfigFileName),
                new File(CONFIG_FILE_NAME),
                DEFAULT_PROFILE_PATH
        };

        for (File file : configFiles) {
            if (file != null && file.exists() && file.isFile()) {
                if (debug) {
                    System.out.println(String.format("load configuration from \"%s\".", file.getAbsolutePath()));
                }
                parseProperty(file);
                return;
            }
        }
        //����Ĭ������
        if (debug) {
            System.out.println(String.format("load configuration from \"%s\".", DEFAULT_PROFILE_PATH.getAbsolutePath()));
        }
        try {
            extractDefaultProfile();
            parseProperty(DEFAULT_PROFILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("error load config file " + DEFAULT_PROFILE_PATH, e);
        }

    }

    /**
     * ��ѹĬ�ϵ������ļ���~/.tprofiler/profile.properties����Ϊģ�壬�Ա��û��༭
     * @throws IOException
     */
    private void extractDefaultProfile() throws IOException {
	  /*
	   * �������stream���и��ƣ������ǲ���properties.load��save����Ҫԭ��Ϊ����2�㣺
	   * 1. ���ܣ�streamֱ�Ӹ��ƿ죬û��properties��������(�����ļ���С�������������Ժ���)
	   * 2. properties�����ע�Ͷ�ʧ�����ļ���Ϊģ���ṩ���û�������ע����Ϣ
	   */
        InputStream in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));
        OutputStream out = null;
        try {
            File profileDirectory = DEFAULT_PROFILE_PATH.getParentFile();
            if (!profileDirectory.exists()) {
                profileDirectory.mkdirs();
            }
            out = new BufferedOutputStream(new FileOutputStream(DEFAULT_PROFILE_PATH));
            byte[] buffer = new byte[1024];
            for (int len = -1; (len = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, len);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * �����û��Զ��������ļ�
     * @param path
     */
    private void parseProperty(File path) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(path)); //�����ļ�ԭʼ���ݣ�δ���б����滻

            //�������������ģ�����System.properties�������ļ�����
            Properties context = new Properties();
            context.putAll(System.getProperties());
            context.putAll(properties);

            //��������
            loadConfig(new ConfigureProperties(properties, context));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��������
     * @param properties
     */
    private void loadConfig(Properties properties) {
        String startProfTime = properties.getProperty("startProfTime");
        String endProfTime = properties.getProperty("endProfTime");
        String logFilePath = properties.getProperty("logFilePath");
        String methodFilePath = properties.getProperty("methodFilePath");
        String samplerFilePath = properties.getProperty("samplerFilePath");
        String includePackageStartsWith = properties.getProperty("includePackageStartsWith");
        String eachProfUseTime = properties.getProperty("eachProfUseTime");
        String eachProfIntervalTime = properties.getProperty("eachProfIntervalTime");
        String samplerIntervalTime = properties.getProperty("samplerIntervalTime");
        String excludePackageStartsWith = properties.getProperty("excludePackageStartsWith");
        String needNanoTime = properties.getProperty("needNanoTime");
        String ignoreGetSetMethod = properties.getProperty("ignoreGetSetMethod");
        String excludeClassLoader = properties.getProperty("excludeClassLoader");
        String debugMode = properties.getProperty("debugMode");
        String port = properties.getProperty("port");
        setPort(port == null ? 50000 : Integer.valueOf(port));
        setDebugMode("true".equalsIgnoreCase(debugMode == null ? null : debugMode.trim()));
        setExcludeClassLoader(excludeClassLoader);
        setExcludePackageStartsWith(excludePackageStartsWith);
        setEndProfTime(endProfTime);
        setIncludePackageStartsWith(includePackageStartsWith);
        setLogFilePath(logFilePath);
        setMethodFilePath(methodFilePath);
        setSamplerFilePath(samplerFilePath);
        setStartProfTime(startProfTime);
        setNeedNanoTime("true".equals(needNanoTime));
        setIgnoreGetSetMethod("true".equals(ignoreGetSetMethod));
        if (eachProfUseTime == null) {
            setEachProfUseTime(5);
        } else {
            setEachProfUseTime(Integer.valueOf(eachProfUseTime.trim()));
        }
        if (eachProfIntervalTime == null) {
            setEachProfIntervalTime(50);
        } else {
            setEachProfIntervalTime(Integer.valueOf(eachProfIntervalTime.trim()));
        }
        if (samplerIntervalTime == null) {
            setSamplerIntervalTime(10);
        } else {
            setSamplerIntervalTime(Integer.valueOf(samplerIntervalTime.trim()));
        }
    }


    /**
     * @return
     */
    public String getStartProfTime() {
        return startProfTime;
    }

    /**
     * @param startProfTime
     */
    public void setStartProfTime(String startProfTime) {
        this.startProfTime = startProfTime;
    }

    /**
     * @return
     */
    public String getEndProfTime() {
        return endProfTime;
    }

    /**
     * @param endProfTime
     */
    public void setEndProfTime(String endProfTime) {
        this.endProfTime = endProfTime;
    }

    /**
     * @return
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * @param logFilePath
     */
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * @return the methodFilePath
     */
    public String getMethodFilePath() {
        return methodFilePath;
    }

    /**
     * @param methodFilePath
     *            the methodFilePath to set
     */
    public void setMethodFilePath(String methodFilePath) {
        this.methodFilePath = methodFilePath;
    }

    /**
     * @return
     */
    public String getIncludePackageStartsWith() {
        return includePackageStartsWith;
    }

    /**
     * @param includePackageStartsWith
     */
    public void setIncludePackageStartsWith(String includePackageStartsWith) {
        this.includePackageStartsWith = includePackageStartsWith;
    }

    /**
     * @return
     */
    public int getEachProfUseTime() {
        return eachProfUseTime;
    }

    /**
     * @param eachProfUseTime
     */
    public void setEachProfUseTime(int eachProfUseTime) {
        this.eachProfUseTime = eachProfUseTime;
    }

    /**
     * @return
     */
    public int getEachProfIntervalTime() {
        return eachProfIntervalTime;
    }

    /**
     * @param eachProfIntervalTime
     */
    public void setEachProfIntervalTime(int eachProfIntervalTime) {
        this.eachProfIntervalTime = eachProfIntervalTime;
    }

    /**
     * @return
     */
    public String getExcludePackageStartsWith() {
        return excludePackageStartsWith;
    }

    /**
     * @param excludePackageStartsWith
     */
    public void setExcludePackageStartsWith(String excludePackageStartsWith) {
        this.excludePackageStartsWith = excludePackageStartsWith;
    }

    /**
     * @return
     */
    public boolean isNeedNanoTime() {
        return needNanoTime;
    }

    /**
     * @param needNanoTime
     */
    public void setNeedNanoTime(boolean needNanoTime) {
        this.needNanoTime = needNanoTime;
    }

    /**
     * @return
     */
    public boolean isIgnoreGetSetMethod() {
        return ignoreGetSetMethod;
    }

    /**
     * @param ignoreGetSetMethod
     */
    public void setIgnoreGetSetMethod(boolean ignoreGetSetMethod) {
        this.ignoreGetSetMethod = ignoreGetSetMethod;
    }

    /**
     * @param samplerFilePath
     *            the samplerFilePath to set
     */
    public void setSamplerFilePath(String samplerFilePath) {
        this.samplerFilePath = samplerFilePath;
    }

    /**
     * @param samplerIntervalTime
     *            the samplerIntervalTime to set
     */
    public void setSamplerIntervalTime(int samplerIntervalTime) {
        this.samplerIntervalTime = samplerIntervalTime;
    }

    /**
     * @return the samplerFilePath
     */
    public String getSamplerFilePath() {
        return samplerFilePath;
    }

    /**
     * @return the samplerIntervalTime
     */
    public int getSamplerIntervalTime() {
        return samplerIntervalTime;
    }

    /**
     * @return the excludeClassLoader
     */
    public String getExcludeClassLoader() {
        return excludeClassLoader;
    }

    /**
     * @param excludeClassLoader the excludeClassLoader to set
     */
    public void setExcludeClassLoader(String excludeClassLoader) {
        this.excludeClassLoader = excludeClassLoader;
    }

    /**
     * @return the debugMode
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * @param debugMode the debugMode to set
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
