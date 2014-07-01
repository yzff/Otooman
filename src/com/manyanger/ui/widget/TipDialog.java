package com.manyanger.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.manyanger.common.AppUtil;
import com.manyounger.otooman.R;


public class TipDialog extends Dialog implements
    DialogInterface.OnCancelListener
{

    private static TipDialog dialog;

    public static void dissmissTip()
    {
        if (null != dialog)
        {
            dialog.dismiss();
        }
    }

    /**
     * Method: isCanceled
     * <p>Author: syn.lee
     * <p>Descreption: get isCanceled
     * <p>@return the isCanceled
     * <p>Modified: 2012-3-14
     */
    public static boolean isCanceled()
    {
        if (null != dialog)
        {
            return dialog.isCanceled;
        }
        return false;
    }

    /**
     * Method: show
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-3-14
     * @param context
     * @param message
     * @param cancer TODO
     * @return 
     */
    public static TipDialog show(final Context context, final String message,
        final OnCancelListener cancer)
    {
        if (AppUtil.isCurrentThreadUiThread())
        {
            init(context, message, cancer);
        }
        else
        {
            AppUtil.getCurrentActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    init(context, message, cancer);
                }
            });
            lock();
        }
        return dialog;
    }

    /**
     * Method: wait
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-8-3 
     */
    private static void lock()
    {
        synchronized (TipDialog.class)
        {
            try
            {
                TipDialog.class.wait();
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    /**
     * Method: init
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-8-3
     * @param context
     * @param message
     * @param cancer 
     */
    private static void init(Context context, String message,
        OnCancelListener cancer)
    {
        dialog = new TipDialog(context, message);
        dialog.setOnCancelListener(cancer);
        dialog.show();
        synchronized (TipDialog.class)
        {
            TipDialog.class.notify();
        }
    }

    /**
     * Method: showWait
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-3-14
     * @param context
     * @param cancer TODO
     */
    public static TipDialog showWait(Context context, OnCancelListener cancer)
    {
        return show(context, context.getResources().getString(R.string.wait),
            cancer);
    }

    private boolean isCanceled;

    private CharSequence text;

    public TipDialog(Context context)
    {
        super(context, R.style.Dialog_Tip);
    }

    private TipDialog(Context context, CharSequence text)
    {
        super(context, R.style.Dialog_Tip);
        this.text = text;
    }

    /**
     * Method: onCancel
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-3-14
     * @param dialog 
     */
    @Override
    public void onCancel(DialogInterface dialog)
    {
        isCanceled = true;
    }

    /**
     * Method: show
     * <p>Author: syn.lee
     * <p>Description: 
     * <p>Modified: 2012-3-14 
     */
    @Override
    public void show()
    {
        isCanceled = false;
        super.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_dialog);
        TextView tip = (TextView) findViewById(R.id.tip_text);
        tip.setFocusable(false);
        tip.setText(text);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.9f;
        getWindow().setAttributes(lp);

    }
}
