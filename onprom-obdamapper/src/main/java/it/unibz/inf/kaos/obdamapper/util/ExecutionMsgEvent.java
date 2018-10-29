package it.unibz.inf.kaos.obdamapper.util;

import java.util.EventObject;

public class ExecutionMsgEvent extends EventObject{

	private static final long serialVersionUID = -3056207072279623114L;
	private StringBuilder log;
	
	public ExecutionMsgEvent(Object source, StringBuilder log) {
		super(source);
		this.log = log;
	}

	public StringBuilder getLog() {
		return log;
	}

	public void setLog(StringBuilder log) {
		this.log = log;
	}
}
