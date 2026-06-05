package minefarts.smarttube.audio;

import android.annotation.TargetApi;
import androidx.annotation.Nullable;
import minefarts.smarttube.C;

/**
 * Attributes for audio playback, which configure the underlying platform
 * {@link android.media.AudioTrack}.
 * <p>
 * To set the audio attributes, create an instance using the {@link Builder} and either pass it to
 * {@link minefarts.smarttube.SimpleExoPlayer#setAudioAttributes(AudioAttributes)} or
 * send a message of type {@link C#MSG_SET_AUDIO_ATTRIBUTES} to the audio renderers.
 * <p>
 * This class is based on {@link android.media.AudioAttributes}, but can be used on all supported
 * API versions.
 */
public final class AudioAttributes {

  public static final AudioAttributes DEFAULT = new Builder().build();

  /**
   * Builder for {@link AudioAttributes}.
   */
  public static final class Builder {

    private @C.AudioContentType int contentType;
    private @C.AudioFlags int flags;
    private @C.AudioUsage int usage;

    /**
     * Creates a new builder for {@link AudioAttributes}.
     * <p>
     * By default the content type is {@link C#CONTENT_TYPE_UNKNOWN}, usage is
     * {@link C#USAGE_MEDIA}, and no flags are set.
     */
    public Builder() {
      contentType = C.CONTENT_TYPE_UNKNOWN;
      flags = 0;
      usage = C.USAGE_MEDIA;
    }

    /**
     * @see android.media.AudioAttributes.Builder#setContentType(int)
     */
    public Builder setContentType(@C.AudioContentType int contentType) {
      this.contentType = contentType;
      return this;
    }

    /**
     * @see android.media.AudioAttributes.Builder#setFlags(int)
     */
    public Builder setFlags(@C.AudioFlags int flags) {
      this.flags = flags;
      return this;
    }

    /**
     * @see android.media.AudioAttributes.Builder#setUsage(int)
     */
    public Builder setUsage(@C.AudioUsage int usage) {
      this.usage = usage;
      return this;
    }

    /**
     * Creates an {@link AudioAttributes} instance from this builder.
     */
    public AudioAttributes build() {
      return new AudioAttributes(contentType, flags, usage);
    }

  }

  public final @C.AudioContentType int contentType;
  public final @C.AudioFlags int flags;
  public final @C.AudioUsage int usage;

  private @Nullable android.media.AudioAttributes audioAttributesV21;

  private AudioAttributes(@C.AudioContentType int contentType, @C.AudioFlags int flags,
      @C.AudioUsage int usage) {
    this.contentType = contentType;
    this.flags = flags;
    this.usage = usage;
  }

  @TargetApi(21)
  public android.media.AudioAttributes getAudioAttributesV21() {
    if (audioAttributesV21 == null) {
      audioAttributesV21 = new android.media.AudioAttributes.Builder()
          .setContentType(contentType)
          .setFlags(flags)
          .setUsage(usage)
          .build();
    }
    return audioAttributesV21;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    AudioAttributes other = (AudioAttributes) obj;
    return this.contentType == other.contentType && this.flags == other.flags
        && this.usage == other.usage;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + contentType;
    result = 31 * result + flags;
    result = 31 * result + usage;
    return result;
  }

}
