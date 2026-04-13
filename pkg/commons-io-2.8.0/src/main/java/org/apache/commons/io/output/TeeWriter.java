

package org.apache.commons.io.output;

import java.io.Writer;
import java.util.Collection;

/**
 * Classic splitter of {@link Writer}. Named after the Unix 'tee' command. It allows a stream to be branched off so
 * there are now two streams.
 * <p>
 * This currently a only convenience class with the proper name "TeeWriter".
 * </p>
 *
 * @since 2.7
 */
public class TeeWriter extends ProxyCollectionWriter {

    /**
     * Creates a new filtered collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    public TeeWriter(final Collection<Writer> writers) {
        super(writers);
    }

    /**
     * Creates a new filtered collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    public TeeWriter(final Writer... writers) {
        super(writers);
    }
}
