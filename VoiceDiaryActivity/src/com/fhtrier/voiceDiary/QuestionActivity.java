package com.fhtrier.voiceDiary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.holoeverywhere.app.AlertDialog;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.actionbarsherlock.app.SherlockActivity;
import com.fhtrier.voiceDiary.command.Command;
import com.fhtrier.voiceDiary.command.FemaleCommand;
import com.fhtrier.voiceDiary.command.MaleCommand;
import com.fhtrier.voiceDiary.command.MorningCommand;
import com.fhtrier.voiceDiary.command.SmokerCommand;

public class QuestionActivity extends SherlockActivity
{
	private static final long MAX_TIME = 1000 * 60 * 10;

	private ViewPool viewPool;
	private ViewFlipper viewFlipper;
	private List<Integer> questionIds = new LinkedList<Integer>();
	private SQLiteDatabase sqLiteDatabase;
	private HashMap<String, Command> commands = new HashMap<String, Command>();

	public final int GOOD_NOTIFICATION=0;
	public final int BAD_NOTIFICATION=1;
	public final int NEUTRAL_NOTIFICATION=2;
	public final int NO_RECORD=3;
	String user;
	private long startTime;

	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private List<Integer> answerId = new LinkedList<Integer>();

	public QuestionActivity()
	{
		commands.put("sm", new SmokerCommand());
		commands.put("mo", new MorningCommand());
		commands.put("fm", new FemaleCommand());
		commands.put("ma", new MaleCommand());
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question);

		this.getSupportActionBar().setIcon(R.drawable.actionbar_logo);
		this.getSupportActionBar().setTitle("");

		viewPool = new ViewPool(this);

		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		viewFlipper.setInAnimation(this.inFromRightAnimation());
		viewFlipper.setOutAnimation(this.outToLeftAnimation());

		sqLiteDatabase = MyApplication.getSqLiteDatabase();

		Cursor c = sqLiteDatabase.rawQuery(String.format("SELECT MAX(`id_questionnaire`) FROM `questionnaire` WHERE `language` = '%s';", this.getString(R.string.lang)), null);
		c.moveToFirst();
		int id_questionnaire = c.getInt(0);
		c.close();

		Cursor question = sqLiteDatabase.rawQuery(String.format("SELECT MIN(`id_question`) FROM `question` WHERE `fk_questionnaire` = '%d' AND `type` IS NULL;", id_questionnaire), null);
		MyApplication.printCursor(question);

		if (question.moveToFirst() && !question.isNull(0))
		{
			questionIds.add(question.getInt(0));
		}
		question.close();

		c = sqLiteDatabase.rawQuery(String.format("SELECT `type` FROM `question` WHERE `fk_questionnaire` = '%d' AND `type` IS NOT NULL GROUP BY `type`;", id_questionnaire), null);

		while (c.moveToNext())
		{
			Log.d(QuestionActivity.class.getName(), c.getString(0));

			Command command = commands.get(c.getString(0));
			if (command != null && command.askQuestionType(sqLiteDatabase))
			{
				question = sqLiteDatabase.rawQuery(String.format("SELECT MIN(`id_question`) FROM `question` WHERE `type` = '%s' AND `fk_questionnaire` = '%d' GROUP BY `type`;", c.getString(0), id_questionnaire), null);
				question.moveToFirst();
				questionIds.add(question.getInt(0));
				question.close();
			}
		}

		c.close();

		startTime = System.currentTimeMillis();
		this.createStartDialog().show();

