package it.unibz.inf.kaos.obdamapper.util;

import java.util.EventListener;

public interface ExecutionMsgListener extends EventListener {

    void addNewExecutionMsg(ExecutionMsgEvent log);
}
