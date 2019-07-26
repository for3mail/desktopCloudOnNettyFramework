package com.gb.cloud.server;

import com.gb.cloud.common.MyMsg;
import com.gb.cloud.common.OneFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private String userName;
    private byte [] data_array;
    RandomAccessFile raf;
    final int PART_SIZE = 8192;

    public ServerHandler(String userName) {
        this.userName = userName;
        System.out.println("Connection created, userName: " + userName);
        if (!Files.exists(Paths.get("C:\\Cloud\\Server\\" + userName))) {
            try {
                Files.createDirectory(Paths.get("C:\\Cloud\\Server\\" + userName));
                System.out.println("New folder created");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("user folder existed");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof OneFile) {
            OneFile oneFile = (OneFile)(msg);
            oneFile.printOneFile();


            // СШИВАЕМ ФАЙЛ !!!
            try {
                if (oneFile.partNumber == 0) {
                    raf = new RandomAccessFile("C:\\Cloud\\Server\\" + userName + "\\" + oneFile.name, "rw");
                }
                raf.write(oneFile.data, 0, oneFile.data.length);
                System.out.println("Файл " + oneFile.name + " записан");
                if (oneFile.partNumber == oneFile.numberOfParts-1) raf.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if (msg instanceof MyMsg){
            MyMsg myMsg = (MyMsg)(msg);
            executeMyMsg(myMsg, ctx);
        } else {
            System.out.println("Unknown object");
        }
    }

    public void executeMyMsg(MyMsg myMsg, ChannelHandlerContext ctx) {

        switch (myMsg.type){
            case FILE_REQUEST:
                try {
                    if (!Files.exists(Paths.get("C:\\Cloud\\Server\\" + userName + "\\" + myMsg.fileName)))
                        throw new Exception("Requested file not found");
                    OneFile oneFile = new OneFile();
                    oneFile.name = myMsg.fileName;
                    oneFile.data = Files.readAllBytes(Paths.get("C:\\Cloud\\Server\\" + userName + "\\" + myMsg.fileName));
                    ctx.writeAndFlush(oneFile);
                    System.out.println("sending file");
                } catch (Exception e) {
                    e.printStackTrace();
                };
                break;

            case REFRESH_REQUEST:
                try {
                    Files.list(Paths.get("C:\\Cloud\\Server\\" + userName + "\\")).map(p -> p.getFileName().toString()).forEach(o -> myMsg.stringListView.add(o));
                    ctx.writeAndFlush(myMsg);
                    System.out.println("Обновление отправлено");
                } catch (Exception e) {
                    System.out.println("Что то с формирование листа файлов на сервере");
                    e.printStackTrace();

                };
                break;
                default:
                    System.out.println("Неизвестный тип MyMsg");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.toString());
    }
}
