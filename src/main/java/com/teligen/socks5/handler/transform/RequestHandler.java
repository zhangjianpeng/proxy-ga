package com.teligen.socks5.handler.transform;

import com.teligen.socks5.tools.FileOperation;
import io.netty.util.internal.ConcurrentSet;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestHandler {
    private static final Logger LOGGER=Logger.getLogger(RequestHandler.class);

    private Selector selector;

    private Set<Request> writableRequest=new ConcurrentSet<>();

    public RequestHandler() throws IOException {
        this.selector = Selector.open();
    }

    /**
     * 负责创建请求
     * @throws IOException
     */
    public void request() throws IOException {
        File[] files = getRequestFiles();
        for(File file:files){
            Request request = createRequest(file);
            writableRequest.add(request);
            file.delete();//防止多次请求
        }
    }

    private void removeFailRequest(){
        Iterator<Request> iterator = writableRequest.iterator();
        while (iterator.hasNext()){
            Request request = iterator.next();
            if(request.isFail()){
                iterator.remove();
            }
        }
    }
    /**
     * 将请求数据发送给当前请求的通道
     */
    private void writeDataToRequest() throws IOException {
        Iterator<Request> iterator = writableRequest.iterator();
        for(Request request:writableRequest){
            if(request.isReady()){
                request.writeToChannel();
            }
        }
    }

    private Request getRequest(SocketChannel channel){
        for(Request request:writableRequest){
            if(request.socketChannel.equals(channel)){
                return request;
            }
        }
        return null;
    }

    /**
     * 死循环，获取连接服务器和可读事件，并进行操作
     * @throws IOException
     */
    public void execute() throws IOException {

            int select = selector.selectNow();
            if(select>0){
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()){
                    SelectionKey selectionKey = keyIterator.next();
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    if(selectionKey.isConnectable()){
                        selectionKey.interestOps(SelectionKey.OP_READ);
                        if(channel.isConnectionPending()){
                            channel.finishConnect();
                        }
                        channel.configureBlocking(false);
                        LOGGER.debug("可以将请求数据发送给通道");
                        channel.register(selector,SelectionKey.OP_READ);
                      //  selectionKey.cancel();
                    }else
                    if(selectionKey.isReadable()){
                        LOGGER.debug("从通道读取数据");
                        Request request = getRequest(channel);
                        int interestOps = selectionKey.interestOps();
                        selectionKey.interestOps(interestOps & (~SelectionKey.OP_READ));
                        if(request!=null){
                            request.writeResponseFile();
                            selectionKey.interestOps(interestOps);
                        }
                    }
                    keyIterator.remove();
                }
            }
        writeDataToRequest();
        //removeFailRequest();
    }

    private Request createRequest(File file) throws IOException {
        String channelId = file.getName().substring(0, file.getName().length() - 2);
        FileOperation fileOperation = FileOperation.getInstance();
        byte[] bytes = fileOperation.readDataInFile(file);
        BufferedReader bufferedReader=new BufferedReader(new StringReader(new String (bytes)));
        String line;
        Map<String,String> params=new HashMap<>();
        while ((line=bufferedReader.readLine())!=null){
            String[] split = line.split("=");
            params.put(split[0].toUpperCase(),split[1]);
        }
        String destHost=params.get("HOST");
        int port=Integer.parseInt(params.get("PORT"));
        return new Request(destHost,port,channelId);
    }

    /**
     * 获取一个请求中的第一个文件集合
     * @return
     */
    private File[] getRequestFiles() {
        FileOperation fileOperation = FileOperation.getInstance();
        final Set<String> fileNames=new HashSet<>();
        for(Request request:writableRequest){
            fileNames.add(request.fileName+"_0");
        }
        return fileOperation.getRequestDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {

                return !fileNames.contains(name)&&name.lastIndexOf("_0") + 2 == name.length();
            }
        });
    }


    class Request {
        String host;
        int port;
        String fileName;
        SocketChannel socketChannel;
        AtomicInteger responseCount = new AtomicInteger(1);
        AtomicInteger requestCount = new AtomicInteger(1);

        public Request(String host, int port, String fileName) throws IOException {
            this.host = host;
            this.port = port;
            this.fileName = fileName;
            socketChannel = connectServer();
        }

        SocketChannel connectServer() throws IOException {
            LOGGER.trace("准备连接目标服务器");
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host, port));
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            return socketChannel;
        }

        void writeToChannel() throws IOException {
            FileOperation fileOperation = FileOperation.getInstance();
            File file=new File(fileOperation.getRequestDir(),fileName+"_"+requestCount.getAndIncrement());
            byte[] bytes = fileOperation.readDataInFile(file);
            if(bytes==null){
                requestCount.decrementAndGet();
            }else {
                ByteBuffer wrap = ByteBuffer.wrap(bytes);
                while (wrap.hasRemaining()) {
                    socketChannel.write(wrap);
                }
                //file.delete();
            }
        }

        byte[] readFromChannel() throws IOException {
            byte[] _10K = new byte[10240];
            ByteBuffer wrap = ByteBuffer.wrap(_10K);
            int readCount = socketChannel.read(wrap);
            if(readCount==-1){
                socketChannel.close();
                return null;
            }
            wrap.flip();
            byte[] bytes = new byte[readCount];
            wrap.get(bytes);
            return bytes;
        }

        void writeResponseFile() throws IOException {
            byte[] bytes = readFromChannel();
            if(bytes!=null){
                FileOperation fileOperation = FileOperation.getInstance();
                fileOperation.createResponseFile(fileName + "_" + responseCount.getAndIncrement(), bytes);
            }
        }

        boolean isReady(){
            boolean connected = socketChannel.isConnected();
            return connected;
        }

        public boolean isFail() {
            try {
                return !socketChannel.finishConnect();
            }catch (Exception e){
                LOGGER.error(e);
                return true;
            }
        }
    }
}
