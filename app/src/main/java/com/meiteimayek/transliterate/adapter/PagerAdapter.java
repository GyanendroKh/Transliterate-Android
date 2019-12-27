package com.meiteimayek.transliterate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.meiteimayek.transliterate.R;
import com.meiteimayek.transliterate.common.Step;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PagerAdapter extends RecyclerView.Adapter<PagerAdapter.PagerViewHolder> {
  
  private final List<Step> mList;
  
  public PagerAdapter(List<Step> list) {
    super();
    mList = list;
  }
  
  @NonNull
  @Override
  public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.fragment_tutorial, parent, false);
    return new PagerViewHolder(view);
  }
  
  @Override
  public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
    holder.bind(mList.get(position));
  }
  
  @Override
  public int getItemCount() {
    return mList.size();
  }
  
  public class PagerViewHolder extends RecyclerView.ViewHolder {
    final View mRoot;
    @BindView(R.id.step_img)
    AppCompatImageView mImg;
    @BindView(R.id.step_title)
    MaterialTextView mTitle;
    @BindView(R.id.step_desc)
    MaterialTextView mDesc;
    
    public PagerViewHolder(@NonNull View itemView) {
      super(itemView);
      mRoot = itemView;
      ButterKnife.bind(this, itemView);
    }
    
    private void bind(Step step) {
      mImg.setImageResource(step.getImage());
      mTitle.setText(step.getTitle());
      mDesc.setText(step.getDesc());
    }
  }
  
}
