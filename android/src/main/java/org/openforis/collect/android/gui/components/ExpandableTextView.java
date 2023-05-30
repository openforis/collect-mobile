package org.openforis.collect.android.gui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import org.openforis.collect.R;

public class ExpandableTextView extends AppCompatTextView {

    private static final int MAX_LINE = 2;
    private int maxLine = 2;
    private boolean isViewMore = true;

    public ExpandableTextView(Context context) {
        super(context);
        initViews();
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        this.setTag(this.getText());
    }

    private void setTextInternal(String text) {
        super.setText(text, BufferType.SPANNABLE);
    }
    private void setTextInternal(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    public void initViews() {
        final ExpandableTextView textView = this;

        if (textView.getTag() == null) {
            textView.setTag(textView.getText());
        }
        // this.setTypeface(Typeface.createFromAsset(this.getContext().getAssets(), "GothamBook.ttf"));
        ViewTreeObserver vto = textView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int lineEndIndex;
                ViewTreeObserver obs = textView.getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);
                int lineCount = textView.getLayout().getLineCount();

                String textCurrent = textView.getText().toString();
                String expandText = textView.getContext().getString(isViewMore ? R.string.show_more : R.string.show_less);
                String textNext;
                if (lineCount <= maxLine) {
                    // text remains unchanged
                    textNext = textCurrent;
                } else if (isViewMore && maxLine > 0 && lineCount >= maxLine) {
                    // truncate text
                    lineEndIndex = textView.getLayout().getLineEnd(maxLine - 1);
                    String suffix = "... " + expandText;
                    textNext = textCurrent.subSequence(0, lineEndIndex - suffix.length()) + suffix;
                } else {
                    // expand text
                    lineEndIndex = textView.getLayout().getLineEnd(lineCount - 1);
                    textNext = textCurrent.subSequence(0, lineEndIndex) + " " + expandText;
                }
                textView.setTextInternal(textNext);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                if (lineCount > maxLine)
                    textView.setTextInternal(addClickablePartTextViewResizable(expandText),
                            BufferType.SPANNABLE);
                textView.setSelected(true);
            }
        });
    }

    private SpannableStringBuilder addClickablePartTextViewResizable(final String expandText) {
        final ExpandableTextView textView = this;
        final Context context = this.getContext();
        String string = textView.getText().toString();
        SpannableStringBuilder expandedStringBuilder = new SpannableStringBuilder(string);

        if (string.endsWith(expandText)) {
            expandedStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    textView.setLayoutParams(textView.getLayoutParams());
                    textView.setText(textView.getTag().toString(), BufferType.SPANNABLE);
                    textView.invalidate();
                    maxLine = isViewMore ? -1 : MAX_LINE;
                    isViewMore = !isViewMore;
                    initViews();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(true);
                    ds.setColor(context.getResources().getColor(R.color.colorPrimary));
//                    ds.setTypeface(Typeface.createFromAsset(context.getAssets(), "GothamMedium.ttf"));
                }
            }, string.indexOf(expandText), string.length(), 0);
        }
        return expandedStringBuilder;
    }
}