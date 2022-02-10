import com.sun.org.apache.bcel.internal.generic.Select;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 实现服务端和客户端之间数据通讯，理解NIO非阻塞网络编程机制
 */

public class NIOServer {
    public static void main(String[] args) throws Exception {
        // create ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // get selector
        Selector selector = Selector.open();

        // bind port 12000 and watch it in server
        serverSocketChannel.socket().bind(new InetSocketAddress(12000));

        // config no blocking
        serverSocketChannel.configureBlocking(false);

        // register serverSocketChannel to selector, event is OP_ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // loop for waiting client connection
        while (true) {
            if (selector.select(1000) == 0) {
                System.out.println("server wait 1000 ms, no connection...");
                continue;
            }

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("client connect success, socketChannel: " + socketChannel.hashCode());
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
                    System.out.println("from client: " + new String(buffer.array()));
                }

                keyIterator.remove();
            }

        }
    }
}
