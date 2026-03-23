package com.liskovsoft.mediaserviceinterfaces;

import android.net.Uri;
import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup;
import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup.Item;
import io.reactivex.Observable;
import java.io.File;
import java.util.List;

public interface ChannelGroupService {
  List<ItemGroup> getChannelGroups();

  void addChannelGroup(ItemGroup group);

  void removeChannelGroup(ItemGroup group);

  ItemGroup createChannelGroup(String title, String iconUrl, List<Item> channels);

  void renameChannelGroup(ItemGroup channelGroup, String title);

  Item createChannel(String channelId, String title, String iconUrl);

  ItemGroup findChannelGroupById(String channelGroupId);

  ItemGroup findChannelGroupByTitle(String title);

  String[] findChannelIdsForGroup(String channelGroupId);

  Observable<List<ItemGroup>> importGroupsObserve(Uri uri);

  Observable<List<ItemGroup>> importGroupsObserve(File file);

  void exportData(String data);

  boolean isEmpty();
}
