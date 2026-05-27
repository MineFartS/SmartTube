package minefarts.smarttube.extractor.mp4;

import androidx.annotation.Nullable;
import minefarts.smarttube.utils.ParsableByteArray;
import minefarts.smarttube.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ConstantField")
/* package */ abstract class Atom {

  /**
   * Size of an atom header, in bytes.
   */
  public static final int HEADER_SIZE = 8;

  /**
   * Size of a full atom header, in bytes.
   */
  public static final int FULL_HEADER_SIZE = 12;

  /**
   * Size of a long atom header, in bytes.
   */
  public static final int LONG_HEADER_SIZE = 16;

  /**
   * Value for the size field in an atom that defines its size in the largesize field.
   */
  public static final int DEFINES_LARGE_SIZE = 1;

  /**
   * Value for the size field in an atom that extends to the end of the file.
   */
  public static final int EXTENDS_TO_END_SIZE = 0;

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_ftyp = Utils.getIntegerCodeForString("ftyp");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_avc1 = Utils.getIntegerCodeForString("avc1");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_avc3 = Utils.getIntegerCodeForString("avc3");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_avcC = Utils.getIntegerCodeForString("avcC");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_hvc1 = Utils.getIntegerCodeForString("hvc1");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_hev1 = Utils.getIntegerCodeForString("hev1");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_hvcC = Utils.getIntegerCodeForString("hvcC");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_vp08 = Utils.getIntegerCodeForString("vp08");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_vp09 = Utils.getIntegerCodeForString("vp09");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_vpcC = Utils.getIntegerCodeForString("vpcC");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_av01 = Utils.getIntegerCodeForString("av01");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_av1C = Utils.getIntegerCodeForString("av1C");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dvav = Utils.getIntegerCodeForString("dvav");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dva1 = Utils.getIntegerCodeForString("dva1");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dvhe = Utils.getIntegerCodeForString("dvhe");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dvh1 = Utils.getIntegerCodeForString("dvh1");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dvcC = Utils.getIntegerCodeForString("dvcC");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dvvC = Utils.getIntegerCodeForString("dvvC");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_s263 = Utils.getIntegerCodeForString("s263");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_d263 = Utils.getIntegerCodeForString("d263");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mdat = Utils.getIntegerCodeForString("mdat");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mp4a = Utils.getIntegerCodeForString("mp4a");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE__mp3 = Utils.getIntegerCodeForString(".mp3");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_wave = Utils.getIntegerCodeForString("wave");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_lpcm = Utils.getIntegerCodeForString("lpcm");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_sowt = Utils.getIntegerCodeForString("sowt");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_ac_3 = Utils.getIntegerCodeForString("ac-3");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dac3 = Utils.getIntegerCodeForString("dac3");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_ec_3 = Utils.getIntegerCodeForString("ec-3");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dec3 = Utils.getIntegerCodeForString("dec3");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_ac_4 = Utils.getIntegerCodeForString("ac-4");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dac4 = Utils.getIntegerCodeForString("dac4");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dtsc = Utils.getIntegerCodeForString("dtsc");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dtsh = Utils.getIntegerCodeForString("dtsh");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dtsl = Utils.getIntegerCodeForString("dtsl");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dtse = Utils.getIntegerCodeForString("dtse");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_ddts = Utils.getIntegerCodeForString("ddts");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_tfdt = Utils.getIntegerCodeForString("tfdt");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_tfhd = Utils.getIntegerCodeForString("tfhd");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_trex = Utils.getIntegerCodeForString("trex");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_trun = Utils.getIntegerCodeForString("trun");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_sidx = Utils.getIntegerCodeForString("sidx");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_moov = Utils.getIntegerCodeForString("moov");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mvhd = Utils.getIntegerCodeForString("mvhd");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_trak = Utils.getIntegerCodeForString("trak");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mdia = Utils.getIntegerCodeForString("mdia");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_minf = Utils.getIntegerCodeForString("minf");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stbl = Utils.getIntegerCodeForString("stbl");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_esds = Utils.getIntegerCodeForString("esds");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_moof = Utils.getIntegerCodeForString("moof");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_traf = Utils.getIntegerCodeForString("traf");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mvex = Utils.getIntegerCodeForString("mvex");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mehd = Utils.getIntegerCodeForString("mehd");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_tkhd = Utils.getIntegerCodeForString("tkhd");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_edts = Utils.getIntegerCodeForString("edts");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_elst = Utils.getIntegerCodeForString("elst");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mdhd = Utils.getIntegerCodeForString("mdhd");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_hdlr = Utils.getIntegerCodeForString("hdlr");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stsd = Utils.getIntegerCodeForString("stsd");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_pssh = Utils.getIntegerCodeForString("pssh");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_sinf = Utils.getIntegerCodeForString("sinf");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_schm = Utils.getIntegerCodeForString("schm");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_schi = Utils.getIntegerCodeForString("schi");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_tenc = Utils.getIntegerCodeForString("tenc");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_encv = Utils.getIntegerCodeForString("encv");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_enca = Utils.getIntegerCodeForString("enca");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_frma = Utils.getIntegerCodeForString("frma");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_saiz = Utils.getIntegerCodeForString("saiz");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_saio = Utils.getIntegerCodeForString("saio");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_sbgp = Utils.getIntegerCodeForString("sbgp");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_sgpd = Utils.getIntegerCodeForString("sgpd");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_uuid = Utils.getIntegerCodeForString("uuid");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_senc = Utils.getIntegerCodeForString("senc");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_pasp = Utils.getIntegerCodeForString("pasp");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_TTML = Utils.getIntegerCodeForString("TTML");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_vmhd = Utils.getIntegerCodeForString("vmhd");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mp4v = Utils.getIntegerCodeForString("mp4v");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stts = Utils.getIntegerCodeForString("stts");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stss = Utils.getIntegerCodeForString("stss");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_ctts = Utils.getIntegerCodeForString("ctts");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stsc = Utils.getIntegerCodeForString("stsc");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stsz = Utils.getIntegerCodeForString("stsz");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stz2 = Utils.getIntegerCodeForString("stz2");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stco = Utils.getIntegerCodeForString("stco");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_co64 = Utils.getIntegerCodeForString("co64");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_tx3g = Utils.getIntegerCodeForString("tx3g");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_wvtt = Utils.getIntegerCodeForString("wvtt");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_stpp = Utils.getIntegerCodeForString("stpp");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_c608 = Utils.getIntegerCodeForString("c608");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_samr = Utils.getIntegerCodeForString("samr");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_sawb = Utils.getIntegerCodeForString("sawb");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_udta = Utils.getIntegerCodeForString("udta");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_meta = Utils.getIntegerCodeForString("meta");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_keys = Utils.getIntegerCodeForString("keys");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_ilst = Utils.getIntegerCodeForString("ilst");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_mean = Utils.getIntegerCodeForString("mean");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_name = Utils.getIntegerCodeForString("name");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_data = Utils.getIntegerCodeForString("data");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_emsg = Utils.getIntegerCodeForString("emsg");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_st3d = Utils.getIntegerCodeForString("st3d");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_sv3d = Utils.getIntegerCodeForString("sv3d");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_proj = Utils.getIntegerCodeForString("proj");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_camm = Utils.getIntegerCodeForString("camm");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_alac = Utils.getIntegerCodeForString("alac");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_alaw = Utils.getIntegerCodeForString("alaw");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_ulaw = Utils.getIntegerCodeForString("ulaw");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_Opus = Utils.getIntegerCodeForString("Opus");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dOps = Utils.getIntegerCodeForString("dOps");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_fLaC = Utils.getIntegerCodeForString("fLaC");

  @SuppressWarnings("ConstantCaseForConstants")
  public static final int TYPE_dfLa = Utils.getIntegerCodeForString("dfLa");

  public final int type;

  public Atom(int type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return getAtomTypeString(type);
  }

  /**
   * An MP4 atom that is a leaf.
   */
  /* package */ static final class LeafAtom extends Atom {

    /**
     * The atom data.
     */
    public final ParsableByteArray data;

    /**
     * @param type The type of the atom.
     * @param data The atom data.
     */
    public LeafAtom(int type, ParsableByteArray data) {
      super(type);
      this.data = data;
    }

  }

  /**
   * An MP4 atom that has child atoms.
   */
  /* package */ static final class ContainerAtom extends Atom {

    public final long endPosition;
    public final List<LeafAtom> leafChildren;
    public final List<ContainerAtom> containerChildren;

    /**
     * @param type The type of the atom.
     * @param endPosition The position of the first byte after the end of the atom.
     */
    public ContainerAtom(int type, long endPosition) {
      super(type);
      this.endPosition = endPosition;
      leafChildren = new ArrayList<>();
      containerChildren = new ArrayList<>();
    }

    /**
     * Adds a child leaf to this container.
     *
     * @param atom The child to add.
     */
    public void add(LeafAtom atom) {
      leafChildren.add(atom);
    }

    /**
     * Adds a child container to this container.
     *
     * @param atom The child to add.
     */
    public void add(ContainerAtom atom) {
      containerChildren.add(atom);
    }

    /**
     * Returns the child leaf of the given type.
     *
     * <p>If no child exists with the given type then null is returned. If multiple children exist
     * with the given type then the first one to have been added is returned.
     *
     * @param type The leaf type.
     * @return The child leaf of the given type, or null if no such child exists.
     */
    public @Nullable LeafAtom getLeafAtomOfType(int type) {
      int childrenSize = leafChildren.size();
      for (int i = 0; i < childrenSize; i++) {
        LeafAtom atom = leafChildren.get(i);
        if (atom.type == type) {
          return atom;
        }
      }
      return null;
    }

    /**
     * Returns the child container of the given type.
     *
     * <p>If no child exists with the given type then null is returned. If multiple children exist
     * with the given type then the first one to have been added is returned.
     *
     * @param type The container type.
     * @return The child container of the given type, or null if no such child exists.
     */
    public @Nullable ContainerAtom getContainerAtomOfType(int type) {
      int childrenSize = containerChildren.size();
      for (int i = 0; i < childrenSize; i++) {
        ContainerAtom atom = containerChildren.get(i);
        if (atom.type == type) {
          return atom;
        }
      }
      return null;
    }

    /**
     * Returns the total number of leaf/container children of this atom with the given type.
     *
     * @param type The type of child atoms to count.
     * @return The total number of leaf/container children of this atom with the given type.
     */
    public int getChildAtomOfTypeCount(int type) {
      int count = 0;
      int size = leafChildren.size();
      for (int i = 0; i < size; i++) {
        LeafAtom atom = leafChildren.get(i);
        if (atom.type == type) {
          count++;
        }
      }
      size = containerChildren.size();
      for (int i = 0; i < size; i++) {
        ContainerAtom atom = containerChildren.get(i);
        if (atom.type == type) {
          count++;
        }
      }
      return count;
    }

    @Override
    public String toString() {
      return getAtomTypeString(type)
          + " leaves: " + Arrays.toString(leafChildren.toArray())
          + " containers: " + Arrays.toString(containerChildren.toArray());
    }

  }

  /**
   * Parses the version number out of the additional integer component of a full atom.
   */
  public static int parseFullAtomVersion(int fullAtomInt) {
    return 0x000000FF & (fullAtomInt >> 24);
  }

  /**
   * Parses the atom flags out of the additional integer component of a full atom.
   */
  public static int parseFullAtomFlags(int fullAtomInt) {
    return 0x00FFFFFF & fullAtomInt;
  }

  /**
   * Converts a numeric atom type to the corresponding four character string.
   *
   * @param type The numeric atom type.
   * @return The corresponding four character string.
   */
  public static String getAtomTypeString(int type) {
    return "" + (char) ((type >> 24) & 0xFF)
        + (char) ((type >> 16) & 0xFF)
        + (char) ((type >> 8) & 0xFF)
        + (char) (type & 0xFF);
  }

}
