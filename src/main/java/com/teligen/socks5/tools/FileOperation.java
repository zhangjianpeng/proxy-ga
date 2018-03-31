package com.teligen.socks5.tools;

import io.netty.buffer.ByteBuf;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributeView;

public class FileOperation {

    private static final FileOperation instance=new FileOperation();
    private File requestDir;
    private File responseDir;

    public FileOperation(){
        requestDir=createDirectory(PropertiesTool.getString("requestDir"));
        responseDir=createDirectory(PropertiesTool.getString("responseDir"));
    }

    public static FileOperation getInstance(){
        return instance;
    }
    private File createDirectory(String filePath){
        File file=new File(filePath);
        if(!file.exists()){
            boolean mkdirs = file.mkdirs();
            if(!mkdirs){
                throw new RuntimeException("Can not create directory : "+filePath);
            }
        }
        if (file.isFile()){
            throw new IllegalArgumentException(filePath+" must be directory.");
        }
        File[] files = file.listFiles();
        for(File file1:files){
            file1.delete();
        }
        return file;
    }

    /**
     * 创建请求文件，并将数据写入文件中
     * @param fileName
     * @param bytes
     */
    public void createRequestFile(String fileName, byte[] bytes) throws IOException {
        createFile(requestDir,fileName,bytes);
    }

    /**
     * 创建响应文件，并将数据写入文件
     * @param fileName
     * @param bytes
     * @throws IOException
     */
    public void createResponseFile(String fileName,byte[] bytes) throws IOException{
        createFile(responseDir,fileName,bytes);
    }
    /**
     * 创建文件，并将数据写入文件
     * @param parentDir
     * @param fileName
     * @param bytes
     */
    private void createFile(File parentDir, String fileName, byte[] bytes) throws IOException {
        File file=new File(parentDir,fileName);
        RandomAccessFile accessFile=new RandomAccessFile(file,"rw");
        FileChannel channel = accessFile.getChannel();
        FileLock fileLock = channel.lock();
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length);
        mappedByteBuffer.put(bytes);
        fileLock.release();
        channel.close();
    }

    /**
     * 从ByteBuf对象中读出数据
     * @param byteBuf
     * @return
     */
    public byte[] readBytesInByteBuf(ByteBuf byteBuf){
        byte[] bytes=new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    public byte[] readResponseFile(String fileName) throws IOException {
        File file=new File(responseDir,fileName);
        return readDataInFile(file);
    }

    public byte[] readRequestFile(String fileName) throws IOException{
        File file=new File(requestDir,fileName);
        return readDataInFile(file);
    }

    public byte[] readDataInFile(File file) throws IOException {
        if (file.exists()){
            RandomAccessFile accessFile=new RandomAccessFile(file,"rw");
            FileChannel channel = accessFile.getChannel();
            FileLock fileLock = channel.tryLock();
            if(fileLock!=null){
                byte[] bytes=new byte[(int) accessFile.length()];
                MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, accessFile.length());
                mappedByteBuffer.get(bytes);
                fileLock.release();
                channel.close();
                return bytes;
            }
        }
        return null;
    }

    /**
     * 删除目录下的创建时间超过interval毫秒的文件
     * @param dir
     * @param interval
     */
    public void removeFiles(File dir, final long interval){
        if(dir.isDirectory()){
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    BasicFileAttributeView basicview = Files.getFileAttributeView(pathname.toPath(), BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
                    try {
                        long toMillis = basicview.readAttributes().creationTime().toMillis();
                        return System.currentTimeMillis() - toMillis > interval ;
                    } catch (IOException e) {

                    }
                    return false;
                }
            });
            for(File file:files){
                file.delete();
            }
        }
    }

    public File getRequestDir() {
        return requestDir;
    }

    public File getResponseDir() {
        return responseDir;
    }
}
