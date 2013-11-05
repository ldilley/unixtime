/*
 * $Id$
 * The Unix Time Converter
 * Copyright (C) 2010 Lloyd S. Dilley <lloyd@dilley.me>
 * http://www.dilley.me/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

/**
 * @author Lloyd S. Dilley
 */

package me.dilley.unixtimeconverter;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

public final class UnixTimeConverter extends Activity
{
  private final static Handler currentUnixTimeHandler=new Handler();
  private static Thread currentUnixTimestamp=null;
  private static boolean isRunning=false;

  private static long epoch=0L;
  private static long unixTimestamp=0L;

  private static Button clear=null;
  private static Button convert=null;

  private static EditText currentUnixTime;
  private static EditText time=null;
  private static EditText unixTime=null;

  private static DatePicker date=null;
  
  private static RadioButton unixToHuman=null;
  private static RadioButton humanToUnix=null;
  
  private static RadioButton localTime=null;

  final Runnable updateCurrentUnixTime=new Runnable()
  {
    public void run()
    {
      updateCurrentUnixTime();
    }
  };

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    clear=(Button)this.findViewById(R.id.clear);
    convert=(Button)this.findViewById(R.id.convert);

    currentUnixTime=(EditText)this.findViewById(R.id.currentUnixTime);
    time=(EditText)this.findViewById(R.id.time);
    unixTime=(EditText)this.findViewById(R.id.unixTime);

    date=(DatePicker)this.findViewById(R.id.date);

    unixToHuman=(RadioButton)this.findViewById(R.id.unixToHuman);
    humanToUnix=(RadioButton)this.findViewById(R.id.humanToUnix);

    localTime=(RadioButton)this.findViewById(R.id.localTime);

    calculateCurrentUnixTime();

