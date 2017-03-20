/*
 * Copyright (C) 2013 MILLAU Julien
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
package com.github.devnied.emvnfccard.parser.apdu.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.parser.apdu.IFile;

/**
 * Class used to manage all annotation
 * 
 * @author MILLAU Julien
 * 
 */
public final class AnnotationUtils {

	/**
	 * List of annoted class
	 */
	@SuppressWarnings("unchecked")
	private static final Class<? extends IFile>[] LISTE_CLASS = new Class[] { EmvTransactionRecord.class };

	/**
	 * AnnotationUtils singleton
	 */
	private static final AnnotationUtils INSTANCE = new AnnotationUtils();

	/**
	 * Method to get the unique instance of the class
	 * 
	 * @return AnnotationUtils instance
	 */
	public static AnnotationUtils getInstance() {
		return INSTANCE;
	}

	/**
	 * Map which contain
	 */
	private final Map<String, Map<ITag, AnnotationData>> map;
	private final Map<String, Set<AnnotationData>> mapSet;

	/**
	 * Private default constructor
	 */
	private AnnotationUtils() {
		map = new HashMap<String, Map<ITag, AnnotationData>>();
		mapSet = new HashMap<String, Set<AnnotationData>>();
		extractAnnotation();
	}

	/**
	 * Method to extract all annotation information and store them in the map
	 */
	private void extractAnnotation() {
		for (Class<? extends IFile> clazz : LISTE_CLASS) {

			Map<ITag, AnnotationData> maps = new HashMap<ITag, AnnotationData>();
			Set<AnnotationData> set = new TreeSet<AnnotationData>();

			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				AnnotationData param = new AnnotationData();
				field.setAccessible(true);
				param.setField(field);
				Data annotation = field.getAnnotation(Data.class);
				if (annotation != null) {
					param.initFromAnnotation(annotation);
					maps.put(param.getTag(), param);
					try {
						set.add((AnnotationData) param.clone());
					} catch (CloneNotSupportedException e) {
						// do nothing
					}
				}
			}
			mapSet.put(clazz.getName(), set);
			map.put(clazz.getName(), maps);
		}
	}

	/**
	 * Getter map set
	 * 
	 * @return the map
	 */
	public Map<String, Set<AnnotationData>> getMapSet() {
		return mapSet;
	}

	/**
	 * Getter map
	 * 
	 * @return the map
	 */
	public Map<String, Map<ITag, AnnotationData>> getMap() {
		return map;
	}

}
