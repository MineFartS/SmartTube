package minefarts.sharedutils.formatbuilders.hlsbuilder;

import minefarts.sharedutils.service.data.MediaFormat;

import java.util.List;

public interface UrlListBuilder {
    void append(MediaFormat mediaItem);
    boolean isEmpty();
    List<String> buildUriList();
}
