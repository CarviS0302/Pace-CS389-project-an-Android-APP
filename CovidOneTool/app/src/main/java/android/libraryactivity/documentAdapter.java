package android.libraryactivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_onetool.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//布局适配
public class documentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnLongClickListener{

    private ItemInnerClickListener mItemInnerClickListener;

    public interface ItemInnerClickListener {
        void onItemInnerClick(View view,int position);
    }

    public void setOnItemClickListener(ItemInnerClickListener mItemInnerClickListener) {
        this.mItemInnerClickListener = mItemInnerClickListener;
    }

    interface OnLongClickListener {
        void onLongClick(View view, int position);
    }

    private OnLongClickListener mOnLongClickListener = null;
    public void setOnLongClickListener(OnLongClickListener listener) {
        mOnLongClickListener = listener;
    }

    private Boolean multiSelect;//是否为多选待删除状态
    private Context mContext;//上下文
    public static List<document> mDocumentList = new ArrayList<>();//item holder
    //key为RecyclerView中各子项的position，value为该复选框的选中状态
    public static HashMap<Integer, Boolean> isSelected = new HashMap<>();

    //有item时，以卡片展示
    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView fileType;
        TextView fileName;
        TextView fileStatus;
        TextView expirationDate;
        TextView filePath;
        TextView tvDetail;
        ImageButton checkBtn;
        //不展示Description
        //TextView fileDescription;

        public ViewHolder(View view){
            super(view);
            cardView = (CardView) view;
            fileType = (TextView)view.findViewById(R.id.fileType);
            fileName = (TextView)view.findViewById(R.id.fileName);
            fileStatus = (TextView)view.findViewById(R.id.fileStatus);
            expirationDate = view.findViewById(R.id.expirationDate);
            filePath = view.findViewById(R.id.viewFile);
            tvDetail = view.findViewById(R.id.tvDetail);
            checkBtn = view.findViewById(R.id.checkBtn);
        }
    }

    //无item时，展示空布局
    static class EmptyViewHolder extends RecyclerView.ViewHolder{
        View empty_view;
        TextView textView;
        public EmptyViewHolder(View view){
            super(view);
            empty_view = view;
            textView = (TextView)view.findViewById(R.id.empty_document);
        }
    }


    //adapter的构造函数
    public documentAdapter(List<document> documentList, boolean multiSelect){
        mDocumentList = documentList;
        this.multiSelect = multiSelect;
    }

    //数据源为空返回-1
    @Override
    public int getItemViewType(int position){
        if(mDocumentList.size()<=0){
            return -1;
        }
        return super.getItemViewType(position);
    }

    //创建ViewHolder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext==null){
            mContext = parent.getContext();
        }
        //数据源为空时返回空布局
        if(viewType==-1){
            View view = LayoutInflater.from(mContext).inflate(R.layout.document_empty,parent, false);
            return new EmptyViewHolder(view);
        }
        //数据源不为空时返回卡片布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.document_item, parent, false);
        return new ViewHolder(view);
    }

    //logic
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //holder为卡片布局的holder
        if (holder instanceof ViewHolder){
            ((ViewHolder)holder).checkBtn.setTag(position);
            ((ViewHolder)holder).checkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mItemInnerClickListener!=null) {
                        mItemInnerClickListener.onItemInnerClick(view, position);
                    }
                }
            });
            //状态栏是否可见
            if(multiSelect){
                if(isSelected.containsKey(position)){
                    ((ViewHolder) holder).checkBtn.setBackgroundResource(R.drawable.checkbox);
                }else{
                    ((ViewHolder) holder).checkBtn.setBackgroundResource(R.drawable.checkbox_default);
                }
                ((ViewHolder) holder).checkBtn.setVisibility(View.VISIBLE);
            }else {
                //隐藏图片按钮
                isSelected.clear();
                ((ViewHolder)holder).checkBtn.setVisibility(View.GONE);
            }

            document mDocument = mDocumentList.get(position);
            ((ViewHolder)holder).fileType.setText(mDocument.getFileType());
            ((ViewHolder)holder).fileName.setText(mDocument.getFileName());
            if(mDocument.getFileStatus()){
                ((ViewHolder)holder).fileStatus.setText("Valid");
            }else {
                ((ViewHolder)holder).fileStatus.setText("Invalid");
            }
            ((ViewHolder)holder).expirationDate.setText(mDocument.getExpirationDate());
            ((ViewHolder)holder).tvDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mItemInnerClickListener!=null) {
                        mItemInnerClickListener.onItemInnerClick(view, position);
                    }
                }
            });
            ((ViewHolder)holder).filePath.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mItemInnerClickListener!=null) {
                        mItemInnerClickListener.onItemInnerClick(view, position);
                    }
                }
            });

            ((ViewHolder)holder).cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnLongClickListener.onLongClick(view,position);
                    return true;
                }
            });
        }
    }

    //告诉adapter有多少个item，以便留出足够空间
    @Override
    public int getItemCount() {
        return mDocumentList.size()>0? mDocumentList.size():1;
    }

    @Override
    public boolean onLongClick(View view) {
        if (null != mOnLongClickListener) {
            mOnLongClickListener.onLongClick(view, (int) view.getTag());
        }
        return true;
    }

    public Boolean getMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;
    }
}
