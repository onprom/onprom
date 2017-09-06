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
import it.unibz.inf.kaos.logextractor.model.impl.LEObjectFactory;
import it.unibz.inf.ontop.exception.InvalidMappingException;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class EBDAModelIO {

	
	
	///////////////////////////////////////////////////////////////////////////////////
	// EBDA MODEL I/O STUFF
	///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Write an EBDA Model to a File
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param ebdaModel - the EBDAModel to be written
	 * @param filePath - the path of the file where we want to write the EBDA Model
	 * @throws InvalidEBDAModelException 
	 */
	public static void writeEBDAModelToFile(EBDAModel ebdaModel, String filePath) throws InvalidEBDAModelException{
		
		EBDAModelIOManager io = new EBDAModelIOManager(ebdaModel);

		try {
			io.save(new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read an EBDA Model from a file
	 * 
	 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
	 * @param filePath - the file containing EBDA Model information
	 * @return EBDAModel
	 * @throws InvalidEBDAModelException 
	 */
	public static EBDAModel readEBDAModelFromFile(String filePath) throws InvalidMappingException, InvalidEBDAModelException{
		
		EBDAModel ebdaModel = LEObjectFactory.getInstance().createEBDAModelImpl3();
		EBDAModelIOManager io = new EBDAModelIOManager(ebdaModel);

		try {
			io.load(new File(filePath));
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}		
		
		return ebdaModel;		
	}

	///////////////////////////////////////////////////////////////////////////////////
	// END OF EBDA MODEL I/O STUFF
	///////////////////////////////////////////////////////////////////////////////////

}
