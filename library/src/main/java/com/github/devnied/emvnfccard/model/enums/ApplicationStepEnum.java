/*
 * Copyright (C) 2019 MILLAU Julien
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
package com.github.devnied.emvnfccard.model.enums;

import java.util.List;

import com.github.devnied.emvnfccard.model.Application;

/**
 * Application step enum
 */
public enum ApplicationStepEnum implements IKeyEnum {

	/**
	 * Application not selected
	 */
	NOT_SELECTED(0),
	/**
	 *  Application selected
	 */
	SELECTED(1), 
	/**
	 * Application read
	 */
	READ(2);
	
	/**
	 * Step key
	 */
	private int key;
	
	/**
	 * Constructor
	 * 
	 * @param pKey step id
	 */
	private ApplicationStepEnum(final int pKey) {
		key = pKey;
	}

	@Override
	public int getKey() {
		return key;
	}
	
	/**
	 * Check if at least one application step is getter than the specified step
	 * @param pApplications applications list
	 * @param pStep step to compare
	 * @return 
	 */
	public static boolean isAtLeast(final List<Application> pApplications, final ApplicationStepEnum pStep){
		boolean ret = false;
		if (pApplications != null && pStep != null){
			for (Application app: pApplications){
				if (app != null && app.getReadingStep() != null && app.getReadingStep().key >= pStep.getKey()){
					ret = true;
				}
			}
		}
		return ret;
	}

}