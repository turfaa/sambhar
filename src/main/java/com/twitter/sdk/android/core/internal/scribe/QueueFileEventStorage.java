package com.twitter.sdk.android.core.internal.scribe;

import android.content.Context;
import com.twitter.sdk.android.core.internal.CommonUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueueFileEventStorage implements EventsStorage {
    private final Context context;
    private QueueFile queueFile = new QueueFile(this.workingFile);
    private File targetDirectory;
    private final String targetDirectoryName;
    private final File workingDirectory;
    private final File workingFile;

    public QueueFileEventStorage(Context context, File file, String str, String str2) throws IOException {
        this.context = context;
        this.workingDirectory = file;
        this.targetDirectoryName = str2;
        this.workingFile = new File(this.workingDirectory, str);
        createTargetDirectory();
    }

    private void createTargetDirectory() {
        this.targetDirectory = new File(this.workingDirectory, this.targetDirectoryName);
        if (!this.targetDirectory.exists()) {
            this.targetDirectory.mkdirs();
        }
    }

    public void add(byte[] bArr) throws IOException {
        this.queueFile.add(bArr);
    }

    public int getWorkingFileUsedSizeInBytes() {
        return this.queueFile.usedBytes();
    }

    public void rollOver(String str) throws IOException {
        this.queueFile.close();
        move(this.workingFile, new File(this.targetDirectory, str));
        this.queueFile = new QueueFile(this.workingFile);
    }

    private void move(File file, File file2) throws IOException {
        Closeable fileInputStream;
        Throwable th;
        Closeable closeable = null;
        try {
            OutputStream moveOutputStream;
            fileInputStream = new FileInputStream(file);
            try {
                moveOutputStream = getMoveOutputStream(file2);
            } catch (Throwable th2) {
                th = th2;
                CommonUtils.closeOrLog(fileInputStream, "Failed to close file input stream");
                CommonUtils.closeOrLog(closeable, "Failed to close output stream");
                file.delete();
                throw th;
            }
            try {
                CommonUtils.copyStream(fileInputStream, moveOutputStream, new byte[1024]);
                CommonUtils.closeOrLog(fileInputStream, "Failed to close file input stream");
                CommonUtils.closeOrLog(moveOutputStream, "Failed to close output stream");
                file.delete();
            } catch (Throwable th3) {
                Throwable th4 = th3;
                closeable = moveOutputStream;
                th = th4;
                CommonUtils.closeOrLog(fileInputStream, "Failed to close file input stream");
                CommonUtils.closeOrLog(closeable, "Failed to close output stream");
                file.delete();
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            fileInputStream = null;
            CommonUtils.closeOrLog(fileInputStream, "Failed to close file input stream");
            CommonUtils.closeOrLog(closeable, "Failed to close output stream");
            file.delete();
            throw th;
        }
    }

    public OutputStream getMoveOutputStream(File file) throws IOException {
        return new FileOutputStream(file);
    }

    public File getWorkingDirectory() {
        return this.workingDirectory;
    }

    public File getRollOverDirectory() {
        return this.targetDirectory;
    }

    public List<File> getBatchOfFilesToSend(int i) {
        ArrayList arrayList = new ArrayList();
        for (Object add : this.targetDirectory.listFiles()) {
            arrayList.add(add);
            if (arrayList.size() >= i) {
                break;
            }
        }
        return arrayList;
    }

    public void deleteFilesInRollOverDirectory(List<File> list) {
        for (File file : list) {
            CommonUtils.logControlled(this.context, String.format("deleting sent analytics file %s", new Object[]{file.getName()}));
            file.delete();
        }
    }

    public List<File> getAllFilesInRollOverDirectory() {
        return Arrays.asList(this.targetDirectory.listFiles());
    }

    public void deleteWorkingFile() {
        try {
            this.queueFile.close();
        } catch (IOException unused) {
        }
        this.workingFile.delete();
    }

    public boolean isWorkingFileEmpty() {
        return this.queueFile.isEmpty();
    }

    public boolean canWorkingFileStore(int i, int i2) {
        return this.queueFile.hasSpaceFor(i, i2);
    }
}
