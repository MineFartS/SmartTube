package com.google.android.exoplayer2.upstream.crypto;

import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;

/**
 * A {@link DataSource} that decrypts the data read from an upstream source.
 */
public final class AesCipherDataSource implements DataSource {

  private final DataSource upstream;
  private final byte[] secretKey;

  private @Nullable AesFlushingCipher cipher;

  public AesCipherDataSource(byte[] secretKey, DataSource upstream) {
    this.upstream = upstream;
    this.secretKey = secretKey;
  }

  @Override
  public void addTransferListener(TransferListener transferListener) {
    upstream.addTransferListener(transferListener);
  }

  @Override
  public long open(DataSpec dataSpec) throws IOException {
    long dataLength = upstream.open(dataSpec);
    long nonce = CryptoUtil.getFNV64Hash(dataSpec.key);
    cipher = new AesFlushingCipher(Cipher.DECRYPT_MODE, secretKey, nonce,
        dataSpec.absoluteStreamPosition);
    return dataLength;
  }

  @Override
  public int read(byte[] data, int offset, int readLength) throws IOException {
    if (readLength == 0) {
      return 0;
    }
    int read = upstream.read(data, offset, readLength);
    if (read == C.RESULT_END_OF_INPUT) {
      return C.RESULT_END_OF_INPUT;
    }
    cipher.updateInPlace(data, offset, read);
    return read;
  }

  @Override
  public @Nullable Uri getUri() {
    return upstream.getUri();
  }

  @Override
  public Map<String, List<String>> getResponseHeaders() {
    return upstream.getResponseHeaders();
  }

  @Override
  public void close() throws IOException {
    cipher = null;
    upstream.close();
  }
}
