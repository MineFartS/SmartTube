package minefarts.smarttube.commons.models;

/**
 * Stub for chatkit MessageContentType.
 */
public interface MessageContentType {
    interface Image extends MessageContentType {
        String getImageUrl();
    }
}