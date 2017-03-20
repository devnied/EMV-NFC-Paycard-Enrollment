package com.github.devnied.emvnfccard.fragment.viewPager;

/**
 * Interface for fragment inside view pager
 * 
 * @author Millau Julien
 * 
 */
public interface IFragment {

	/**
	 * Method used to get fragment title
	 * 
	 * @return
	 */
	String getTitle();

	/**
	 * Method used to indicate if the fragment can be displayed or not
	 * 
	 * @return true or false
	 */
	boolean isEnable();

}
