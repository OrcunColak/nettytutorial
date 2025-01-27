import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NettyManager {
    private static final Logger logger = LoggerFactory.getLogger(NettyManager.class);

    // For TCP
    private final EventLoopGroup bossGroup;
    
    // Shared between TCP and UDP
    private final EventLoopGroup workerGroup;
    
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    public NettyManager() {
      this(1,4);
    }
    public NettyManager(int bossThreads, int workerThreads) {
        bossGroup = new MultiThreadIoEventLoopGroup(bossThreads, NioHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(workerThreads, NioHandler.newFactory());
    }

    public addTcpServer(TcpServerParameters parameters) {
      boolean result = false;
      try {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHanlder(new ChannelInitializer<> () {
            @Override
            protected void initChannel(Channel channel) {
              channel.pipeline()
                addLast(parameters.handler());
            }
        });
        
        Channel channel = bootstrap.bind(parameters.port())
          .sync()
          .channel();
      channels.put(parameters.channelId(),channel);
      logger.info("TCP server started on port {}", parameters.port());
      } catch(Exception exception) {
        logger.error("Failed to start TCP Server on port {}: {}",parameters.port(), exception.getMessage());
      }
    }
  
    public void addTcpClient(TcpClientParameters parameters) {
      try {  
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                          .addLast(parameters.inboundHandler());
                    }
                });
        
        
            Channel channel = bootstrap.connect(new InetSocketAddress(parameters.host(), parameters.port()))
                    .sync()
                    .channel();
            channels.put(parameters.channelId(), channel);
            logger.info("TCP client with ID '{}' connected to {}:{}",
                    parameters.channelId(), parameters.host(), parameters.port());
        } catch (InterruptedException exception) {
            logger.error("Failed to connect TCP client with ID '{}': {}",
                    parameters.channelId(), exception.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
    }

    public boolean addUdpServer(UdpServerParameters parameters) {
        boolean result = false;

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(parameters.handler());

            Channel channel = bootstrap.bind(parameters.port()).sync().channel();
            channels.put(parameters.channelId(), channel);
            result = true;
            logger.info("UDP server with ID '{}' started on port {}", parameters.channelId(), parameters.port());
        } catch (InterruptedException exception) {
            logger.error("Failed to start UDP server with ID '{}': {}", parameters.channelId(), exception.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status
        }

        return result;
    }

  public void addUdpClient(UdpClientParameters parameters) {
    try {
      Bootstrap bootstrap = new Bootstrap();

      bootstrap.group(workerGroup)
        .channel(NioDatagramChannel.class)
        .option(ChannelOptin.SO_BROADCAST, true)
        .handler(new ChannelInitializer<DatagramChannel>() {
          @Override
          protected void initChannel(DatagramChannel channel) {
            channel.pipeline()
              .addLast(parameters.inboundHandler());
          }
        });
        // Bind to a random port for the UDP client
        Channel channel = bootstrap.bind(0)
          .sync()
          .channel();
        channels.put(parameters.channelId(), channel);
        logger.info("UDP client started with channel ID {} ", parameters.channelId());
    } catch (InterruptedException exception) {
      logger.error("Failed to start UDP server with ID '{}': {}", parameters.channelId(), exception.getMessage());
      Thread.currentThread().interrupt(); // Restore interrupt status
    }
  }

  public boolean sendTcpMessage(String channelId, byte[] message) {
    boolean channelExists = false;
    Channel channel = channels.get(channelId);
    if (channel instanceof ServersocketChannel || channel instanceof NioSocketChannel) {
      channelExists = true;
      ByteBuf byteBuf = Unpooled.wrappedBuffer(message);
      channel.writeAndFlush(byteBuf);
    }
    return channelExists;
  }
  
  public boolean sendUdpMessage(String channelId, DatagramPacket message) {
    boolean channelExists = false;
    Channel channel = channels.get(channelId);
    if (channel instanceof DatamgramChannel udpChannel) {
      channelExists = true;
      udpChannel.writeAndFlush(message);
    }
    return channelExists;
  }
  
    public boolean shutdownChannel(String channelId) {
        boolean result = false;

        Channel channel = channels.remove(channelId);
        if (channel != null) {
            try {
                channel.close().sync(); // Close the channel and wait for it to complete
                logger.info("Channel with ID {} has been shut down.", channelId);
                result = true;
            } catch (InterruptedException exception) {
                logger.error("Failed to shut down channel with ID {}: {}", channelId, exception.getMessage());
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        } else {
            logger.warn("No channel found with ID {} to shut down.", channelId);
        }

        return result;
    }

    // Shuts down the server gracefully.
    public void shutdown() {
        channels.values().forEach(Channel::close);
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
