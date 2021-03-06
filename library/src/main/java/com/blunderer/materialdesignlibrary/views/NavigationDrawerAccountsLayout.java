package com.blunderer.materialdesignlibrary.views;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.blunderer.materialdesignlibrary.R;
import com.blunderer.materialdesignlibrary.adapters.NavigationDrawerAccountsMenuAdapter;
import com.blunderer.materialdesignlibrary.listeners.OnAccountChangeListener;
import com.blunderer.materialdesignlibrary.listeners.OnMoreAccountClickListener;
import com.blunderer.materialdesignlibrary.models.Account;
import com.blunderer.materialdesignlibrary.models.NavigationDrawerAccountsListItem;
import com.blunderer.materialdesignlibrary.models.NavigationDrawerAccountsListItemAccount;

import java.util.List;

public class NavigationDrawerAccountsLayout extends LinearLayout {

    private List<Account> mAccounts;
    private ImageView mAccountsMenuSwitch;
    private ImageView mBackground;
    private TextView mTitle;
    private TextView mDescription;
    private RoundedImageView mPicture;
    private RoundedImageView mSecondPicture;
    private RoundedImageView mThirdPicture;
    private NavigationDrawerAccountsMenuAdapter mAccountsMenuAdapter;
    private ListView mOriginalListView;
    private ListAdapter mOriginalAdapter;
    private List<NavigationDrawerAccountsListItem> mAccountsMenuItems;
    private ViewGroup mMainLayout;
    private int mOriginalListViewSelectedItemPosition;
    private boolean mIsAccountsMenuEnabled;
    private boolean mShowAccountMenu = false;
    private OnAccountChangeListener mOnAccountChangeListener;

    public int[] mAccountsPositions;
    public boolean isRestored = false;

    public NavigationDrawerAccountsLayout(Context context) {
        this(context, null);
    }

