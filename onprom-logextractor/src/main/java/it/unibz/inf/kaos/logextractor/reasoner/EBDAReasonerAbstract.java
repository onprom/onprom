/*
 * Copyright (C) 2017 Free University of Bozen-Bolzano
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.kaos.logextractor.reasoner;

import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgEvent;
import it.unibz.inf.kaos.obdamapper.util.ExecutionMsgListener;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public abstract class EBDAReasonerAbstract implements EBDAReasoner{

	
	//Listener for all of the execution logs generated by this class
	private ExecutionMsgListener executionLogListener;
	
	public void setExecutionLogListener(ExecutionMsgListener ell) {
		
		this.executionLogListener = ell;
	}

	protected void addNewExecutionMsg(ExecutionMsgEvent eme){
		
		if(executionLogListener != null)
			executionLogListener.addNewExecutionMsg(eme);	
	}

	public abstract void dispose();
}
