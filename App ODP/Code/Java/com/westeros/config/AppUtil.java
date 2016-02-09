package com.westeros.config;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.NotesException;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.ibm.xsp.bluemix.util.context.BluemixContextManager;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.westeros.app.Utils;
/**
 * @author edm00se
 * 
 * This is a facade bean with a couple values being set into memory. Otherwise,
 * it's a pass-through for calls to the BluemixContext instance.
 * 
 */
public class AppUtil implements Serializable {
	
	private static final long	serialVersionUID	= 1L;
	
	private Boolean runningOnBluemix;
	private String dbServerName;
	private String dbAppPath;
	
	public AppUtil(){
		
		this.setRunningOnBluemix();
		
		if( this.runningOnBluemix ) {
			Vector<String> otherDatabaseVec = BluemixContextManager
			.getInstance().getDataService().atDbName();
			this.dbServerName = otherDatabaseVec.get(0);
			this.dbAppPath = otherDatabaseVec.get(1);
		} else {
			this.dbServerName = "";
			this.dbAppPath = ExtLibUtil.getXspContext().getProperty("xsp.local.iceandfire.data", "AppOfIceAndFire_data.nsf");
		}
		
	}
	
	public String getDbServerName() {
		return this.dbServerName;
	}
	public String getDbAppPath() {
		return this.dbAppPath;
	}
	
	public String getCustDbAppPath() {
		if( this.isRunningOnBluemix() ) {
			return BluemixContextManager.getInstance().getDataService().getAppPath();
		}else {
			return this.getDbAppPath();
		}
	}
	
	public Database getDBObj() {
		try {
			if( this.isRunningOnBluemix() ) {
				Map<String, Object> reqScopeMap = Utils.getRequestScope();
				if( !reqScopeMap.containsKey("dbObj") ){
					reqScopeMap.put("dbObj", Utils.getSession()
							.getDatabase(dbServerName, dbAppPath));
				}
				Database db = (Database) reqScopeMap.get("dbObj");
				return db;
			}else {
				Database db = ExtLibUtil.getCurrentSession().getDatabase(
						this.dbServerName, this.dbAppPath);
				return db;
			}
		}catch( NotesException e ) {
			// OpenLogItem.logError(e);
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isRunningOnBluemix() {
		if( this.runningOnBluemix == null ) {
			setRunningOnBluemix();
		}
		return this.runningOnBluemix;
	}
	
	public void setRunningOnBluemix() {
		try {
			this.runningOnBluemix = BluemixContextManager.getInstance().isRunningOnBluemix();
		} catch (Exception e) {
			this.runningOnBluemix = false;
		}
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}