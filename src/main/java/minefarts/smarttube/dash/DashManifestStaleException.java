package minefarts.smarttube.dash;

import java.io.IOException;

/** Thrown when a live playback's manifest is stale and a new manifest could not be loaded. */
public final class DashManifestStaleException extends IOException {}
