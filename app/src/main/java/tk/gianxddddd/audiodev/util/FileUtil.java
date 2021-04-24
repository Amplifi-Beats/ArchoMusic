package tk.gianxddddd.audiodev.util;

import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    public static String readFile(String path) {
        File file = new File(path);
        StringBuilder stringBuilder = new StringBuilder();
        FileReader fileReader;

        try {
            fileReader = new FileReader(file);

            char[] buffer = new char[1024];
            int length;

            while ((length = fileReader.read(buffer)) > 0) {
                stringBuilder.append(new String(buffer, 0, length));
            }

            fileReader.close();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public static void writeStringToFile(String path, String content) {
        File file = new File(path);

        if (!FileUtil.doesExists(path)) {
            FileUtil.createFile(path);
        }

        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file, false);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void createFile(String path) {
        File newFile = new File(path);

        try {
            if (!newFile.getParentFile().exists()) {
                createDirectory(newFile.getParentFile().toString());
            }

            if (!newFile.exists()) {
                newFile.createNewFile();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void createDirectory(String path) {
        File newDirectory = new File(path);

        if (!newDirectory.exists()) {
            try {
                newDirectory.mkdirs();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void renameFile(String originPath, String newPath) {
        // Method not implemented yet.
    }

    public static void deleteFile(String path) {
        File file = new File(path);

        if (file.isFile()) {
            file.delete();
        } else {
            throw new RuntimeException("Unable to delete file " + path);
        }
    }

    public static String getFileExtension(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return extension;
    }

    public static String getMimeType(String path) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return type;
    }

    public static String getExternalStorageDir() {
        /* This method is deprecated on API 29. */
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getPackageDir(Context context) {
        return context.getExternalFilesDir(null).getAbsolutePath();
    }

    public static boolean doesExists(String path) {
        File file = new File(path);

        return file.exists();
    }

    public static boolean isFile(String path) {
        File file = new File(path);

        return file.isFile();
    }

    public static boolean isDirectory(String path) {
        File directory = new File(path);

        return directory.isDirectory();
    }
}
