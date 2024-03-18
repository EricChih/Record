package com.ub.gir.web.util;


import com.ub.gir.web.plugins.checkmarx.ToolPlugins;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.*;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/**
 * @author elliot
 * @version 1.0
 * @date 2023/3/28
 */
@Slf4j
public class FileUtil {

    /**
     * 檢查硬碟是否 不存在
     *
     * @param filePath 文件路径
     */
    public static boolean isDiskNonExistent(Path filePath) {
        try {
            Path rootPath = filePath.getRoot();
            boolean isNonExistent = !(Files.exists(rootPath) && Files.isDirectory(rootPath));
            if (isNonExistent) {
                String logDetails = DataUtil.preventLogForging(filePath.toString());
                log.info("Disk is not found: " + logDetails);
            }

            return isNonExistent;
        } catch (Exception e) {
            log.error("Error deleting empty folder : {}", e.getMessage(), e);
        }
        return true;
    }

    /**
     * 當資料夾不存在創建資料夾
     *
     * @param path 目錄名稱
     */
    public static void mkdir(Path path) throws IOException {
        path = path.normalize();
        if (Files.notExists(path)) {
            if (ToolPlugins.checkAuthorization("authorization")) {
                Files.createDirectory(path);
                setFilePermissions(path);
            }
        }
    }

