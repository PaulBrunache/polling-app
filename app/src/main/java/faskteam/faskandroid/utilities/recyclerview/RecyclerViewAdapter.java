package faskteam.faskandroid.utilities.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * Abstract class extending Base RecyclerView.Adapter class.
 * <p>
 * Subclasses require the passing of parameter &lt;T&gt;, where T is a class extending
 * the Object class. This allows re-usability of this class for many implementations of
 * a RecyclerView Adapter
 * </p>
 * <p>
 * Adds functionality to superclass, implementing methods that can add and remove items from the
 * arraylist, <b>mDataList</b> as well as a custom onItemClickListener {@link OnItemClickListener}
 * that passes back both the view object that was clicked and its position in the list.
 * </p>
 *
 * @param <T> extends Object
 * @param <VH> extends RecyclerViewAdapter.ViewHolder
 */
public abstract class RecyclerViewAdapter<T, VH extends RecyclerViewAdapter.ViewHolder> extends RecyclerView.Adapter<VH>
        implements ItemTouchHelperAdapter {

    protected final LayoutInflater mInflater;
    //associated data list for rows.
    protected final List<T> mDataList = new ArrayList<>();
    protected Context mContext;

    protected OnItemClickListener mListener;
    protected OnMenuItemSelectedListener mMenuListner;

    public RecyclerViewAdapter(Context context) {
        //adapters need an inflater to inflate the layout for each row
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    /**
     * Returns the number of rows to display in the adapter view. Whatever number is set here
     * determines the number of row that will be included in the adapter view.
     * @return int
     */
    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /**
     * Adds an item T to the end data ArrayList.
     * @param item
     */
    public void addItem(T item) {
        mDataList.add(item);
        this.notifyItemInserted(mDataList.size() - 1);
    }

    public void addItems(List<T> items) {
        mDataList.addAll(items);
    }

    public void clearDataList() {
        mDataList.clear();
        this.notifyDataSetChanged();
    }

    public void replaceDataList(List<T> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        this.notifyDataSetChanged();

    }

    public List<T> getDataList() {
        return mDataList;
    }

    /**
     * Retrieves and returns an item from the data ArrayList located at the passed in position
     * @param position
     * @return T
     */
    public T getItem(int position) {
        return mDataList.get(position);
    }

    /**
     * Removes and returns an item from the data ArrayList located at the passed in position
     * @param position
     * @return T
     */
    public T removeItem(int position) {
        T item = mDataList.remove(position);
        this.notifyItemRemoved(position);
        return item;
    }

    /**
     * Removes and returns a list of items from the data ArrayList located at each position in the
     * positions List
     * @param positions
     * @return List&lt;T&gt;
     */
    public List<T> removeItems(List<Integer> positions) {
        List<T> removedItems = new ArrayList<>(positions.size());
        Collections.sort(positions);

        int startPosition = positions.get(0);
        int count = 0;
        for (int i = 0; i < positions.size(); i++) {
            int currentPosition = positions.get(i);
            if (currentPosition - startPosition > i) {
                notifyItemRangeRemoved(startPosition, count);
                startPosition = currentPosition;
                count = 0;
            }
            removedItems.add(mDataList.remove(currentPosition));
            count++;
        }
        notifyItemRangeRemoved(startPosition, count);

        return removedItems;
    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mDataList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mDataList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * sets OnItemClickListener for adapter.
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setOnMenuSelectedListner(OnMenuItemSelectedListener listener) {
        this.mMenuListner = listener;
    }

    /**
     * Subclass of {@link RecyclerView.ViewHolder}
     * Class already implements an onClickListener, which listens to any click on the
     * passed in itemView and calls the onItemClick() method from the associated OnItemClickListener
     * if it exists.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    /**
     * Created by Sam.I on 10/20/2015.
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);

    }

    public interface OnMenuItemSelectedListener {
        void onMenuItemSelected(View view, int position, int id);
    }
}
