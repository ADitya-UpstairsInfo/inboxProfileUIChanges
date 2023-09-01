package com.yearlater.inboxmessenger.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.yearlater.inboxmessenger.R;
import com.yearlater.inboxmessenger.activities.main.messaging.ChatActivity;
import com.yearlater.inboxmessenger.model.constants.DownloadUploadStat;
import com.yearlater.inboxmessenger.model.constants.MessageStat;
import com.yearlater.inboxmessenger.model.constants.MessageType;
import com.yearlater.inboxmessenger.model.realms.Message;

import java.util.Date;
import java.util.List;

public class AdapterHelper {


    //check if it's only one item and there it's a media item and it's downloaded also
    public static boolean shouldEnableShareButton(List<Message> selectedItems) {
        if (selectedItems.size() == 1 && isHasMediaItem(selectedItems) && isMediaDownloaded(selectedItems))
            return true;

        return false;

    }

    public static Boolean shouldEnableReplyItem(List<Message> selectedItemsForActionMode,Boolean isGroup,Boolean isGroupActive){
        if (selectedItemsForActionMode.size() != 1) return false;

        Message message = selectedItemsForActionMode.get(0);
        if (MessageType.isDeletedMessage(message.getType())) return false;
        if (message.isMessageFromMe() && message.getMessageStat() == MessageStat.PENDING) //if it's sent message then check if the message was sent before replying.
            return false;
        if (isGroup){
            if (!isGroupActive)  return false;
        }

        return true;

    }

    //check if the list has a media item
    public static boolean isHasMediaItem(List<Message> selectedItems) {
        boolean returnVal = false;
        for (Message message : selectedItems) {

            if (MessageType.isMediaItem(message.getType()))
                returnVal = true;
            else
                return false;

        }
        return returnVal;
    }

    //check if media is downloaded
    public static boolean isMediaDownloaded(List<Message> selectedItems) {
        boolean returnVal = false;

        for (Message message : selectedItems) {

            if (message.getDownloadUploadStat() == DownloadUploadStat.SUCCESS
                    /* second param is for non media messages and default is 0 */
                    || message.getDownloadUploadStat() == DownloadUploadStat.DEFAULT)
                returnVal = true;

            else return false;


        }

        return returnVal;


    }

    public static boolean hasDeletedMessage(List<Message> messages) {
        for (Message message : messages) {
            if (MessageType.isDeletedMessage(message.getType()))
                return true;
        }
        return false;
    }

    //check if media is downloaded to enable forward button
    public static boolean shouldEnableForwardButton(List<Message> selectedItems) {
        if (isMediaDownloaded(selectedItems) && !hasDeletedMessage(selectedItems))
            return true;
        return false;
    }

    //check if all messages are ONLY text
    public static boolean shouldEnableCopyItem(List<Message> selectedItems) {
        boolean returnVal = false;
        for (Message message : selectedItems) {
            if (message.isExists() && message.isTextMessage())
                returnVal = true;
            else return false;
        }
        return returnVal;
    }


    public static int getVoiceMessageIcon(boolean isVoiceMessageSeen) {
        if (isVoiceMessageSeen)
            return R.drawable.ic_mic_read_with_stroke;
        else
            return R.drawable.ic_mic_sent_with_stroke;
    }

    //check if this message is selected or not
    public static boolean isSelectedForActionMode(Message message, List<Message> selcetedItems) {
        return (!selcetedItems.isEmpty() && message.isExists() && selcetedItems.contains(message));
    }

    public static int getPlayIcon(boolean isPlaying) {
        if (isPlaying)
            return R.drawable.ic_pause;

        return R.drawable.ic_play_arrow;
    }

    public static int getMessageStatDrawable(int messageStat) {
        switch (messageStat) {
            case MessageStat.PENDING:
                return R.drawable.ic_watch_later_green;

            case MessageStat.SENT:
                return R.drawable.ic_check;

            case MessageStat.RECEIVED:
                return R.drawable.ic_done_all;

            case MessageStat.READ:
                return R.drawable.ic_check_read;

            default:
                return R.drawable.ic_check;

        }
    }


    //change the checkmark color to grey
    public static Drawable getColoredStatDrawable(Context context, int messageStat) {
        Resources resources = context.getResources();
        Drawable drawable = resources.getDrawable(getMessageStatDrawable(messageStat));
        drawable.mutate();

        //if it's not read we will keep the blue color
        if (messageStat != MessageStat.READ) {
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            DrawableCompat.setTint(drawable, context.getResources().getColor(R.color.colorTextDescfortick));
        }
        return drawable;
    }

    public static boolean canDeleteForEveryOne(List<Message> selectedItems) {
        for (Message selectedItem : selectedItems) {

            long messageTime = Long.parseLong(selectedItem.getTimestamp());
            long now = new Date().getTime();
            if (selectedItem.getMessageStat() == MessageStat.PENDING
                    || MessageType.isDeletedMessage(selectedItem.getType())//check if it's already deleted
                    || !MessageType.isSentType(selectedItem.getType())//check if it's sent by the user
                    || TimeHelper.isMessageTimePassed(now, messageTime)) //check if time is valid
                return false;
        }
        return true;
    }


    //check if the list has a media item
    public static boolean shouldHideAllItems(List<Message> selectedItems) {
        boolean returnVal = false;
        for (Message message : selectedItems) {

            if (!MessageType.isMessageSupported(message.getType()))
                return true;
            else
                returnVal = false;

        }
        return returnVal;
    }
    public static void drawProfileImage(Bitmap resource, ImageView imageView, Context context)
    {
        Bitmap original = resource;
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_inboxprofileshape);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        float size = context.getResources().getDimension(R.dimen.profile_pic_size) ;//* ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        mCanvas.drawBitmap(Bitmap.createScaledBitmap(original, (int)size, (int)size, false), 2, 2, null);
        mCanvas.drawBitmap(bitmap, 0, 0, paint);
        paint.setXfermode(null);
        imageView.setImageBitmap(result);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
    public static void drawProfileImage(int resource, ImageView imageView, Context context)
    {
        Drawable vectorDrawableResource = ContextCompat.getDrawable(context,resource);
        Bitmap original = Bitmap.createBitmap(vectorDrawableResource.getIntrinsicWidth(),
                vectorDrawableResource.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasResource = new Canvas(original);
        vectorDrawableResource.setBounds(0, 0, canvasResource.getWidth(), canvasResource.getHeight());
        vectorDrawableResource.draw(canvasResource);
        drawProfileImage(original, imageView, context);
    }
}
