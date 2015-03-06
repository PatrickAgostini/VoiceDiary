package com.fhtrier.voiceDiary;

import java.util.Stack;

import android.view.View;

public class ViewPool
{
    private final QuestionActivity questionActivity;
    private Stack<View> views = new Stack<View>();

    public ViewPool(QuestionActivity questionActivity)
    {
        this.questionActivity = questionActivity;
    }

    public View popView()
    {
        if (views.isEmpty())
        {
            return questionActivity.getLayoutInflater().inflate(R.layout.view_question, null);
        }
        return views.pop();
    }

    public void pushView(View view)
    {
        views.push(view);
    }
}
