package com.gb.cloud.server;

import com.gb.cloud.common.MyMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (authorized(msg)){
            MyMsg myMsg = (MyMsg) (msg);
            ctx.pipeline().addLast(new ServerHandler(myMsg.login));
            System.out.println("authorized");
            ctx.pipeline().remove(this);
        }


    }

    private boolean authorized(Object msg){
        //if (!(msg.getClass() == MyMsg.class)) return false;
        if (!(msg instanceof MyMsg)) return false;
        MyMsg myMsg = (MyMsg)(msg);
        if (myMsg.login.equals("user1") && myMsg.password.equals("pass1")) return true;
        if (myMsg.login.equals("user2") && myMsg.password.equals("pass2")) return true;
        if (myMsg.login.equals("user3") && myMsg.password.equals("pass3")) return true;
        if (myMsg.login.equals("user4") && myMsg.password.equals("pass4")) return true;
        return false;
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
