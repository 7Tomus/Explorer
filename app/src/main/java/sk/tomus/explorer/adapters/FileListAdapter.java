package sk.tomus.explorer.adapters;

import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import sk.tomus.explorer.MainActivity;
import sk.tomus.explorer.R;
import sk.tomus.explorer.miscellaneous.App;
import sk.tomus.explorer.miscellaneous.SelectableAdapter;


public class FileListAdapter extends SelectableAdapter<FileListAdapter.FileViewHolder> {

    private List<File> fileList;
    private FileViewHolder.ClickListener clickListener;

    public static class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener{

        public TextView fileName, fileSize;
        public ImageView fileIcon;
        public View selectedOverlay;
        private ClickListener listener;

        public FileViewHolder(View view, ClickListener listener){
            super(view);

            this.listener = listener;
            fileName = (TextView) view.findViewById(R.id.textFileName);
            fileSize = (TextView) view.findViewById(R.id.textFileSize);
            fileIcon = (ImageView) view.findViewById(R.id.imageFileIcon);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                return listener.onItemLongClicked(getPosition());
            }

            return false;
        }
        public interface ClickListener {
            public void onItemClicked(int position);
            public boolean onItemLongClicked(int position);
        }
    }

    public FileListAdapter(List<File> fileList, FileViewHolder.ClickListener clickListener){
        this.fileList = fileList;
        this.clickListener = clickListener;
    }

    public void setFiles(List<File> files) {
        fileList.clear();
        fileList.addAll(files);
        notifyDataSetChanged();
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView;
        if(App.instance.getCurrentActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_vertical, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_grid, parent, false);
        }
        return new FileViewHolder(itemView, clickListener);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position){
        File file = fileList.get(position);
        //icon
        if(file.isDirectory()){
            holder.fileIcon.setImageResource(R.mipmap.icon_folder);
        }else{
            holder.fileIcon.setImageResource(R.mipmap.icon_file);
        }
        //name
        holder.fileName.setText(file.getName());
        //file size
        if(file.length()>0){
            if(file.length()/1000000 > 0){
                holder.fileSize.setText(file.length()/1000000 + " MB");
            }
            else if(file.length()/1000 > 0){
                holder.fileSize.setText(file.length()/1000 + " KB");
            }
            else{
                holder.fileSize.setText(file.length() + " B");
            }
        }else{
            holder.fileSize.setText("");
        }
        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);

        setInitialAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount(){
        return fileList.size();
    }

    @Override
    public void onViewDetachedFromWindow(FileViewHolder holder) {
        holder.itemView.clearAnimation();
        super.onViewDetachedFromWindow(holder);
    }

    public File getFile(int position){
        return fileList.get(position);
    }

    private void setInitialAnimation(View viewToAnimate, int position){
        int rows = ((MainActivity)App.instance.getCurrentActivity()).getRows();
        if(position < rows && ((MainActivity)App.instance.getCurrentActivity()).getInitAnim()){
            AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(450);
            viewToAnimate.startAnimation(animation);
        }
        if(position == rows){
            ((MainActivity)App.instance.getCurrentActivity()).setInitAnim(false);
        }
    }
}
