package com.utils;

import android.os.Environment;
import android.text.TextUtils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * 保存log到安卓内存中
 */
public class Log4 {

    private static String curDate = "";
    private static DateFormat format = new SimpleDateFormat("yyyyMMdd");

    private static Logger logger;
    private static LogConfigurator logConfigurator = null;

    public static int fileCount = 31;
    private static String filePath = Environment.getExternalStorageDirectory() + File.separator + "rfid";

    //Log初始化
    public static void configLog() {
        logConfigurator = new LogConfigurator();
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setLogCatPattern("%m%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 10);
        logConfigurator.setMaxBackupSize(fileCount);
        logConfigurator.setUseLogCatAppender(true);
        logConfigurator.setUseFileAppender(true);
        configureWrap();
        logger = Logger.getLogger("rfid");

        reFiles();
    }

    //configure文件名称
    private static void configureWrap() {
        String date = format.format(new Date());
        configureWrap(date);
    }

    //configure文件名称
    private static void configureWrap(String date) {
        if (logConfigurator == null) return;
        if (TextUtils.isEmpty(date)) {
            date = format.format(new Date());
        }
        if (date == curDate) return;

        String fileName = getFileName(date);
        logConfigurator.setFileName(fileName);
        logConfigurator.configure();
        curDate = date;
        reFiles();
    }

    //生成文件名称
    private static String getFileName(String date) {
        return filePath + File.separator + "rfid." + date + ".log";
    }

    //调整文件数量
    public static void reFiles() {
        File file = new File(filePath);
        if (!file.isDirectory() || !file.exists()) return;

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".log");
            }
        };
        //取得所有的log文件
        File[] files = file.listFiles(filter);
        if (files == null ||
                files.length <= fileCount) return;

        List<String> fileNames = new ArrayList<>();
        for (File f : files) {
            fileNames.add(f.getName());
        }
        //对文件名进行排序
        Collections.sort(fileNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        //删除部分文件
        try {
            for (int i = 0; i < fileNames.size() - fileCount; i++) {
                String fileToDel = filePath + File.separator + fileNames.get(i);
                File f = new File(fileToDel);
                f.delete();
                Log4.info("删除log文件：" + fileToDel);
            }
        } catch (Exception ex) {
            Log4.error("文件删除error", ex);
        }
    }

    public static void debug(Object message) {
        if (logger == null) return;
        configureWrap();
        logger.debug(message);
    }

    public static void debug(Object message, Throwable t) {
        if (logger == null) return;
        configureWrap();
        logger.debug(message, t);
    }

    public static void info(Object message) {
        if (logger == null) return;
        configureWrap();
        logger.info(message);
    }

    public static void info(Object message, Throwable t) {
        if (logger == null) return;
        configureWrap();
        logger.info(message, t);
    }

    public static void warn(Object message) {
        if (logger == null) return;
        configureWrap();
        logger.warn(message);
    }

    public static void warn(Object message, Throwable t) {
        if (logger == null) return;
        configureWrap();
        logger.warn(message, t);
    }

    public static void error(Object message) {
        if (logger == null) return;
        configureWrap();
        logger.error(message);
    }

    public static void error(Object message, Throwable t) {
        if (logger == null) return;
        configureWrap();
        logger.error(message, t);
    }
}
