package minefarts.smarttube.text;

import java.util.List;

/**
 * Receives text output.
 */
public interface TextOutput {

  /**
   * Called when there is a change in the {@link Cue}s.
   *
   * @param cues The {@link Cue}s. May be empty.
   */
  void onCues(List<Cue> cues);
}
