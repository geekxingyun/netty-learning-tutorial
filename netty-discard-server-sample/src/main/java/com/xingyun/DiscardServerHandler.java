package com.xingyun;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * 1.DiscardServerHandler扩展ChannelInboundHandlerAdapter，它是ChannelInboundHandler的实现。
 * ChannelInboundHandler提供了可以覆盖的各种事件处理程序方法。 目前，仅扩展ChannelInboundHandlerAdapter即可，而不是自己实现处理程序接口。
 * 继承关系如下所示: com.xingyun.DiscardServerHandler extends ChannelInboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelInboundHandler
 * @author qing-feng.zhao
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {//1
    /**
     * 2.我们在这里重写channelRead（）事件处理程序方法。
     * 每当从客户端接收到新数据时，就会使用接收到的消息来调用此方法。 在此示例中，接收到的消息的类型为ByteBuf。
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {//2
        // Discard the received data silently.
        // 3. 为了实现DISCARD协议，处理程序必须忽略收到的消息。
        //  ByteBuf是一个引用计数的对象，必须通过release（）方法显式释放它。
        // 请记住，释放任何传递给处理程序的引用计数对象是处理程序的责任。 通常，channelRead（）处理程序方法的实现方式如下：
        //@Override
        //public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //    try {
        //        // Do something with msg
        //    } finally {
        //        ReferenceCountUtil.release(msg);
        //    }
        //}
       // ((ByteBuf) msg).release(); // (3)

        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) { // (1)
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }

//        ctx.write(msg); // (1)
//        ctx.flush(); // (2)

        //ctx.writeAndFlush(msg);
    }

    /**
     * 4. 当Netty因I/O错误而引发异常时，或者由于处理事件时引发异常而由处理程序实现引发异常时，将使用Throwable调用exceptionCaught（）事件处理程序方法。
     *    在大多数情况下，应该记录捕获到的异常，并在此处关闭其关联的通道，尽管此方法的实现可能会有所不同，具体取决于您要处理特殊情况时要采取的措施。
     *    例如，您可能想在关闭连接之前发送带有错误代码的响应消息。
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
