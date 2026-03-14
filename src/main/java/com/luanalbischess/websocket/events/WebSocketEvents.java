package com.luanalbischess.websocket.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.luanalbischess.websocket.session.ConnectionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEvents {

	private final ConnectionRegistry registry;

	@EventListener
	public void onConnect(SessionConnectedEvent event) {
		String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
		log.info("Nova conexão (session {})", sessionId);
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		registry.unregister(sessionId);
		log.info("Jogador desconectado (session {})", sessionId);
	}
}
