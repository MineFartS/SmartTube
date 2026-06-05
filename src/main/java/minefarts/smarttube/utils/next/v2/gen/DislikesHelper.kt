package minefarts.smarttube.utils.next.v2.gen

import minefarts.smarttube.google.common.helpers.ServiceHelper

internal fun DislikesResult.getDislikeCount() = dislikes?.let { if (it > 0) ServiceHelper.prettyCount(it) else null }
internal fun DislikesResult.getLikeCount() = likes?.let { if (it > 0) ServiceHelper.prettyCount(it) else null }