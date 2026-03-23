package com.liskovsoft.sharedutils.okhttp.old;

import androidx.annotation.NonNull;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Dns;

public class PreferIpv4Dns implements Dns {
  @NonNull
  @Override
  public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
    InetAddress[] addresses = InetAddress.getAllByName(hostname);
    if (addresses == null || addresses.length == 0) {
      throw new UnknownHostException("Bad host: " + hostname);
    }

    // prefer IPv4; list IPv4 first
    ArrayList<InetAddress> result = new ArrayList<>();
    for (InetAddress address : addresses) {
      if (address instanceof Inet4Address) {
        result.add(address);
      }
    }
    for (InetAddress address : addresses) {
      if (!(address instanceof Inet4Address)) {
        result.add(address);
      }
    }

    return result;
  }
}
