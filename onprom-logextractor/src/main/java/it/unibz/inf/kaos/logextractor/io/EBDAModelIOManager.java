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

package it.unibz.inf.kaos.logextractor.io;

import java.io.File;
import java.io.IOException;

import it.unibz.inf.kaos.logextractor.exception.InvalidEBDAModelException;
import it.unibz.inf.kaos.logextractor.model.EBDAModel;
import it.unibz.inf.kaos.logextractor.model.impl.EBDAModelNaiveImpl;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.io.ModelIOManager;

/**
 * I/O manager for EBDA Model
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 */
public class EBDAModelIOManager {

	private EBDAModelNaiveImpl ebdaModel;
	private ModelIOManager ioMan;
	
	public EBDAModelIOManager(EBDAModel eobdaModel){
		ioMan = new ModelIOManager(eobdaModel);
	}

	public void load(File ebdaFile) throws IOException, InvalidMappingException, InvalidEBDAModelException {		
		ioMan.load(ebdaFile);

		//Validate the Event OBDA Model
			if(!this.validateEBDAModel(this.ebdaModel)) 
				throw new InvalidEBDAModelException();
		//END OF Validating the Event OBDA Model
	}
	
	public void save(File eobdaFile) throws IOException {
		ioMan.save(eobdaFile);
	}
	
	private boolean validateEBDAModel(EBDAModelNaiveImpl eobdaModel){
		//TODO Still need to be implemented
		return true;
	}
}
