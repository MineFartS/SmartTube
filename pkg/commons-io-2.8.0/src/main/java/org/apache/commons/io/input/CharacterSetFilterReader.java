
package org.apache.commons.io.input;

import java.io.Reader;
import java.util.Collections;
import java.util.Set;

/**
 * A filter reader that removes a given set of characters represented as <code>int</code> code points, handy to remove
 * known junk characters from CSV files for example.
 * <p>
 * This class must convert each <code>int</code> read to an <code>Integer</code>. You can increase the Integer cache
 * with a system property, see {@link Integer}.
 * </p>
 */
public class CharacterSetFilterReader extends AbstractCharacterFilterReader {

    private static final Set<Integer> EMPTY_SET = Collections.emptySet();
    private final Set<Integer> skipSet;

    /**
     * Constructs a new reader.
     *
     * @param reader
     *            the reader to filter.
     * @param skip
     *            the set of characters to filter out.
     */
    public CharacterSetFilterReader(final Reader reader, final Set<Integer> skip) {
        super(reader);
        this.skipSet = skip == null ? EMPTY_SET : Collections.unmodifiableSet(skip);
    }

    @Override
    protected boolean filter(final int ch) {
        // Note WRT Integer.valueOf(): You can increase the Integer cache with a system property, see {@link Integer}.
        return skipSet.contains(Integer.valueOf(ch));
    }

}
