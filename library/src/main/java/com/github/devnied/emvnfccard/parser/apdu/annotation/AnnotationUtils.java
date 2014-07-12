package com.github.devnied.emvnfccard.parser.apdu.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.github.devnied.emvnfccard.model.EMVPaymentRecord;
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
	private static final Class<? extends IFile>[] LISTE_CLASS = new Class[] { EMVPaymentRecord.class };

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
	private final Map<String, Set<AnnotationData>> map;

	/**
	 * Private default constructor
	 */
	private AnnotationUtils() {
		map = new HashMap<String, Set<AnnotationData>>();
		extractAnnotation();
	}

	/**
	 * Method to extract all annotation information and store them in the map
	 */
	private void extractAnnotation() {
		for (Class<? extends IFile> clazz : LISTE_CLASS) {

			Set<AnnotationData> set = new TreeSet<AnnotationData>();

			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				AnnotationData param = new AnnotationData();
				field.setAccessible(true);
				param.setField(field);
				Data annotation = field.getAnnotation(Data.class);
				if (annotation != null) {
					param.initFromAnnotation(annotation);
					set.add(param);
				}
			}
			map.put(clazz.getName(), set);
		}
	}

	/**
	 * Getter map
	 * 
	 * @return the map
	 */
	public Map<String, Set<AnnotationData>> getMap() {
		return map;
	}

}
