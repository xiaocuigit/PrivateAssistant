package com.assistant.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.assistant.bean.Note;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/5/6
 * <p>
 * 功能描述 :
 */
public class FileUtils {
    public final static String SD_ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public final static String APP_DIR = SD_ROOT_DIR + File.separator + "MyNotes";
    public final static String BACKUP_FILE_NAME = "notes.txt";
    public final static String FILE_PATH = APP_DIR + File.separator + BACKUP_FILE_NAME;

    private void makeSureAppDirCreated() {
        if (checkSdcardStatus()) {
            mkdir(APP_DIR);
        } else {
            Logger.e("create app dir is error!");
        }
    }

    private void mkdir(String appDir) {
        if (TextUtils.isEmpty(appDir)) {
            return;
        }
        File dirFile = new File(appDir);
        if (!dirFile.exists()) {
            boolean res = dirFile.mkdir();
            if (!res) {
                Logger.e("make dir " + appDir + " error!");
            }
        }
        Logger.d("创建目录成功");
    }

    public boolean isFileExist() {
        if (TextUtils.isEmpty(FILE_PATH)) {
            return false;
        }
        File file = new File(FILE_PATH);
        return (file.exists() && file.isFile());
    }

    public boolean createFile(String filename) {
        makeSureAppDirCreated();
        return createFile(APP_DIR, filename);
    }

    private boolean createFile(String appDir, String filename) {
        File dirFile = new File(appDir);
        if (!dirFile.isDirectory()) {
            return false;
        }
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        File newFile = new File(appDir + File.separator + filename);

        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean writeNotesFile(String content) {
        return writeFile(BACKUP_FILE_NAME, content, false);
    }

    public boolean writeFile(String fileName, String content, boolean append) {
        return writeFile(APP_DIR, fileName, content, append);
    }

    private boolean writeFile(String appDir, String fileName, String content, boolean append) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        FileWriter fileWriter = null;
        try {
            String filePath = appDir + File.separator + fileName;
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content + "\n");
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }


    private boolean checkSdcardStatus() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public boolean backupSNotes(List<Note> notes) {
        createFile(BACKUP_FILE_NAME);
        StringBuilder builder = new StringBuilder();
        int num = 1;
        for (Note note : notes) {
            if (note.getIsLock().equals("false")) {
                builder.append("   未加密笔记: " + num + "\n");
                builder.append("标题: " + note.getTitle() + "\n");
                builder.append("内容: " + note.getContent() + "\n");
                if (note.getLastOprTime() != 0 && note.getLastOprTime() > note.getCreateTime()) {
                    builder.append("最后编辑时间: " + TimeUtils.getTime(note.getLastOprTime()) + "\n");
                }
                builder.append("创建时间: " + TimeUtils.getTime(note.getCreateTime()) + "\n\n");
                num++;
            }
        }
        return writeNotesFile(builder.toString());
    }
}
