package cn.itcast.demo;

import org.csource.fastdfs.*;

public class Test {

    public static void main(String[] args) throws Exception {
        ClientGlobal.init("D:\\shanghai34\\fastDFS\\src\\main\\resources\\fdfs_client.conf");

        TrackerClient trackerClient = new TrackerClient();

        TrackerServer trackerServer = trackerClient.getConnection();

        StorageServer storageServer = null;

        StorageClient storageClient = new StorageClient(trackerServer,storageServer);

        String[] ss = storageClient.upload_file("G:\\2021.7.7\\图片\\2.소프모드 보상\\1.jpg", "jpg", null);

        for(String s:ss){
            System.out.println(s);
        }
    }
}
