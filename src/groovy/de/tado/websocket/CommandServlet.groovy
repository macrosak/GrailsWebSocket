package de.tado.websocket

import org.apache.catalina.websocket.WebSocketServlet
import org.apache.catalina.websocket.StreamInbound
import java.util.concurrent.CopyOnWriteArraySet
import org.apache.catalina.websocket.MessageInbound
import org.apache.catalina.websocket.WsOutbound
import java.nio.ByteBuffer
import java.nio.CharBuffer


class CommandServlet extends WebSocketServlet {

    private final Set<TadoDevice> connections = new CopyOnWriteArraySet<TadoDevice>();

    def broadcastThread = null

    @Override
    protected StreamInbound createWebSocketInbound(String s) {
        if (!broadcastThread)
            broadcastTest()
        return new TadoDevice()

    }

    private void broadcastTest() {
        broadcastThread  = Thread.start {
            while(true) {
                sleep(5000)
                broadcast("SERVER MESSAGE")
            }

        }
    }

    private final class TadoDevice extends MessageInbound {

        private Long id = null


        @Override
        protected void onOpen(WsOutbound outbound) {
            connections.add(this)
        }

        @Override
        protected void onBinaryMessage(ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("Binary message not supported.")
        }

        @Override
        protected void onTextMessage(CharBuffer message) {
            println "TADO_${id}: ${message.toString()}"
            sendMessage("Received message from TADO_${id} '${message.toString()}'")
            processCommand(message.toString())
        }

        private void processCommand(String command) {
            String[] token = command.split()

            switch (token[0]) {
                case 'id':
                    if (!id)
                        id = token[1].toLong()
                    else
                        sendError("ID has been already set.")
                    break
            // TODO commands
                default:
                    if (!id)
                        sendError("Illegal state. Set ID first.")
            }
        }

        public void sendMessage(String message) {
            CharBuffer buffer = CharBuffer.wrap(message)
            wsOutbound.writeTextMessage(buffer)
            wsOutbound.flush()
        }

        private void sendError(Object message) {
            sendMessage("ERROR: ${message.toString()}")
        }
    }

    private void broadcast(String message) {
        for (TadoDevice connection : connections) {
            try {
                connection.sendMessage(message)
            } catch (IOException ignore) {
                // Ignore
            }
        }
    }
}
