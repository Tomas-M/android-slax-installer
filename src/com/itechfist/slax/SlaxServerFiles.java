package com.itechfist.slax;

public class SlaxServerFiles {
	
	private boolean flag;
	private String file;
	
	
	public SlaxServerFiles(boolean flag, String fileName) {
		this.flag = flag;
		this.file = fileName;
	}
	public boolean getFlag() {
		return flag;
	}
	public String getFile() {
		return file;
	}

}
