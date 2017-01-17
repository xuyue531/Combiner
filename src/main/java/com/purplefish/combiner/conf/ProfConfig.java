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
 * 读取并保存配置
 *
 * @author xiaodu
 * @since 2010-6-22
 */
public class ProfConfig {

    /**
     * 配置文件名
     */
    private static final String CONFIG_FILE_NAME = "combiner.properties";

    /**
     * 默认的配置文件路径，~/.combiner/profile.properties
     */
    private File DEFAULT_PROFILE_PATH = new File(System.getProperty("user.home"), "/.combiner/" + CONFIG_FILE_NAME);

    private int rate;
    private int capacity;
    private int effective;
    private int cyclicalTaskPeriod;
    private int combinePool;

    /**
     * 构造方法
     */
    public ProfConfig() {

        //此时配置文件中的debug参数还未读取，因此使用-Dcombiner.debug=true来读取，用于开发时调试
        boolean debug = "true".equalsIgnoreCase(System.getProperty("combiner.debug"));
      /*
	   * 查找顺序：
	   * 1. 系统参数-Dcombiner.properties=/path/combiner.properties
	   * 2. 当前文件夹下的combiner.properties
	   * 3. 用户文件夹~/.combiner/combiner.properties，如：/home/xuyue/.combiner/combiner.properties
	   * 4. 默认jar包中的combiner.properties
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
        //加载默认配置
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
     * 解压默认的配置文件到~/.tprofiler/profile.properties，作为模板，以便用户编辑
     * @throws IOException
     */
    private void extractDefaultProfile() throws IOException {
	  /*
	   * 这里采用stream进行复制，而不是采用properties.load和save，主要原因为以下2点：
	   * 1. 性能，stream直接复制快，没有properties解析过程(不过文件较小，解析开销可以忽略)
	   * 2. properties会造成注释丢失，该文件作为模板提供给用户，包含注释信息
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
     * 解析用户自定义配置文件
     * @param path
     */
    private void parseProperty(File path) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(path)); //配置文件原始内容，未进行变量替换

            //变量查找上下文，采用System.properties和配置文件集合
            Properties context = new Properties();
            context.putAll(System.getProperties());
            context.putAll(properties);

            //加载配置
            loadConfig(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载配置
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
