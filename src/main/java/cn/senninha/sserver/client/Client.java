package cn.senninha.sserver.client;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.senninha.game.map.Grid;
import cn.senninha.game.map.GridStatus;
import cn.senninha.game.map.MapGround;
import cn.senninha.game.map.Steps;
import cn.senninha.game.map.manager.MapHelper;
import cn.senninha.sserver.lang.message.BaseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * 客户端, AI也功用这个--
 * 
 * @author senninha on 2017年11月8日
 *
 */
public class Client {
	private Logger logger = LoggerFactory.getLogger(Client.class);
	private int sessionId;
	private String name;
	private int line;
	private ChannelHandlerContext ctx;
	private MapGround mapGround;
	private List<Steps> steps = new LinkedList<>();
	private int speed;
	private int x;
	private int y;
	private long fireTime;
	private int fireIntervel;
	private int canBeFire;
	private int direction;


	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * 是否还存活
	 * @return
	 */
	public boolean alive() {
		return (canBeFire) > 0;
	}
	
	/**
	 * 挨了一炮，如果还活着是true，gg了是false
	 * @return
	 */
	public boolean beFire() {
		canBeFire--;
		return alive();
	}
	
	

	public int getCanBeFire() {
		return canBeFire;
	}

	public void setCanBeFire(int canBeFire) {
		this.canBeFire = canBeFire;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	/**
	 * 每10ms走过的像素！！！！
	 * @return
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * 每10ms走过的像素
	 * @param speed
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

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
		if(ctx != null) {
			ctx.writeAndFlush(message);
		}else {
//			logger.error("已经掉线:{}", this.getName());
		}
	}

	public void setSessionInCtx(int sessionId) {
		ctx.channel().attr(AttributeKey.valueOf("sessionId")).set(sessionId);
	}

	public void clearAllSteps() {
		if (mapGround != null) {
			steps.clear();
		}
	}

	/**
	 * 用这个方法表示还在战斗地图中
	 * @return
	 */
	public MapGround getMapGround() {
		return mapGround;
	}

	public void setMapGround(MapGround mapGround) {
		this.mapGround = mapGround;
	}

	/**
	 * 
	 * @return 没有步子的时候返回null
	 */
	public Steps getHeadStepButNotRemove() {
		if (steps.size() == 0) {
			return null;
		}
		return steps.get(0);
	}

	public void addSteps(Steps step) {
		steps.add(step);
	}

	public void removeHeadSteps() {
		if (steps.size() == 0) {
			return;
		}
		steps.remove(0);
	}
	
	/**
	 * 获取开火时间
	 * @return
	 */
	public long getFireTime() {
		return fireTime;
	}

	/**
	 * 设置开火时间
	 * @param fireTime
	 */
	public void setFireTime(long fireTime) {
		this.fireTime = fireTime;
	}

	/**
	 * 设置开火间隔
	 * @return
	 */
	public int getFireIntervel() {
		return fireIntervel;
	}

	/**
	 * 设置开火间隔
	 * @param fireIntervel
	 */
	public void setFireIntervel(int fireIntervel) {
		this.fireIntervel = fireIntervel;
	}

	/**
	 * 进入地图,不会更新地图
	 * 
	 * @param mapGround
	 * @return
	 */
	public boolean enterMap(MapGround mapGround) {
		this.mapGround = mapGround;
		mapGround.getClientInMap().put(sessionId, this);
		return true;
	}
	
	/**
	 * 加血
	 * @param blood
	 */
	public void addLive(int blood) {
		this.canBeFire++;
	}
	
	/**
	 * 更新位置，同时会更新到对应的地图里
	 * @param x 像素
	 * @param y
	 */
	public boolean updateLocation(int x, int y) {
//		
//		if(!halfValueCheck(mapGround.getBlocks(), x, y)) {
//			return false;
//		}
//		
		int gridIndex = MapHelper.convertPixelToGridIndex(x, y);
		int currentGridIndex = MapHelper.convertPixelToGridIndex(this.x, this.y);
		
//		if(gridIndex > MapHelper.TOTAL_GRIDS){
//			return false;
//		}
		
		if(mapGround.getBlocks().get(gridIndex).getStatus() == GridStatus.CAN_RUN.getStatus()
				|| currentGridIndex == gridIndex) {	//判断是否可一站立,移动后未改变格子也要考虑
			//先更新client的位置
			this.x = x;
			this.y = y;
			
			//去除占据格子
			mapGround.getBlocks().get(currentGridIndex).setStatus(GridStatus.CAN_RUN.getStatus());
			mapGround.getBlocks().get(currentGridIndex).setSessionId(0);
			
			//然后占据这个格子
			mapGround.getBlocks().get(gridIndex).setStatus(GridStatus.HAS_PLAYER.getStatus());
			mapGround.getBlocks().get(gridIndex).setSessionId(this.sessionId);
			
			logger.debug("玩家{}进入地图成功", this.getName());
			return true;
		}else {
			logger.error("玩家{}进入地图失败", this);
			return false;
		}
		
	}
	
	/**
	 * 碰撞中值判断，如果不行直接去除这个方法，目前只检测砖头--
	 * @param x 像素点
	 * @param y 像素点
	 * @param gridIndexOfXY 当前像素点代表的格子
	 * @return
	 */
	protected boolean halfValueCheck(List<Grid> grids, int x, int y) {
		/** 分别进行上，下，左，右的碰撞检测 **/
		int halfValue = MapHelper.PER_GRID_PIXEL / 2 - 1;
		int widthMax = MapHelper.PER_GRID_PIXEL * MapHelper.WIDTH_GRIDS - halfValue;
		int heightMax = MapHelper.PER_GRID_PIXEL * MapHelper.HEIGHT_GRIDS - halfValue;
		if(x < halfValue || y < halfValue || x > widthMax || y > heightMax) {	//越界了
			return false;
		}
		
		/** 检测上，右，下，左加上半值是否有砖块 **/
		int temX, temY;
		int temGridIndex;
		
		/** 上检测 **/
		temY = y - halfValue;
		temGridIndex = MapHelper.convertPixelToGridIndex(x, temY);
		if(grids.get(temGridIndex).getStatus() == GridStatus.CAN_NOT_SHOT.getStatus()) {
			return false;
		}
		
		/** 右检测 **/
		temX = x + halfValue;
		temGridIndex = MapHelper.convertPixelToGridIndex(temX, y);
		if(grids.get(temGridIndex).getStatus() == GridStatus.CAN_NOT_SHOT.getStatus()) {
			return false;
		}
		
		/**下检测 **/
		temY = y + halfValue;
		temGridIndex = MapHelper.convertPixelToGridIndex(x, temY);
		if(grids.get(temGridIndex).getStatus() == GridStatus.CAN_NOT_SHOT.getStatus()) {
			return false;
		}
		
		/**左检测 **/
		temX = x - halfValue;
		temGridIndex = MapHelper.convertPixelToGridIndex(temX, y);
		if(grids.get(temGridIndex).getStatus() == GridStatus.CAN_NOT_SHOT.getStatus()) {
			return false;
		}
		
		/** 左上角检测，右上角检测，右下角检测，左下角检测 **/
		
		return true;
		
	}

	/**
	 * 离开地图
	 * 
	 * @return
	 */
	public boolean exitMap() {
		mapGround.getClientInMap().remove(sessionId);
		mapGround = null;
		return true;
	}
	
	/**
	 * 是否在线
	 * @return
	 */
	public boolean isOnline() {
		return ctx != null;
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
		return "Client [sessionId=" + sessionId + ", name=" + name + ", line=" + line + ", ctx="
				+ ctx + ", mapGround=" + mapGround + ", steps=" + steps + ", speed=" + speed + ", x=" + x + ", y=" + y
				+ ", fireTime=" + fireTime + ", fireIntervel=" + fireIntervel + "]";
	}
	
	/**
	 * 下线标志
	 */
	public void offline() {
		this.ctx = null;
	}
}
