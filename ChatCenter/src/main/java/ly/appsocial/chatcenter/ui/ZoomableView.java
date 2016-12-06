package ly.appsocial.chatcenter.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ly.appsocial.chatcenter.R;

public class ZoomableView extends RelativeLayout implements View.OnTouchListener{

    private TextView mSelectedSessionLabel;
    private ImageButton mBtScaleToTop;
    private ImageButton mBtScaleToBottom;
    private ImageButton mBtRemoveZoomView;
    private View mRootView;

    private OnScaleButtonTouchListener mListener;
    private int mMinHeight;
    private int mMinY = 0;
    private int mMaxY = 0;

    /** Initial state of TextView */
    private int mInitTvHeight = 0;

    /** Initial state of Root View */
    private LayoutParams mInitRootParams;

    private int mNumberOfSlots = 0;
    private int mTouchDownY = 0;
    private int _yDeltaRootView;

    private boolean isScaleToTop = false;
    private boolean isScaleToBottom = false;

    public ZoomableView(Context context) {
        super(context);
        setZoomableView();
    }

    public ZoomableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setZoomableView();
    }

    private void setZoomableView() {

        mMinHeight = getContext().getResources().getDimensionPixelSize(R.dimen.time_block_height);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        mRootView = inflater.inflate(R.layout.view_zoomable, this, true);

        mBtScaleToBottom = (ImageButton) mRootView.findViewById(R.id.button_scale_to_bottom);
        mBtScaleToTop = (ImageButton) mRootView.findViewById(R.id.button_scale_to_top);
        mSelectedSessionLabel = (TextView) mRootView.findViewById(R.id.text_view);
        mBtRemoveZoomView = (ImageButton) mRootView.findViewById(R.id.button_remove_zoom_view);

        mSelectedSessionLabel.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        mBtScaleToBottom.setOnTouchListener(this);
        mBtScaleToTop.setOnTouchListener(this);
        mBtRemoveZoomView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onDeleteZoomView(ZoomableView.this);
                }
            }
        });
    }


    public boolean onTouch(View view, MotionEvent event) {
        final int Y = (int) event.getRawY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isScaleToTop = false;
                isScaleToBottom = false;

                mTouchDownY = Y;
                /** Initial state of TextView*/
                mInitTvHeight = mSelectedSessionLabel.getHeight();

                /** Initial state of RootView*/
                mInitRootParams = (LayoutParams) mRootView.getLayoutParams();
                _yDeltaRootView = Y - mInitRootParams.topMargin;

                if (mListener != null) {
                    mListener.onTouchBtScaleDown(this);
                }
                break;

            case MotionEvent.ACTION_UP:
                mNumberOfSlots = getNumberOfMaskedRows();
                resizeToFitWithRowsHeight(mNumberOfSlots);

                if (mListener != null) {
                    mListener.onTouchBtScaleUp(this);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if(view.getId() == R.id.button_scale_to_bottom) {
                    isScaleToBottom = true;
                    onResizeToBottom(event);
                } else if (view.getId() == R.id.button_scale_to_top) {
                    isScaleToTop = true;
                    onResizeToTop(event);
                }

                break;
        }
        return true;
    }

    private void onResizeToTop(MotionEvent event) {
        /** Get new height for TextView */
        int tvNewHeight = mInitTvHeight - (int) event.getRawY() + mTouchDownY;

        /** Get new height for TextView */
        int rvNewHeight = tvNewHeight + 2 * getResources().getDimensionPixelSize(R.dimen.resizable_view_margin);
        int topMargin = (int) (event.getRawY() - _yDeltaRootView);

        if (tvNewHeight >= mMinHeight && topMargin >= mMinY) {
            /** Set new height for RootView */
            LayoutParams rvParams = (LayoutParams) mRootView.getLayoutParams();
            rvParams.topMargin = topMargin;
            rvParams.height = rvNewHeight;
            mRootView.setLayoutParams(rvParams);


            /** Set new height for TextView */
            LayoutParams tvParams = (LayoutParams) mSelectedSessionLabel.getLayoutParams();
            tvParams.height = tvNewHeight;
            mSelectedSessionLabel.setLayoutParams(tvParams);
        }
    }

    private void onResizeToBottom(MotionEvent event) {
        /** Get new height for TextView */
        int tvNewHeight = mInitTvHeight + (int) event.getRawY() - mTouchDownY;
        /** Get new height for RootView */
        int rvNewHeight = tvNewHeight + 2 * getResources().getDimensionPixelSize(R.dimen.resizable_view_margin);
        /** Set new height for RootView */
        LayoutParams rvParams = (LayoutParams) mRootView.getLayoutParams();
        if (tvNewHeight >= mMinHeight && (rvParams.topMargin + rvNewHeight) <= mMaxY) {

            rvParams.height = rvNewHeight;
            mRootView.setLayoutParams(rvParams);

            /** Set new height for TextView */
            LayoutParams textParams = (LayoutParams) mSelectedSessionLabel.getLayoutParams();
            textParams.height = tvNewHeight;
            mSelectedSessionLabel.setLayoutParams(textParams);
        }
    }

    private void resizeToFitWithRowsHeight(int numberOfSlots) {
        /** Get new height for TextView*/
        LayoutParams tvParams = (LayoutParams) mSelectedSessionLabel.getLayoutParams();
        tvParams.height = numberOfSlots * getResources().getDimensionPixelSize(R.dimen.time_block_height);

        /** Get new height for RootView*/
        LayoutParams rvParams = (LayoutParams) mRootView.getLayoutParams();
        rvParams.height = tvParams.height + 2 * getResources().getDimensionPixelSize(R.dimen.resizable_view_margin);

        /** Set new height for views*/
        mRootView.setLayoutParams(rvParams);
        mSelectedSessionLabel.setLayoutParams(tvParams);
    }

    private int getNumberOfMaskedRows() {
        int numberOfSlots;
        int textHeight = mSelectedSessionLabel.getHeight();
        int slotHeight = getResources().getDimensionPixelSize(R.dimen.time_block_height);

        /** Get number of slot was masked */
        numberOfSlots = textHeight / slotHeight;
        if (textHeight > (numberOfSlots + 0.25) * slotHeight) {
            numberOfSlots += 1;
        }

        return numberOfSlots;
    }

    public void setListener(OnScaleButtonTouchListener listener) {
        mListener = listener;
    }

    public int getNumberOfSlots() {
        return mNumberOfSlots;
    }

    public void setSelectedSessionLabel(String label) {
        mSelectedSessionLabel.setText(label);
    }

    public boolean isScaleToTop() {
        return isScaleToTop;
    }

    public boolean isScaleToBottom() {
        return isScaleToBottom;
    }

    public void setMinY(int minY) {
        mMinY = minY;
    }

    public void setMaxY(int maxY) {
        mMaxY = maxY;
    }

    public int getTopY() {
        LayoutParams layoutParams = (LayoutParams) mRootView.getLayoutParams();
        int topY = layoutParams.topMargin + 2 * getResources().getDimensionPixelSize(R.dimen.resizable_view_margin);
        return topY;
    }

    public int getBottomY() {
        LayoutParams layoutParams = (LayoutParams) mRootView.getLayoutParams();
        int bottomY = layoutParams.topMargin + mRootView.getHeight()
                - 2 * getResources().getDimensionPixelSize(R.dimen.resizable_view_margin);
        return bottomY ;
    }

    public void setZoomToTopEnable(boolean enable) {
        mBtScaleToTop.setVisibility(enable ? VISIBLE:INVISIBLE);
    }

    public void setZoomToBottomEnable(boolean enable) {
        mBtScaleToBottom.setVisibility(enable ? VISIBLE:INVISIBLE);
    }

    public interface OnScaleButtonTouchListener {
        void onTouchBtScaleDown(ZoomableView view);
        void onTouchBtScaleUp(ZoomableView view);
        void onDeleteZoomView(ZoomableView view);
    }
}
