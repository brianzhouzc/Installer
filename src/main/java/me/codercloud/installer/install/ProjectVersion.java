package me.codercloud.installer.install;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

public class ProjectVersion {
	
	private String id;
	private Date date;
	
	public ProjectVersion(long date, String id) {
		if(date != -1)
			this.date = new Date(date*1000);
		else
			this.date = null;
		this.id = id;
		
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getDateTxt(String format) {
		
		return date == null?null:DateFormatUtils.format(date, format);
	}
	
	public String getId() {
		return id;
	}
	
}
