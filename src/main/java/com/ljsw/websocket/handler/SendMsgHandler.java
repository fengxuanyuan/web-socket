package com.ljsw.websocket.handler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljsw.websocket.model.SendMsg;

@ServerEndpoint("/send")
@Component
public class SendMsgHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SendMsgHandler.class);

	private ObjectMapper objectMapper = new ObjectMapper();
	
	private static CopyOnWriteArraySet<SendMsgHandler> webSocketSet = new CopyOnWriteArraySet<SendMsgHandler>();
	
	private Session session;
	
	private Integer id;
	
	@OnMessage
	public void handleMessage(Session session, String message) throws IOException {
		
		SendMsg sendMsg =	objectMapper.readValue(message, SendMsg.class);
		
		Integer id = sendMsg.getId();
		String msg = sendMsg.getMsg();
		
		this.id = id;
		
		sendAll(id, msg);
	}
	
	public void sendMessage(Integer id, String message) throws IOException {
        LOGGER.info("用户: " + id + " ， 说 ：" + message);
        this.session.getBasicRemote().sendText("用户: " + id + " ， 说 ：" + message);
    }
	
	public void sendAll(Integer id,String message) throws IOException {
		for (SendMsgHandler item : webSocketSet) {
            try {
                item.sendMessage(id, message);
            } catch (IOException e) {
                continue;
            }
        }
    }
	
	@OnOpen
    public void onOpen(Session session) {
		this.session = session;
        webSocketSet.add(this);
		LOGGER.info("有新连接加入！");
    }
	
	@OnClose
    public void onClose(Session session) throws IOException {
		webSocketSet.remove(this);
		sendAll(this.id, "退出了房间！");
        LOGGER.info("有一连接关闭！");
    }
	
	@OnError
    public void onError(Session session, Throwable error) {
		LOGGER.info("发生错误", error);
        error.printStackTrace();
    }

	
}