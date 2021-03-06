package com.hapramp.interfaces;

import com.hapramp.datamodels.response.UserStatsModel;

/**
 * Created by Ankit on 12/23/2017.
 */

public interface UserStatsCallback {
  void onUserStatsFetched(UserStatsModel stats);

  void onUserStatsFetchError();
}
