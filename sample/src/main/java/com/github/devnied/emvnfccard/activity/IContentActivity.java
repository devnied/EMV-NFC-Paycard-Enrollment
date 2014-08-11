package com.github.devnied.emvnfccard.activity;

import com.github.devnied.emvnfccard.fragment.IRefreshable;
import com.github.devnied.emvnfccard.model.EmvCard;

public interface IContentActivity {

	/**
	 * Method used to get log from root activity
	 * 
	 * @return String buffer
	 */
	StringBuffer getLog();

	/**
	 * Method used to get read card data
	 * 
	 * @return
	 */
	EmvCard getCard();

	/**
	 * Method to set refreshabe content
	 * 
	 * @param pRefreshable
	 */
	void setRefreshableContent(IRefreshable pRefreshable);

}
