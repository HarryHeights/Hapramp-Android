package com.hapramp.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hapramp.R;
import com.hapramp.preferences.HaprampPreferenceManager;
import com.hapramp.search.FollowingSearchManager;
import com.hapramp.steemconnect.SteemConnectUtils;
import com.hapramp.steemconnect4j.SteemConnect;
import com.hapramp.steemconnect4j.SteemConnectCallback;
import com.hapramp.steemconnect4j.SteemConnectException;
import com.hapramp.ui.activity.ProfileActivity;
import com.hapramp.utils.Constants;
import com.hapramp.utils.ImageHandler;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ankit on 4/6/2018.
 */

public class UserSearchItemView extends FrameLayout implements FollowingSearchManager.FollowingSearchCallback {

  @BindView(R.id.user_pic)
  ImageView userPic;
  @BindView(R.id.content)
  TextView content;
  @BindView(R.id.followUnfollowBtn)
  TextView followUnfollowBtn;
  @BindView(R.id.followUnfollowProgress)
  ProgressBar followUnfollowProgress;
  private boolean followed = false;
  private Context mContext;
  private Handler mHandler;
  private String mUsername;
  private String me;
  private SteemConnect steemConnect;
  private FollowingSearchManager followingSearchManager;

  public UserSearchItemView(@NonNull Context context) {
    super(context);
    init(context);
  }

  private void init(Context context) {
    this.mContext = context;
    View view = LayoutInflater.from(context).inflate(R.layout.user_suggestions_item_row, this);
    ButterKnife.bind(this, view);
    mHandler = new Handler();
    me = HaprampPreferenceManager.getInstance().getCurrentSteemUsername();
    followingSearchManager = new FollowingSearchManager(this);
    steemConnect = SteemConnectUtils.getSteemConnectInstance(HaprampPreferenceManager.getInstance().getSC2AccessToken());
    attachListeners();
  }

  private void attachListeners() {
    followUnfollowBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isFollowed()) {
          confirmUnfollowAction();
        } else {
          requestFollowOnSteem();
        }
      }
    });

    content.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(mContext, ProfileActivity.class);
        i.putExtra(Constants.EXTRAA_KEY_STEEM_USER_NAME, getUsername());
        mContext.startActivity(i);
      }
    });

  }

  private boolean isFollowed() {
    return this.followed;
  }

  private void confirmUnfollowAction() {
    AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
      .setTitle("Unfollow")
      .setMessage("Do you want to Unfollow " + getUsername() + " ?")
      .setPositiveButton("UnFollow", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          requestUnFollowOnSteem();
        }
      })
      .setNegativeButton("No", null);
    builder.show();
  }

  private void requestFollowOnSteem() {
    showProgress(true);
    new Thread() {
      @Override
      public void run() {
        steemConnect.follow(
          me,
          mUsername,
          new SteemConnectCallback() {
            @Override
            public void onResponse(String s) {
              mHandler.post(new Runnable() {
                @Override
                public void run() {
                  userFollowedOnSteem();
                }
              });
            }

            @Override
            public void onError(SteemConnectException e) {
              userFollowFailed();
            }
          }
        );
      }
    }.start();
  }

  private String getUsername() {
    return this.mUsername;
  }

  private void requestUnFollowOnSteem() {
    showProgress(true);
    new Thread() {
      @Override
      public void run() {
        steemConnect.unfollow(
          me,
          mUsername,
          new SteemConnectCallback() {
            @Override
            public void onResponse(String s) {
              mHandler.post(new Runnable() {
                @Override
                public void run() {
                  userUnFollowedOnSteem();
                }
              });
            }

            @Override
            public void onError(SteemConnectException e) {
              userUnfollowFailed();
            }
          }
        );
      }
    }.start();
  }

  private void showProgress(boolean show) {
    if (show) {
      //hide button
      followUnfollowBtn.setVisibility(GONE);
      followUnfollowProgress.setVisibility(VISIBLE);
    } else {
      //show button
      followUnfollowBtn.setVisibility(VISIBLE);
      followUnfollowProgress.setVisibility(GONE);
    }
  }

  private void userFollowedOnSteem() {
    showProgress(false);
    alreadyFollowed();
    syncFollowings();
    t("You started following " + getUsername());
  }

  private void userFollowFailed() {
    showProgress(false);
    notFollowed();
    t("Failed to follow " + getUsername());
  }

  private void userUnFollowedOnSteem() {
    showProgress(false);
    notFollowed();
    syncFollowings();
    t("You unfollowed " + getUsername());
  }

  private void userUnfollowFailed() {
    showProgress(false);
    alreadyFollowed();
    t("Failed to unfollow " + getUsername());
  }

  private void alreadyFollowed() {
    followUnfollowBtn.setText("Unfollow");
    followUnfollowBtn.setSelected(true);
    followUnfollowBtn.setBackgroundResource(R.drawable.unfollow_btn_bg);
    followed = true;
  }

  private void syncFollowings() {
    followingSearchManager.requestFollowings(me);
  }

  private void t(String s) {
    Toast.makeText(mContext, s, Toast.LENGTH_LONG).show();
  }

  private void notFollowed() {
    followUnfollowBtn.setText("Follow");
    followUnfollowBtn.setSelected(false);
    followUnfollowBtn.setBackgroundResource(R.drawable.follow_btn_bg);
    followed = false;
  }

  public void setUsername(String username) {
    this.mUsername = username;
    content.setText(username);
    ImageHandler.loadCircularImage(mContext, userPic, String.format(mContext.getResources().getString(R.string.steem_user_profile_pic_format), username));
    invalidateFollowButton();
  }

  private void invalidateFollowButton() {
    if (mUsername.equals(me)) {
      followUnfollowBtn.setVisibility(GONE);
      return;
    }
    Set<String> followings = HaprampPreferenceManager.getInstance().getFollowingsSet();
    if (followings.contains(mUsername)) {
      alreadyFollowed();
    } else {
      notFollowed();
    }
  }

  public UserSearchItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public UserSearchItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  @Override
  public void onFollowingResponse(ArrayList<String> followings) {
    HaprampPreferenceManager.getInstance().saveCurrentUserFollowings(followings);
  }

  @Override
  public void onFollowingRequestError(String e) {

  }
}
