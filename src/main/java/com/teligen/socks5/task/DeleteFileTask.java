package com.teligen.socks5.task;

import com.teligen.socks5.tools.FileOperation;

import java.io.File;

public class DeleteFileTask implements Runnable{
    @Override
    public void run() {
        FileOperation fileOperation = FileOperation.getInstance();
        File requestDir = fileOperation.getRequestDir();
        File responseDir = fileOperation.getResponseDir();
        fileOperation.removeFiles(requestDir,180000);
        fileOperation.removeFiles(responseDir,180000);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {


        }
    }
}
