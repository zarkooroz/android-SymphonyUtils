
package modules.symphony.is.utils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * File handling helper class
 */
public class FileUtil {

    private static final String TAG = FileUtil.class.getCanonicalName();

    public static final int COPY = 1, MOVE = 2, DELETE = 3;

    /**
     * Scans provided file using media scanner
     *
     * @param context context to send broadcast
     * @param file    file to scan
     */
    public static void scanFile(Context context, File file) {
        //TODO: refactor this method to receive array of files to be scanned
        Uri uri = Uri.fromFile(file);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(scanFileIntent);
        MediaScannerConnection.scanFile(context,
                new String[]{
                        file.toString()
                }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d(TAG, "Scanned " + path + ":");
                        Log.d(TAG, "-> uri=" + uri);
                    }
                });
    }

    /**
     * Deletes provided file
     *
     * @param context context to get content resolver
     * @param file    file to delete
     */
    public static void deleteFile(Context context, File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");

        final int result = context.getContentResolver().delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{
                        canonicalPath
                });
        file.delete();
        scanFile(context, file);
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                context.getContentResolver().delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{
                                absolutePath
                        });
            }
        }
    }

    /**
     * Gets file path for filename
     *
     * @param filename filename
     * @return absolute path
     */
    public static String getFilePath(String filename) {
        File temp = new File(filename);

        String absolutePath = temp.getAbsolutePath();
        String filePath = absolutePath.substring(0,
                absolutePath.lastIndexOf(File.separator));

        return filePath;
    }

    /**
     * Copies file from input path to output path
     *
     * @param context    context to scan files
     * @param inputPath  source file
     * @param outputPath output file
     * @return copy successful boolean
     */
    public static boolean copy(Context context, File inputPath, File outputPath) {
        if (outputPath.exists()) {
            return false;
        }
        boolean copyDone = false;
        InputStream in;
        OutputStream out;
        try {

            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath, false);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            copyDone = true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            copyDone = false;
        } finally {
            scanFile(context, outputPath);
            scanFile(context, inputPath);
        }
        return copyDone;
    }

    /**
     * Delete file/directory in destination directory. Copy source
     * file/directory to destination directory
     *
     * @param context           context for coping file
     * @param fileToCopy        file to be copied
     * @param destinationFolder destination file
     * @return always true
     */
    public static boolean overwriteFile(Context context, File fileToCopy, File destinationFolder) {
        File destinationFile = new File(destinationFolder.getPath());
        deleteFile(context, destinationFile);
        copy(context, fileToCopy, destinationFile);
        //TODO: remove hardcoded return
        return true;
    }

}