    /**
     * 複製 單一檔案
     *
     * @param sourceFileStringPath 來源
     * @param targetFileStringPath 目標
     */
    public static void copyFile(String sourceFileStringPath, String targetFileStringPath) throws IOException {
        Path sourceFilePath = Paths.get(sourceFileStringPath).normalize();
        Path targetFilePath = Paths.get(targetFileStringPath).normalize();
        setFilePermissions(sourceFilePath);
        setFilePermissions(targetFilePath);
        if (isDiskNonExistent(sourceFilePath)) return;
        if (isDiskNonExistent(targetFilePath)) return;

        String logDetails = DataUtil.preventLogForging(sourceFilePath.toString());
        if (Files.notExists(sourceFilePath)) {
            throw new RuntimeException("Source file not found: " + sourceFilePath);
        }

        mkdir(targetFilePath.getParent());
        if (ToolPlugins.checkAuthorization("authorization")) {
            Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("File copied successfully: " + logDetails);
    }

    /**
     * 複製 資料夾內所有檔案
     *
     * @param sourceFolderStringPath 來源
     * @param targetFolderStringPath 目標
     * @param fileName               檔案名稱
     */
    public static void copyFolder(String sourceFolderStringPath, String targetFolderStringPath, String fileName) throws IOException {
        Path sourceFolderPath = Paths.get(sourceFolderStringPath).normalize();
        Path targetFolderPath = Paths.get(targetFolderStringPath).normalize();
        setFilePermissions(sourceFolderPath);
        setFilePermissions(targetFolderPath);
        if (isDiskNonExistent(sourceFolderPath)) return;
        if (isDiskNonExistent(targetFolderPath)) return;

        File[] files = walkFiles(sourceFolderPath);
        for (File file : files) {
            if (file.isFile() && file.getName().equalsIgnoreCase(fileName)) {
                try {
                    Path sPath = file.toPath().normalize();
                    Path tPath = targetFolderPath.resolve(file.getName()).normalize();
                    Files.copy(sPath, tPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("FolderFile copy to " + targetFolderPath + " successfully");
                } catch (IOException e) {
                    throw new RuntimeException("FolderFile copy to Failed: " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * 搬移 單一檔案
     *
     * @param sourceFileStringPath 來源
     * @param targetFileStringPath 目標
     */
    public static void moveFile(String sourceFileStringPath, String targetFileStringPath) throws IOException {
        Path sourceFilePath = Paths.get(sourceFileStringPath).normalize();
        Path targetFilePath = Paths.get(targetFileStringPath).normalize();
        setFilePermissions(sourceFilePath);
        setFilePermissions(targetFilePath);
        if (isDiskNonExistent(sourceFilePath)) return;
        if (isDiskNonExistent(targetFilePath)) return;

        String logDetails = DataUtil.preventLogForging(sourceFilePath.toString());
        if (Files.notExists(sourceFilePath)) {
            throw new RuntimeException("Source file not found: " + sourceFilePath);
        }

        mkdir(targetFilePath.getParent());
        Files.move(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        deleteEmptyFolder(sourceFilePath.getParent());
        log.info("File moved successfully: " + logDetails);
    }

    /**
     * 搬移 資料夾內所有檔案
     *
     * @param sourceFolderStringPath 來源
     * @param targetFolderStringPath 目標
     */
    public static void moveFolderByFileName(String sourceFolderStringPath, String targetFolderStringPath) throws IOException {
        Path sourceFolderPath = Paths.get(sourceFolderStringPath).normalize();
        Path targetFolderPath = Paths.get(targetFolderStringPath).normalize();
        setFilePermissions(sourceFolderPath);
        setFilePermissions(targetFolderPath);
        if (isDiskNonExistent(sourceFolderPath)) return;
        if (isDiskNonExistent(targetFolderPath)) return;
        mkdir(targetFolderPath);

        File[] files = walkFiles(sourceFolderPath);
        for (File file : files) {
            if (file.isFile()) {
                try {
                    Path sPath = file.toPath().normalize();
                    Path tPath = targetFolderPath.resolve(file.getName()).normalize();
                    Files.move(sPath, tPath, StandardCopyOption.REPLACE_EXISTING);
                    deleteEmptyFolder(targetFolderPath);
                    log.info("FolderFile moved to " + targetFolderPath + " successfully");
                } catch (IOException e) {
                    throw new RuntimeException("FolderFile moved to Failed: " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * 強制刪除資料夾+檔案
     *
     * @param directory  欲刪資料夾+檔案
     * @param contentLog 檔案紀錄
     */
    public static void forceDeleteFolder(String directory, StringBuilder contentLog) throws IOException {
        try {
            Path path = Paths.get(directory).normalize();
            setFilePermissions(path);

            AtomicInteger deleteCount = new AtomicInteger(0);
            try (Stream<Path> paths = Files.walk(path)) {
                paths.sorted((p1, p2) -> -p1.compareTo(p2))
                        .forEach(p -> {
                            if (!p.equals(path)) {
                                try {
                                    if (Files.isRegularFile(p)) {
                                        deleteCount.incrementAndGet();
                                    }
                                    Files.delete(p);
                                    contentLog.append("File/Folder deleted: ").append(p).append("\n");
                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to delete file/folder " + p, e);
                                }
                            }
                        });
            }
            contentLog.append("\nSuccessfully deleted number of items: ").append(deleteCount.get());
        } catch (SecurityException e) {
            log.error("Security occurs when accessing directory : {}", e.getMessage(), e);
        }
    }

    /**
     * 删除 單一檔案
     *
     * @param directory 要删除的文件夹路径
     */
    public static void deleteFile(String directory) throws IOException {
        Path path = Paths.get(directory).normalize();
        setFilePermissions(path);
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                deleteEmptyFolder(path.getParent());
                log.info("File deleted successfully");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }

    /**
     * 删除空文件夹
     *
     * @param path 要删除的文件夹路径
     */
    public static void deleteEmptyFolder(Path path) throws IOException {
        path = path.normalize();
        try {
            if (ToolPlugins.checkAuthorization("authorization")) {
                File[] sourceFiles = walkFiles(path);
                if (sourceFiles.length == 0) {
                    boolean isDeleted = path.toFile().delete();
                    log.info("DeleteEmptyFolder is " + isDeleted);
                }
            }
        } catch (Exception e) {
            log.error("Error deleting empty folder : {}", e.getMessage(), e);
        }
    }

    /**
     * 輸出 file.txt
     *
     * @param path    檔案路径
     * @param content 檔案內容
     */
    public static void writeToFile(Path path, String content) {
        try {
            Files.write(path, content.getBytes());
        } catch (SecurityException e) {
            log.error("Security occurs when accessing directory ： {}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error occurred while writing the file ：{}", e.getMessage(), e);
        }
    }

    // 避免檔案名稱有路徑遍歷，防止Relative Path Traversal風險，像是 ../../etc/test.txt 需要過濾成 test.txt
    public static String sanitizePathTraversal(String fileName) {
        try {
            if (fileName.contains("/")) {
                int lastIndex = fileName.lastIndexOf("/");
                if (lastIndex != -1) {
                    fileName = fileName.substring(lastIndex + 1);
                }
            }
            return fileName;
        } catch (SecurityException e) {
            log.error("Security occurs when accessing directory： " + e.getMessage());
            throw e;
        }
    }

    /**
     * 根據source路徑遍歷以下所有檔案
     */
    public static File[] walkFiles(Path sourceFolderPath) throws IOException {
        try (Stream<Path> pathStream = Files.walk(sourceFolderPath, Integer.MAX_VALUE)) {
            return pathStream.filter(path -> !path.equals(sourceFolderPath))
                    .map(Path::toFile)
                    .toArray(File[]::new);
        }
    }

    /**
     * 將檔案設定ACL權限()
     */
    public static void setFilePermissions(Path path) throws IOException {
        if (Files.exists(path)) {
            AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
            log.info("UserPrincipal!!!: {}", System.getProperty("user.name"));
            log.info("UserPrincipalgetenv!!!!!: {}", System.getenv("USERNAME"));
            UserPrincipal user = path.getFileSystem().getUserPrincipalLookupService()
                    .lookupPrincipalByName(System.getProperty("user.name"));
            AclEntry entry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(user)
                    .setPermissions(EnumSet.of(AclEntryPermission.READ_DATA, AclEntryPermission.EXECUTE))
                    .build();
            List<AclEntry> aclEntries = aclView.getAcl();
            aclEntries.add(0, entry);
            aclView.setAcl(aclEntries);
        }
    }
}