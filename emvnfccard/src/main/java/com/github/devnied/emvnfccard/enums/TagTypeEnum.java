package com.github.devnied.emvnfccard.enums;

/*
 * Copyright 2010 sasc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Seem EMV Book 3 Annex B
 * 
 * @author sasc
 */
public enum TagTypeEnum {

	/**
	 * The value field of a primitive data object contains a data element for financial transaction interchange
	 */
	PRIMITIVE,
	/**
	 * The value field of a constructed data object contains one or more primitive or constructed data objects. The value field of
	 * a constructed data object is called a template.
	 */
	CONSTRUCTED
}