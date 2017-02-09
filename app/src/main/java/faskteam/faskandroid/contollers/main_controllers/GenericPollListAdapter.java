package faskteam.faskandroid.contollers.main_controllers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import faskteam.faskandroid.R;
import faskteam.faskandroid.entities.Poll;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;

public class GenericPollListAdapter extends RecyclerViewAdapter<Poll, GenericPollListAdapter.MyViewHolder> {

    public GenericPollListAdapter(Context context) {
        super(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.poll_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Poll poll = mDataList.get(position);
        holder.pollQuestion.setText(poll.getQuestion());

        holder.pollCreator.setText(poll.getCreatorUsername());
        holder.pollTimeRemaining.setText(poll.getTimeRemaining());

    }

    protected class MyViewHolder extends RecyclerViewAdapter.ViewHolder {

        TextView pollCreator;
        TextView pollTimeRemaining;
        TextView pollQuestion;
        TextView pollBody;
        TextView pollTag1;
        TextView pollTag2;
        TextView pollTag3;

        public MyViewHolder(View itemView) {
            super(itemView);

            pollCreator = (TextView) itemView.findViewById(R.id.poll_creator);
            pollTimeRemaining = (TextView) itemView.findViewById(R.id.poll_time_remaining);
            pollQuestion = (TextView) itemView.findViewById(R.id.poll_question);
            pollBody = (TextView) itemView.findViewById(R.id.poll_body);
//            pollTag1 = (TextView) itemView.findViewById(R.id.poll_tag_1);
//            pollTag2 = (TextView) itemView.findViewById(R.id.poll_tag_2);
//            pollTag3 = (TextView) itemView.findViewById(R.id.poll_tag_3);
        }
    }
}
