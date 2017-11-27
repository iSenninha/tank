package cn.senninha.sserver.client;

import cn.senninha.sserver.lang.message.BaseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * 客户端
 * @author senninha on 2017年11月8日
 *
 */
public class Client {
	private int sessionId;
	private String name;
	private int line;
	private ChannelHandlerContext ctx;
	
	public int getSessionId() {
		return sessionId;
	}
	public String getName() {
		return name;
	}
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public void pushMessage(BaseMessage message) {
		ctx.writeAndFlush(message);
	}
	public void setSessionInCtx(int sessionId) {
		ctx.channel().attr(AttributeKey.valueOf("sessionId")).set(sessionId);
	}

	public Client(int sessionId, String name, ChannelHandlerContext ctx) {
		super();
		this.sessionId = sessionId;
		this.name = name;
		this.ctx = ctx;
		this.line = -1;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Client [sessionId=");
		builder.append(sessionId);
		builder.append(", name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}
