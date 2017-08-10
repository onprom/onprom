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


package it.unibz.inf.kaos.logextractor.util;

import java.util.ArrayList;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * 
 * @author Ario Santoso (santoso.ario@gmail.com / santoso@inf.unibz.it)
 *
 */
public class EfficientHashMap<V> {

	private TIntIntHashMap map;
	private TObjectIntHashMap<String> collisionMap;
	private ArrayList<V> values;
	private int counter;
	
	public EfficientHashMap(int initCapacity){
	
		map = new TIntIntHashMap(initCapacity);
		collisionMap = new TObjectIntHashMap<String>(initCapacity);
		values = new ArrayList<V>(initCapacity);
		counter = 0;
	}

	public EfficientHashMap(){
		
		map = new TIntIntHashMap();
		collisionMap = new TObjectIntHashMap<String>();
		values = new ArrayList<V>();
		counter = 0;
	}

	
	public void put(String key, V value){
		
		int keyInt = key.hashCode();
		
		if(!this.map.contains(keyInt)){
			
			this.map.put(key.hashCode(), counter);
			this.values.add(value);
			counter++;
			
		}else{
			
			this.collisionMap.put(key.intern(), counter);
			this.values.add(value);
			counter++;
		}
	}
	
	public boolean containsKey(String key){
		
		return this.map.containsKey(key.hashCode());
	}

	public V get (String key){
		
		if(this.collisionMap.contains(key)){
			
			int idx = this.collisionMap.get(key);
			return this.values.get(idx);
		}else{
			
			int idx = this.map.get(key.hashCode());
			return this.values.get(idx);
		}
	}
	
	public void clear(){
		
		this.map.clear();
		this.values.clear();
	}
	
	public ArrayList<V> values(){
		
		return this.values;
	}

}
