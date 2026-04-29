
package org.apache.commons.io.input;

import java.io.Reader;

/**
 * A filter reader that filters out a given character represented as an <code>int</code> code point, handy to remove
 * known junk characters from CSV files for example. This class is the most efficient way to filter out a single
 * character, as opposed to using a {@link CharacterSetFilterReader}. You can also nest {@link CharacterFilterReader}s.
 */
public class CharacterFilterReader extends AbstractCharacterFilterReader {

    private final int skip;

    /**
     * Constructs a new reader.
     *
     * @param reader
     *            the reader to filter.
     * @param skip
     *            the character to filter out.
     */
    public CharacterFilterReader(final Reader reader, final int skip) {
        super(reader);
        this.skip = skip;
    }

    @Override
    protected boolean filter(final int ch) {
        return ch == skip;
    }

}
