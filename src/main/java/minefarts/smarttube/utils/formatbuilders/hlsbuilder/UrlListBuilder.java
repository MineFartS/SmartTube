package minefarts.smarttube.utils.formatbuilders.hlsbuilder;

import minefarts.smarttube.utils.service.data.MediaFormat;

import java.util.List;

public interface UrlListBuilder {
    void append(MediaFormat mediaItem);
    boolean isEmpty();
    List<String> buildUriList();
}
