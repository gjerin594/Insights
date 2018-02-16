/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformcommons.dal.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 
 * @author Vishal Ganjare (vganjare)
 * This class will hold the details about the Neo4j fields for which the index has been enabled.
 *
 */
public class Neo4jFieldIndexRegistry {
	private static final Logger log = Logger.getLogger(Neo4jFieldIndexRegistry.class);
	private static Map<String, List<String>> indexedFieldsRegistry = new HashMap<String, List<String>>();
	private static Queue<FieldIndexData> indexingRequiredFieldsQueue = new LinkedList<FieldIndexData>();
	private static final Neo4jFieldIndexRegistry instance = new Neo4jFieldIndexRegistry();
	private Thread fieldIndexCreatorThread = null;
	
	private Neo4jFieldIndexRegistry() {
		loadFieldIndices();
		addDefaultFieldIndices();
	}
	
	public static Neo4jFieldIndexRegistry getInstance() {
		return instance;
	}

	/**
	 * Add index for give label and field.
	 * @param label
	 * @param field
	 */
	public void syncFieldIndex(String label, String field) {
		if(ApplicationConfigProvider.getInstance().isEnableFieldIndex()) {
			List<String> indexedFields = indexedFieldsRegistry.get(label);
			if(indexedFields == null || !indexedFields.contains(field)) {
				indexingRequiredFieldsQueue.add(new FieldIndexData(label, field));
			}
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			while(!indexingRequiredFieldsQueue.isEmpty()) {
				FieldIndexData fieldData = indexingRequiredFieldsQueue.peek();
				//createFieldIndex(label, field);
				JsonObject addFieldIndex = dbHandler.addFieldIndex(fieldData.getLabel(), fieldData.getField());
				System.out.println(addFieldIndex);
				FieldIndexData data = indexingRequiredFieldsQueue.poll();
				System.out.println("Peek---> L: "+label+", F: "+field+" ...... Poll---> L: "+data.getLabel()+", F: "+data.getField());
			}
			/*if(fieldIndexCreatorThread == null || !fieldIndexCreatorThread.isAlive()) {
				fieldIndexCreatorThread = new Thread(new FieldIndexCreator());
				fieldIndexCreatorThread.start();
			}*/
		}
	}
	
	/**
	 * Load the field indices from Neo4j
	 */
	private void loadFieldIndices() {
		indexedFieldsRegistry.clear();
		Neo4jDBHandler dbHandler = new Neo4jDBHandler();
		JsonArray fieldIndices = dbHandler.loadFieldIndices();
		for(JsonElement fieldIndexElem : fieldIndices) {
			JsonObject fieldIndex = fieldIndexElem.getAsJsonObject();
			String label = fieldIndex.get("label").getAsString();
			String field = fieldIndex.getAsJsonArray("property_keys").get(0).getAsString();
			List<String> fields = indexedFieldsRegistry.get(label);
			if(fields == null) {
				fields = new ArrayList<String>();
				indexedFieldsRegistry.put(label, fields);
			}
			fields.add(field);
		}
	}
	
	/**
	 * Add the indices for default fields in InSights.
	 */
	private void addDefaultFieldIndices() {
		syncFieldIndex("DATA", "uuid");
		syncFieldIndex("DATA", "toolName");
		syncFieldIndex("DATA", "correlationTime");
		syncFieldIndex("DATA", "maxCorrelationTime");
		syncFieldIndex("DATA", "inSightsTime");
		syncFieldIndex("DATA", "inSightsTimeX");
	}
	
	
	class FieldIndexCreator implements Runnable {		
		/**
		 * Execute the index creation in parallel. 
		 */
		@Override
		public void run() {
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			while(!indexingRequiredFieldsQueue.isEmpty()) {
				FieldIndexData fieldData = indexingRequiredFieldsQueue.peek();
				String label = fieldData.getLabel();
				String field = fieldData.getField();
				//createFieldIndex(label, field);
				JsonObject addFieldIndex = dbHandler.addFieldIndex(label, field);
				System.out.println(addFieldIndex);
				FieldIndexData data = indexingRequiredFieldsQueue.poll();
				System.out.println("Peek---> L: "+label+", F: "+field+" ...... Poll---> L: "+data.getLabel()+", F: "+data.getField());
				/*int totalSleepTime = 5 * 60; //Total wait time in seconds
				while(true) {
					loadFieldIndices();
					List<String> indexedFields = indexedFieldsRegistry.get(label);
					if(indexedFields == null || !indexedFields.contains(field)) {
						try {
							System.out.println("Thread sleeping for 10 secs");
							Thread.sleep(10 * 1000); //Wait till the index is online.
							System.out.println("Thread woke up after 10 secs");
							totalSleepTime = totalSleepTime - 10;
							if(totalSleepTime < 0) {
								break;
							}
						} catch (InterruptedException e) {
							log.error("Index creator thread interrupted.", e);
						}
					}else {
						FieldIndexData data = indexingRequiredFieldsQueue.poll();
						System.out.println("Peek---> L: "+label+", F: "+field+" ...... Poll---> L: "+data.getLabel()+", F: "+data.getField());
						break;
					}
				}*/
			}
		}
		
		/**
		 * For give label and field, create index in Neo4j
		 * @param label
		 * @param field
		 */
		private void createFieldIndex(String label, String field) {
			String query = "CREATE INDEX ON :"+label+"("+field+")";
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			try {
				System.out.println(query);
				GraphResponse executeCypherQuery = dbHandler.executeCypherQuery(query);
				log.debug(executeCypherQuery.getJson());
				System.out.println(executeCypherQuery.getJson());
			} catch (GraphDBException e) {
				log.error("Unable to add field index with query: "+query, e);
			}
		}
	}
}
