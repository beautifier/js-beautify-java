package io.beautifier.core;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
@FunctionalInterface
public interface BeautifierFunction {

	String beautify(@Nullable String sourceText, Options<?> options);

}
