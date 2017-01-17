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
    private static final String CONFIG_FILE_NAME = "combiner.properties";

    /**
     * Ĭ�ϵ������ļ�·����~/.combiner/profile.properties
     */
    private File DEFAULT_PROFILE_PATH = new File(System.getProperty("user.home"), "/.combiner/" + CONFIG_FILE_NAME);

    private int rate;
    private int capacity;
    private int effective;
    private int cyclicalTaskPeriod;
    private int combinePool;

    /**
     * ���췽��
     */
    public ProfConfig() {

        //��ʱ�����ļ��е�debug������δ��ȡ�����ʹ��-Dcombiner.debug=true����ȡ�����ڿ���ʱ����
        boolean debug = "true".equalsIgnoreCase(System.getProperty("combiner.debug"));
      /*
	   * ����˳��
	   * 1. ϵͳ����-Dcombiner.properties=/path/combiner.properties
	   * 2. ��ǰ�ļ����µ�combiner.properties
	   * 3. �û��ļ���~/.combiner/combiner.properties���磺/home/xuyue/.combiner/combiner.properties
	   * 4. Ĭ��jar���е�combiner.properties
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
            loadConfig(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��������
     * @param properties
     */
    private void loadConfig(Properties properties) {
        int rate = Integer.parseInt(properties.getProperty("rate"));
        int capacity = Integer.parseInt(properties.getProperty("capacity"));
        int effective = Integer.parseInt(properties.getProperty("effective"));
        int cyclicalTaskPeriod = Integer.parseInt(properties.getProperty("cyclicalTaskPeriod"));
        int combinePool = Integer.parseInt(properties.getProperty("combinePool"));

        setRate(rate);
        setCapacity(capacity);
        setEffective(effective);
        setCyclicalTaskPeriod(cyclicalTaskPeriod);
        setCombinePool(combinePool);
    }

    public static String getConfigFileName() {
        return CONFIG_FILE_NAME;
    }

    public File getDEFAULT_PROFILE_PATH() {
        return DEFAULT_PROFILE_PATH;
    }

    public void setDEFAULT_PROFILE_PATH(File DEFAULT_PROFILE_PATH) {
        this.DEFAULT_PROFILE_PATH = DEFAULT_PROFILE_PATH;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getEffective() {
        return effective;
    }

    public void setEffective(int effective) {
        this.effective = effective;
    }

    public int getCyclicalTaskPeriod() {
        return cyclicalTaskPeriod;
    }

    public void setCyclicalTaskPeriod(int cyclicalTaskPeriod) {
        this.cyclicalTaskPeriod = cyclicalTaskPeriod;
    }

    public int getCombinePool() {
        return combinePool;
    }

    public void setCombinePool(int combinePool) {
        this.combinePool = combinePool;
    }
}
