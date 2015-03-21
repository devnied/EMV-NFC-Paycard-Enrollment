package com.github.devnied.emvnfccard.data;

/**
 * Item wrapper for list view
 *
 * @author Millau Julien
 *
 * @param <T>
 */
public class ItemWrapper<T> {

	/**
	 * Item data
	 */
	private T data;

	/**
	 * Item state
	 */
	private boolean clicked;

	public ItemWrapper(final T pData) {
		data = pData;
	}

	/**
	 * @return the data
	 */
	public T getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(final T data) {
		this.data = data;
	}

	/**
	 * @return the clicked
	 */
	public boolean isClicked() {
		return clicked;
	}

	/**
	 * @param clicked
	 *            the clicked to set
	 */
	public void setClicked(final boolean clicked) {
		this.clicked = clicked;
	}

}
