package com.pro100svitlo.creditCardNfcReader.utils;

import com.pro100svitlo.creditCardNfcReader.enums.CommandEnum;

public class CommandApdu {

	protected int mCla = 0x00;

	protected int mIns = 0x00;

	protected int mP1 = 0x00;

	protected int mP2 = 0x00;

	protected int mLc = 0x00;

	protected byte[] mData = new byte[0];

	protected int mLe = 0x00;

	protected boolean mLeUsed = false;

	public CommandApdu(final CommandEnum pEnum, final byte[] data, final int le) {
		mCla = pEnum.getCla();
		mIns = pEnum.getIns();
		mP1 = pEnum.getP1();
		mP2 = pEnum.getP2();
		mLc = data == null ? 0 : data.length;
		mData = data;
		mLe = le;
		mLeUsed = true;
	}

	public CommandApdu(final CommandEnum pEnum, final int p1, final int p2, final int le) {
		mCla = pEnum.getCla();
		mIns = pEnum.getIns();
		mP1 = p1;
		mP2 = p2;
		mLe = le;
		mLeUsed = true;
	}

	public CommandApdu(final CommandEnum pEnum, final int p1, final int p2) {
		mCla = pEnum.getCla();
		mIns = pEnum.getIns();
		mP1 = p1;
		mP2 = p2;
		mLeUsed = false;
	}

	public byte[] toBytes() {
		int length = 4; // CLA, INS, P1, P2
		if (mData != null && mData.length != 0) {
			length += 1; // LC
			length += mData.length; // DATA
		}
		if (mLeUsed) {
			length += 1; // LE
		}

		byte[] apdu = new byte[length];

		int index = 0;
		apdu[index] = (byte) mCla;
		index++;
		apdu[index] = (byte) mIns;
		index++;
		apdu[index] = (byte) mP1;
		index++;
		apdu[index] = (byte) mP2;
		index++;
		if (mData != null && mData.length != 0) {
			apdu[index] = (byte) mLc;
			index++;
			System.arraycopy(mData, 0, apdu, index, mData.length);
			index += mData.length;
		}
		if (mLeUsed) {
			apdu[index] += (byte) mLe; // LE
		}

		return apdu;
	}

}
