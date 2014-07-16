package com.github.devnied.emvnfccard.parser.apdu.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.github.devnied.emvnfccard.iso7816emv.ITag;
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
