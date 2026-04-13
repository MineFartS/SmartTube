

package org.apache.commons.io.file;

import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;

/**
 * A {@link SimpleFileVisitor} typed to a {@link Path}.
 *
 * @since 2.7
 */
public abstract class SimplePathVisitor extends SimpleFileVisitor<Path> {

    /**
     * Constructs a new instance.
     */
    protected SimplePathVisitor() {
        super();
    }

}
