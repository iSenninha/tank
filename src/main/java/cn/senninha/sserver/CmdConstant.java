package cn.senninha.sserver;

public interface CmdConstant {
	/** req **/
	/** 登陆 **/
	public static final int LOGIN_REQ = 1001;			
	/** 心跳 **/
	public static final int HEART_REQ = 1002;			
	
	/**------------------------------------------------------------------**/
	
	/** resp **/
	/** 登陆 **/
	public static final int LOGIN_RES = 2001;
	//  心跳 **/
	public static final int HEART_RES = 2002;	
	/** 发送地图信息 **/
	public static final int MAP_RESOURCE_RES = 2003;
	
	
	
	/** 内部封装cmd **/
	/** 格子 **/
	public static final int GRID = 3000;
}