    public NavigationDrawerAccountsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.mdl_navigation_drawer_accounts, this, true);

        mMainLayout = (ViewGroup) getChildAt(0);

        setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT));
        setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics()));
        setOrientation(VERTICAL);

        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                updateMoreAccountsList();

                if (Build.VERSION.SDK_INT >= 16) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }

        });
    }

    /**
     * Set a ListView that will be used to show the Accounts Menu
     * when clicking on the Accounts Layout.
     * You should also set its adapter by calling setListViewAdapter.
     *
     * @param listView The ListView that is used to show the Accounts Menu.
     */
    public void setListView(ListView listView) {
        if (listView == null) throw new IllegalArgumentException("The ListView must not be null");

        mOriginalListView = listView;
        if (mOriginalAdapter != null) initAccountsLayout();
    }

    /**
     * Set Adapter for the Accounts Menu ListView.
     * You should also set the ListView by calling setListView.
     *
     * @param adapter The Adapter that is used by the Accounts Menu ListView.
     */
    public void setListViewAdapter(ListAdapter adapter) {
        if (adapter == null)
            throw new IllegalArgumentException("The ListView Adapter must not be null");

        mOriginalAdapter = adapter;
        if (mOriginalListView != null) initAccountsLayout();
    }

    /**
     * Set a List of Accounts that will be displayed.
     *
     * @param accounts the List of Accounts displayed.
     */
    public void setAccounts(List<Account> accounts) {
        if (accounts == null || accounts.size() <= 0) {
            throw new IllegalArgumentException(
                    "The accounts list must contain at least one account");
        }

        mAccounts = accounts;
        if (isRestored) isRestored = false;
        else resetAccountsPositions(accounts.size());
        initAccounts();
    }

    /**
     * Set a List of Items for the Accounts Menu.
     *
     * @param accountsMenuItems the List of items for the Accounts Menu.
     */
    public void setNavigationDrawerAccountsMenuItems(
            List<NavigationDrawerAccountsListItem> accountsMenuItems) {
        if (accountsMenuItems != null && accountsMenuItems.size() > 0) {
            mShowAccountMenu = true;
            mAccountsMenuItems = accountsMenuItems;
            initAccountsMenuSwitch();
            mAccountsMenuAdapter = new NavigationDrawerAccountsMenuAdapter(
                    getContext(),
                    R.layout.mdl_navigation_drawer_row,
                    mAccountsMenuItems);
        }
    }

    /**
     * Register a callback to be invoked when the current account has changed.
     *
     * @param onAccountChangeListener The callback that will run.
     */
    public void setOnAccountChangeListener(OnAccountChangeListener onAccountChangeListener) {
        mOnAccountChangeListener = onAccountChangeListener;
    }

    public void notifyListTopItemSelectedChanged() {
        mOriginalListViewSelectedItemPosition = mOriginalListView.getCheckedItemPosition();
    }

    private void changeAccount() {
        Account currentAccount = getAccount(0);
        if (currentAccount == null) return;

        if (currentAccount.useBackgroundResource()) {
            mBackground.setImageResource(currentAccount.getBackgroundResource());
        }

        mPicture.setImageResource(currentAccount.getPictureResource());

        if (!TextUtils.isEmpty(currentAccount.getTitle())) {
            mTitle.setText(currentAccount.getTitle());
        } else mTitle.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(currentAccount.getDescription())) {
            mDescription.setText(currentAccount.getDescription());
        } else mDescription.setVisibility(View.GONE);

        Account secondAccount = getAccount(1);
        if (secondAccount == null) return;
        mSecondPicture.setImageResource(secondAccount.getPictureResource());

        Account thirdAccount = getAccount(2);
        if (thirdAccount == null) return;
        mThirdPicture.setImageResource(thirdAccount.getPictureResource());
    }

    private void resetAccountsPositions(int size) {
        mAccountsPositions = new int[size];
        for (int i = 0; i < size; ++i) mAccountsPositions[i] = i;
    }

    private Account getAccount(int position) {
        if (mAccountsPositions == null || position >= mAccountsPositions.length) return null;

        if (mAccountsPositions[position] < 0 || mAccountsPositions[position] >= mAccounts.size()) {
            if (mAccounts.size() > position) mAccountsPositions[position] = position;
            else return null;
        }
        return mAccounts.get(mAccountsPositions[position]);
    }

    private void initAccounts() {
        ViewGroup dataLayout = (ViewGroup) mMainLayout.getChildAt(1);
        ViewGroup dataPicturesLayout = (ViewGroup) dataLayout.getChildAt(0);
        ViewGroup dataTextViewsLayout = (ViewGroup) ((ViewGroup) dataLayout.getChildAt(1))
                .getChildAt(0);
        mBackground = (ImageView) mMainLayout.getChildAt(0);
        mPicture = (RoundedImageView) dataPicturesLayout.getChildAt(0);
        mTitle = (TextView) dataTextViewsLayout.getChildAt(0);
        mDescription = (TextView) dataTextViewsLayout.getChildAt(1);

        if (getAccount(1) != null) {
            mSecondPicture = (RoundedImageView) dataPicturesLayout.getChildAt(2);
            mSecondPicture.setVisibility(VISIBLE);
            mSecondPicture.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int tmpPosition = mAccountsPositions[0];
                    mAccountsPositions[0] = mAccountsPositions[1];
                    if (getAccount(2) == null) mAccountsPositions[1] = tmpPosition;
                    else {
                        mAccountsPositions[1] = mAccountsPositions[2];
                        mAccountsPositions[2] = tmpPosition;
                    }
                    changeAccount();
                    if (mOnAccountChangeListener != null) {
                        mOnAccountChangeListener
                                .onAccountChange(mAccounts.get(mAccountsPositions[0]));
                    }
                }

            });
        }

        if (getAccount(2) != null) {
            mThirdPicture = (RoundedImageView) dataPicturesLayout.getChildAt(1);
            mThirdPicture.setVisibility(VISIBLE);
            mThirdPicture.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int tmpPosition = mAccountsPositions[0];
                    mAccountsPositions[0] = mAccountsPositions[2];
                    mAccountsPositions[2] = tmpPosition;
                    changeAccount();
                    if (mOnAccountChangeListener != null) {
                        mOnAccountChangeListener
                                .onAccountChange(mAccounts.get(mAccountsPositions[0]));
                    }
                }

            });
        }

        changeAccount();
    }

    private void initAccountsLayout() {
        mIsAccountsMenuEnabled = false;

        mMainLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mShowAccountMenu) return;

                if (mIsAccountsMenuEnabled) {
                    mOriginalListView.setAdapter(mOriginalAdapter);
                    if (mOriginalListViewSelectedItemPosition != ListView.INVALID_POSITION) {
                        mOriginalListView.setItemChecked(
                                mOriginalListViewSelectedItemPosition, true);
                    }
                    mAccountsMenuSwitch.setImageResource(R.drawable.ic_arrow_drop_down);
                } else {
                    mOriginalListViewSelectedItemPosition = mOriginalListView
                            .getCheckedItemPosition();
                    mOriginalListView.setAdapter(mAccountsMenuAdapter);
                    mAccountsMenuSwitch.setImageResource(R.drawable.ic_arrow_drop_up);
                }

                mIsAccountsMenuEnabled = !mIsAccountsMenuEnabled;
            }

        });
    }

    private void initAccountsMenuSwitch() {
        ViewGroup switchLayout = (ViewGroup) ((ViewGroup) mMainLayout.getChildAt(1)).getChildAt(1);
        mAccountsMenuSwitch = (ImageView) switchLayout.getChildAt(1);
        mAccountsMenuSwitch.setImageResource(R.drawable.ic_arrow_drop_down);
        mAccountsMenuSwitch.setVisibility(VISIBLE);
    }

    private void updateMoreAccountsList() {
        if (mAccounts != null && mAccounts.size() > 3 &&
                mAccountsMenuItems != null && mAccountsMenuAdapter != null) {
            NavigationDrawerAccountsListItemAccount moreAccount;
            for (int i = (mAccounts.size() - 1); i >= 3; --i) {
                Account account = mAccounts.get(i);
                moreAccount = new NavigationDrawerAccountsListItemAccount(getContext());
                moreAccount.setTitle(account.getTitle());
                moreAccount.setOnClickListener(generateAccountClickListener(i));
                if (account.getPictureResource() != -1) {
                    moreAccount.setIcon(getContext(), account.getPictureResource());
                }
                mAccountsMenuItems.add(0, moreAccount);
            }
            mAccountsMenuAdapter.notifyDataSetChanged();
        }
    }

    private OnMoreAccountClickListener generateAccountClickListener(final int position) {
        return new OnMoreAccountClickListener() {

            @Override
            public void onMoreAccountClick(View v, int i) {
                int tmpPosition = mAccountsPositions[0];
                mAccountsPositions[0] = position;

                Account account = mAccounts.get(mAccountsPositions[1]);
                NavigationDrawerAccountsListItemAccount moreAccount =
                        new NavigationDrawerAccountsListItemAccount(getContext());
                moreAccount.setTitle(account.getTitle());
                moreAccount.setOnClickListener(
                        generateAccountClickListener(mAccountsPositions[1]));
                if (account.getPictureResource() != -1) {
                    moreAccount.setIcon(getContext(), account.getPictureResource());
                }
                mAccountsMenuItems.remove(i - 1);
                mAccountsMenuItems.add(0, moreAccount);
                mAccountsMenuAdapter.notifyDataSetChanged();

                mAccountsPositions[1] = mAccountsPositions[2];
                mAccountsPositions[2] = tmpPosition;
                changeAccount();
                if (mOnAccountChangeListener != null) {
                    mOnAccountChangeListener
                            .onAccountChange(mAccounts.get(mAccountsPositions[0]));
                }
            }

        };
    }

}
