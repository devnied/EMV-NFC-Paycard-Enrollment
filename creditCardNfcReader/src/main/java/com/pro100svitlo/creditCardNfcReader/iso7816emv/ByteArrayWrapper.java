package com.pro100svitlo.creditCardNfcReader.iso7816emv;

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