    convert.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v)
      {
        convertValues();
      }
    });

    clear.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View v)
      {
        clearValues();
      }
    });
  }

  protected void calculateCurrentUnixTime()
  {
    currentUnixTimestamp=new Thread()
    {
      public void run()
      {
    	while(isRunning)
    	{
    	  try
    	  {
            epoch=System.currentTimeMillis()/1000;
            currentUnixTimeHandler.post(updateCurrentUnixTime);
            Thread.sleep(1000);
    	  }
    	  catch(InterruptedException ie)
    	  {
            ie.printStackTrace();
    	  }
    	}
      }
    };
    isRunning=true;
    currentUnixTimestamp.start();
  }

  private void updateCurrentUnixTime()
  {
    currentUnixTime.setText(Long.toString(epoch));
  }

  private final void convertValues()
  {
    if(humanToUnix.isChecked())
    {
      try
      {
        Calendar unixTS=Calendar.getInstance();
        unixTS.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        StringTokenizer timeToken=new StringTokenizer(time.getText().toString());
        String hours=timeToken.nextToken(":");
        String minutes=timeToken.nextToken(":");
        String seconds=timeToken.nextToken(":");
        if(Integer.parseInt(hours)<0 || Integer.parseInt(minutes)<0 || Integer.parseInt(seconds)<0)
        {
   	  final AlertDialog.Builder errorDialog=new AlertDialog.Builder(this);
   	  errorDialog.setTitle("Error");
   	  errorDialog.setIcon(android.R.drawable.ic_dialog_alert);
   	  errorDialog.setPositiveButton("OK", null);
   	  errorDialog.setCancelable(true);
   	  errorDialog.setMessage("Please input positive integers for hours, minutes, and seconds.");
   	  errorDialog.create().show();
          unixTime.setText("");
          return;
        }
        unixTS.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), Integer.parseInt(hours), Integer.parseInt(minutes), Integer.parseInt(seconds));
        unixTime.setText(Long.toString(unixTS.getTimeInMillis()/1000L));
      }
      catch(Exception e)
      {
  	final AlertDialog.Builder errorDialog=new AlertDialog.Builder(this);
  	errorDialog.setTitle("Error");
   	errorDialog.setIcon(android.R.drawable.ic_dialog_alert);
   	errorDialog.setPositiveButton("OK", null);
   	errorDialog.setCancelable(true);
   	errorDialog.setMessage("Please input time in the format HH:MM:SS where HH is hours, MM is minutes, and SS is seconds.");
   	errorDialog.create().show();
   	time.setText("00:00:00");
      }
    }
    else if(unixToHuman.isChecked())
    {
      try
      {
        unixTimestamp=Long.parseLong(unixTime.getText().toString());
        if(Long.toString(unixTimestamp).length()==0 || Long.toString(unixTimestamp)==null)
        {
   	  final AlertDialog.Builder errorDialog=new AlertDialog.Builder(this);
          errorDialog.setTitle("Error");
   	  errorDialog.setIcon(android.R.drawable.ic_dialog_alert);
   	  errorDialog.setPositiveButton("OK", null);
   	  errorDialog.setCancelable(true);
   	  errorDialog.setMessage("Please input a UNIX timestamp.");
   	  errorDialog.create().show();
          unixTime.setText("");
          return;
        }
        java.text.SimpleDateFormat dateOutput=new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        if(localTime.isChecked())
        {
          dateOutput.setTimeZone(java.util.TimeZone.getDefault());
        }
        else
        {
          dateOutput.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        String dateOutputString=dateOutput.format(new java.util.Date(unixTimestamp*1000));
        StringTokenizer dateToken=new StringTokenizer(dateOutputString);
        String dateString=dateToken.nextToken();
        String timeString=dateToken.nextToken();
        StringTokenizer dateStringToken=new StringTokenizer(dateString);
        String monthString=dateStringToken.nextToken("/");
        String dayString=dateStringToken.nextToken("/");
        String yearString=dateStringToken.nextToken("/");
        date.updateDate(Integer.parseInt(yearString), Integer.parseInt(monthString)-1, Integer.parseInt(dayString));
        time.setText(timeString); 
      }
      catch(Exception e)
      {
        final AlertDialog.Builder errorDialog=new AlertDialog.Builder(this);
   	errorDialog.setTitle("Error");
        errorDialog.setIcon(android.R.drawable.ic_dialog_alert);
  	errorDialog.setPositiveButton("OK", null);
   	errorDialog.setCancelable(true);
        errorDialog.setMessage("Please input a valid UNIX timestamp.");
   	errorDialog.create().show();
   	unixTime.setText("");
      }
    }
  }

  private static void clearValues()
  {
    unixTime.setText("");
    time.setText("00:00:00");
    Calendar cal=Calendar.getInstance();
    cal.setTimeZone(TimeZone.getDefault());
    date.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
      case R.id.about:
        showAbout();
        return true;
      case R.id.help:
    	showHelp();
    	return true;
      case R.id.quit:
        System.exit(0);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private final void showAbout()
  {
    final SpannableString aboutInfo=new SpannableString("    UNIX Time Converter\n    Version: 1.0\n    Author: Lloyd Dilley\n    lloyd@dilley.me@\n    http://www.dilley.me/");
    final AlertDialog.Builder aboutDialog=new AlertDialog.Builder(this);
    final TextView tv=new TextView(this);
    Linkify.addLinks(aboutInfo, Linkify.ALL);
    tv.setMovementMethod(LinkMovementMethod.getInstance());
    tv.setText(aboutInfo);
    tv.setTextColor(Color.WHITE);
    tv.setLinkTextColor(Color.RED);
    aboutDialog.setTitle("About");
    aboutDialog.setIcon(android.R.drawable.ic_dialog_info);
    aboutDialog.setPositiveButton("OK", null);
    aboutDialog.setCancelable(true);
    aboutDialog.setView(tv);
    aboutDialog.create().show();
  }

  private final void showHelp()
  {
    final AlertDialog.Builder helpDialog=new AlertDialog.Builder(this);
    helpDialog.setTitle("Help");
    helpDialog.setIcon(android.R.drawable.ic_dialog_info);
    helpDialog.setPositiveButton("OK", null);
    helpDialog.setCancelable(true);
    helpDialog.setMessage("Some reminders...\n\n" +
    		"* Time is in military (24 hour) format.\n\n" +
    		"* Local time is the timezone configured on your phone.\n\n" +
    		"* Negative UNIX timestamps can be entered for a date prior to 1970.\n\n" +
    		"* Setting the year prior to 1970 will yield a negative UNIX timestamp.\n\n" +
    		"* UNIX time is also called epoch time.\n\n" +
    		"* Epoch or UNIX time started at midnight on January 1, 1970 UTC.\n\n" +
    		"* Other than local time, you can use the UTC timezone.\n\n" +
    		"* UTC and GMT are often used interchangeably, but they are different.\n\n" +
    		"* Hours, minutes, and seconds prior to 10 can be entered without the leading zero.\n\n" +
    		"* Changing the timezone only affects the result of UNIX to human conversion.\n\n" +
    		"* The current UNIX time is always in UTC.\n\n" +
    		"* If your phone is not configured for UTC/GMT, the current local time you input will result in a different UNIX timestamp than the current UNIX time.\n\n" +
    		"* Brush your teeth before you go to bed.\n\n");
    helpDialog.create().show();
  }
}
