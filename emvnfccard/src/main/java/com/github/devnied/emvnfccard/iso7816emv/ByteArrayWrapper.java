package com.github.devnied.emvnfccard.iso7816emv;

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

import java.util.Arrays;

public final class ByteArrayWrapper {

	private final byte[] data;
	private final int hashcode;

	private ByteArrayWrapper(final byte[] data) {
		this.data = data;
		hashcode = Arrays.hashCode(data);
	}

	public static ByteArrayWrapper wrapperAround(final byte[] data) {
		if (data == null) {
			throw new NullPointerException();
		}
		return new ByteArrayWrapper(data);
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ByteArrayWrapper)) {
			return false;
		}
		return Arrays.equals(data, ((ByteArrayWrapper) other).data);
	}

	@Override
	public int hashCode() {
		return hashcode;
	}
}