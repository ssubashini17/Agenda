package com.messagehub;

public enum Status {

	read("read"), unread("unread");

	private String value;

	private Status(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

}
