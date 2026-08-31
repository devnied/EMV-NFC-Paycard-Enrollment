package com.github.devnied.emvnfccard;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;

/**
 * The library is consumed as a released artifact, so the members an integrator
 * may have called or overridden are kept, deprecated rather than removed.
 */
public class ApiCompatibilityTest {

	/**
	 * Subclass used to reach the protected members from the test, and to
	 * observe the delegation.
	 */
	private static final class ExposedParser extends EmvParser {

		private boolean delegated;

		private ExposedParser(final EmvTemplate pTemplate) {
			super(pTemplate);
		}

		@SuppressWarnings("deprecation")
		private boolean callFormerSignature(final byte[] pGpo) throws CommunicationException {
			return extractCommonsCardData(pGpo);
		}

		@Override
		protected boolean extractCommonsCardData(final byte[] pGpo, final Application pApplication) {
			delegated = true;
			return true;
		}
	}

	/**
	 * The extraction now fills the application being read, which changed the
	 * signature of a protected method. The former one is kept so that the code
	 * calling it still compiles and still links.
	 */
	@Test
	public void testExtractCommonsCardDataKeepsItsFormerSignature() throws NoSuchMethodException {
		Method compatibility = EmvParser.class.getDeclaredMethod("extractCommonsCardData", byte[].class);

		Assertions.assertThat(Modifier.isProtected(compatibility.getModifiers())).isTrue();
		Assertions.assertThat(compatibility.getReturnType()).isEqualTo(boolean.class);
		Assertions.assertThat(compatibility.getAnnotation(Deprecated.class)).isNotNull();
		// The throws clause is part of the contract: an override may not add a
		// checked exception the overridden method does not declare
		Assertions.assertThat(compatibility.getExceptionTypes()).containsOnly(CommunicationException.class);

		Method current = EmvParser.class.getDeclaredMethod("extractCommonsCardData", byte[].class, Application.class);
		Assertions.assertThat(Modifier.isProtected(current.getModifiers())).isTrue();
		Assertions.assertThat(current.getAnnotation(Deprecated.class)).isNull();
	}

	/**
	 * The kept signature is not an empty shell: it runs the extraction, with
	 * an application to fill.
	 */
	@Test
	public void testFormerSignatureDelegates() throws CommunicationException {
		ExposedParser parser = new ExposedParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());

		// An empty response carries no track and no application data
		Assertions.assertThat(parser.callFormerSignature(new byte[0])).isFalse();
		// The override of the two arguments method is not what the deprecated
		// one calls, so it cannot bounce back into it
		Assertions.assertThat(parser.delegated).isFalse();
	}

	/**
	 * The migration the deprecation recommends is to move an override to the
	 * two arguments method. Such an override calling the deprecated one must
	 * not bounce back into itself.
	 */
	@Test
	public void testMovedOverrideDoesNotRecurse() throws CommunicationException {
		MigratedParser parser = new MigratedParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());

		// Would end in a StackOverflowError if the deprecated overload
		// dispatched virtually on the two arguments method
		Assertions.assertThat(parser.extractCommonsCardData(new byte[0], new Application())).isFalse();
		Assertions.assertThat(parser.calls).isEqualTo(1);
	}

	/**
	 * Subclass written the way the deprecation tells an integrator to migrate:
	 * the override sits on the two arguments method and calls the former one.
	 */
	private static final class MigratedParser extends EmvParser {

		private int calls;

		private MigratedParser(final EmvTemplate pTemplate) {
			super(pTemplate);
		}

		@Override
		@SuppressWarnings("deprecation")
		protected boolean extractCommonsCardData(final byte[] pGpo, final Application pApplication)
				throws CommunicationException {
			calls++;
			return extractCommonsCardData(pGpo);
		}
	}
}
