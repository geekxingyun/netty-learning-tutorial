package com.xingyun;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qing-feng.zhao
 */
@Slf4j
public class DiscardServer {
    /**
     * 端口
     */
    private int port;

    /**
     * 构造方法
     * @param port
     */
    public DiscardServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        log.info("please visit: http://127.0.0.1:8080");
        //默认端口8080
        int port = 8080;
        //如果传入了参数
        if (args.length > 0) {
            //使用传入的参数端口
            port = Integer.parseInt(args[0]);
        }
        //启动服务器
        new com.xingyun.DiscardServer(port).run();
    }

    /**
     * @throws Exception
     */
    public void run() throws Exception {
        // 1. NioEventLoopGroup是处理I/O操作的多线程事件循环。
        // Netty为不同类型的传输提供了各种EventLoopGroup实现。
        // 在此示例中，我们正在实现服务器端应用程序，因此将使用两个NioEventLoopGroup。

        //第一个通常称为“boss”，接受传入的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        //第二个通常称为“worker”，一旦boss接受连接并将注册的连接注册给worker，便处理已接受连接的流量。
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        //使用多少个线程以及如何将它们映射到创建的通道取决于EventLoopGroup实现，甚至可以通过构造函数进行配置。
        try {

            //ServerBootstrap是设置服务器的帮助程序类。您可以直接使用Channel设置服务器。
            // 但是，请注意，这是一个繁琐的过程，在大多数情况下您无需这样做。
            ServerBootstrap b = new ServerBootstrap(); // (2)

            b.group(bossGroup, workerGroup)
                    //在这里，我们指定使用NioServerSocketChannel类，该类用于实例化新的Channel来接受传入的连接。
                    .channel(NioServerSocketChannel.class) // (3)
                    // 此处指定的处理程序将始终由新接受的Channel评估。 ChannelInitializer是一个特殊的处理程序，旨在帮助用户配置新的Channel。
                    // 您很可能希望通过添加一些处理程序（例如DiscardServerHandler）来实现新的Channel的ChannelPipeline，以实现您的网络应用程序。
                    //随着应用程序变得复杂，您可能会向管道添加更多处理程序，并最终将此匿名类提取到顶级类中。
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    //您还可以设置特定于Channel实现的参数。我们正在编写一个TCP / IP服务器，因此我们可以设置套接字选项，例如tcpNoDelay和keepAlive。
                    // 请参考ChannelOption的apidocs和特定的ChannelConfig实现，以获取有关受支持的ChannelOptions的概述。
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    //您是否注意到option()和childOption()
                    //   option()用于接受传入连接的NioServerSocketChannel。
                    //   childOption()用于父级ServerChannel接受的通道，在这种情况下为NioServerSocketChannel。
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // 剩下的就是绑定到端口并启动服务器。
            // 在这里，我们绑定到计算机中所有NIC（网络接口卡）的端口8080。
            // 现在，您可以根据需要多次调用bind()方法（使用不同的绑定地址）。
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
        //恭喜你！您刚刚在Netty上完成了第一台服务器。
    }
}