		this.schowView(-1);

	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (System.currentTimeMillis() - startTime > MAX_TIME)
		{
			Toast tost = Toast.makeText(getApplicationContext(), this.getString(R.string.questionnaire_timeout), Toast.LENGTH_SHORT);
			tost.show();

			this.onBackPressed();
		}
	}

	private void schowView(int questionId)
	{
		if (questionId == -1 && questionIds.size() > 0)
		{
			questionId = questionIds.remove(0);
		}

		if (questionId == -1)
		{
			View view = this.getLayoutInflater().inflate(R.layout.view_question_end, viewFlipper);
			((Button)view.findViewById(R.id.questionnair_finish)).setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					QuestionActivity.this.onBackPressed();

				}
			});
			try
			{


				Cursor c = sqLiteDatabase.rawQuery("SELECT `id_user` FROM `user` WHERE `offline_login`!=0;", null);

				c.moveToFirst();
				this.user = c.getString(0);
				c.close();

				c = sqLiteDatabase.rawQuery(String.format("SELECT `next_protocolentry_id` FROM `user_protocolenty` WHERE `id_user` = '%s';", this.user), null);
				c.moveToFirst();
				int id = c.getInt(0);
				c.close();
				sqLiteDatabase.execSQL(String.format("UPDATE `user_protocolenty` SET `next_protocolentry_id` = '%d' WHERE `id_user` = '%s';", (id + 1), this.user));

				sqLiteDatabase.execSQL(String.format("INSERT INTO `protocolentry` VALUES ('%s', '%d', '%s');", this.user, id, dateFormat.format(new Date())));
				for (int i : answerId)
				{
					sqLiteDatabase.execSQL(String.format("INSERT INTO `rel_protocolentry_answer` VALUES ('%s', '%d', '%d');", this.user, id, i));
				}


			}
			catch (SQLiteException e)
			{
				Log.e(QuestionActivity.class.getName(), Log.getStackTraceString(e));
			}
		}
		else
		{
			showQuestionView(questionId);
		}
	}

	private void showQuestionView(int questionId)
	{
		View view = viewPool.popView();
		TextView tv = (TextView) view.findViewById(R.id.textQuestion);
		Cursor question = sqLiteDatabase.rawQuery(String.format("SELECT `question_text` FROM `question` WHERE `id_question` = '%d';", questionId), null);
		question.moveToFirst();
		tv.setText(question.getString(0));
		question.close();

		final Cursor answers = sqLiteDatabase.rawQuery(String.format("SELECT `id_answer`, `answer_text`, `fk_next_question` FROM `answer` WHERE `fk_question` = '%d';", questionId), null);
		answers.moveToFirst();
		final RadioGroup rg = (RadioGroup) view.findViewById(R.id.answersRadioGroup);
		final Button button = (Button) view.findViewById(R.id.question_button);

		rg.removeAllViews();
		rg.check(-1);

		button.setVisibility(Button.INVISIBLE);
		button.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				answerId.add(rg.getCheckedRadioButtonId());
				Cursor nextQuestionId = sqLiteDatabase.rawQuery(String.format("SELECT `fk_next_question` FROM `answer` WHERE `id_answer` = '%d';", rg.getCheckedRadioButtonId()), null);
				nextQuestionId.moveToFirst();

				if (nextQuestionId.isNull(0))
				{
					QuestionActivity.this.schowView(-1);
				}
				else
				{
					QuestionActivity.this.schowView(nextQuestionId.getInt(0));
				}

				nextQuestionId.close();
				viewFlipper.showNext();
				QuestionActivity.this.viewPool.pushView(viewFlipper.getChildAt(0));
				viewFlipper.removeViewAt(0);
			}
		});

		rg.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				button.setVisibility(Button.VISIBLE);
			}
		});

		RadioButton rb;

		while (!answers.isAfterLast())
		{
			rb = new RadioButton(this);
			rb.setTextSize(20);
			rb.setText(answers.getString(1));
			rb.setId(answers.getInt(0));

			rg.addView(rb);
			answers.moveToNext();
		}

		answers.close();
		viewFlipper.addView(view);

	}
	private AlertDialog createStartDialog()
	{
		AlertDialog.Builder builder =  new AlertDialog.Builder(this);
		builder.setTitle(this.getString(R.string.questionnaire_dialog_title));
		builder.setMessage(this.getString(R.string.questionnaire_dialog_message));
		builder.setCancelable(false);
		builder.setPositiveButton(this.getString(R.string.set_frequency_dialog_ok), new android.content.DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{

			}
		});

		return builder.create();
	}

	private Animation inFromRightAnimation()
	{

		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	private Animation outToLeftAnimation()
	{
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(500);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	@Override
	public void onBackPressed() {
		//MyApplication.getSqLiteDatabase().execSQL(String.format("UPDATE `user` SET `offline_login` = '%d' WHERE `session_id` NOT NULL;",1));
		Dialog dialog = generateDialog();
		dialog.show();
	}

	public Dialog generateDialog(){
		Context context = this;

		Dialog dialog = new Dialog(context);    	
		dialog.setContentView(R.layout.notification);
		ImageView image = (ImageView) dialog.findViewById(R.id.image);

		TextView title = (TextView) dialog.findViewById(android.R.id.title);
		title.setSingleLine(false);

		dialog.setTitle(this.getString(R.string.dialog_good_job));
		image.setImageResource(R.drawable.thumbs_up);

		image.setOnClickListener(new View.OnClickListener(){
			public void onClick(View View3) {
				QuestionActivity.this.finish();
			} });
		return dialog;
	}
}
