package com.eventshare.eventshare.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.eventshare.eventshare.ChatGroups;
import com.eventshare.eventshare.DbWrapper;
import com.eventshare.eventshare.R;
import com.eventshare.eventshare.Utils;
import com.parse.ParseQuery;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchEventActivity extends BaseActivity {

    private static final String TAG = "ES_DEBUG";
    private static final int MASK_NO_FIELDS_SET = 0x0;
    private static final int MASK_DATE_START_FIELD_SET = 1<<0;
    private static final int MASK_DATE_END_FIELD_SET = 1<<1;
    private static final int MASK_KEYWORDS_FIELD_SET = 1<<2;
    public static final int REQUEST_SHOW_RESULTS = 4;

    private int mFieldsSetMask;

    private Calendar mEventCalenderDateStart;
    private Calendar mEventCalenderDateEnd;

    private EditText etKeywords;
    private EditText etDateStart;
    private EditText etDateEnd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event);

        mFieldsSetMask = MASK_NO_FIELDS_SET;
        mEventCalenderDateStart = Calendar.getInstance();
        mEventCalenderDateEnd = Calendar.getInstance();
        mEventCalenderDateStart.clear();
        mEventCalenderDateEnd.clear();

        setupLayout();
        setupLayoutCallbacks();

    }



    private void setupLayout() {
        etKeywords = (EditText) findViewById(R.id.serach_keywords);
        etDateStart = (EditText) findViewById(R.id.search_event_date_start);
        etDateEnd = (EditText) findViewById(R.id.search_event_date_end);
    }

    private void setupLayoutCallbacks() {
        etKeywords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFieldsSetMask = mFieldsSetMask | MASK_KEYWORDS_FIELD_SET;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etDateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                                mEventCalenderDateStart.set(Calendar.DAY_OF_MONTH, day);
                                mEventCalenderDateStart.set(Calendar.MONTH, month);
                                mEventCalenderDateStart.set(Calendar.YEAR, year);

                                EditText date = (EditText) findViewById(R.id.search_event_date_start);

                                String myFormat = "dd/MM/yyyy";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                                date.setText(sdf.format(mEventCalenderDateStart.getTime()));

                                mFieldsSetMask = mFieldsSetMask | MASK_DATE_START_FIELD_SET;
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setMinDate(Calendar.getInstance());
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        etDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                                mEventCalenderDateEnd.set(Calendar.DAY_OF_MONTH, day);
                                mEventCalenderDateEnd.set(Calendar.MONTH, month);
                                mEventCalenderDateEnd.set(Calendar.YEAR, year);

                                EditText date = (EditText) findViewById(R.id.search_event_date_end);

                                String myFormat = "dd/MM/yyyy";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                                date.setText(sdf.format(mEventCalenderDateEnd.getTime()));

                                mFieldsSetMask = mFieldsSetMask | MASK_DATE_END_FIELD_SET;
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setMinDate(Calendar.getInstance());
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search: {
                if (mFieldsSetMask == MASK_NO_FIELDS_SET) {
                    return false;
                }
                List<ParseQuery<ChatGroups>> queries = new ArrayList<>();

                if (etKeywords.getText().length() != 0) {
                    List<String> listOfKeywords = Arrays.asList(etKeywords.getText().toString().split(" "));

                    ParseQuery<ChatGroups> byKeywordsQuery = DbWrapper.getSearchByKeywordsQuery(listOfKeywords);
                    queries.add(byKeywordsQuery);
                }

                // end date not set. set it to next year
                if ((mFieldsSetMask & MASK_DATE_END_FIELD_SET) == 0X0) {
                    mEventCalenderDateEnd.set(Calendar.DAY_OF_MONTH, 1);
                    mEventCalenderDateEnd.set(Calendar.MONTH, 0);

                    Calendar nowCalendar = Calendar.getInstance();
                    mEventCalenderDateEnd = nowCalendar;
                    mEventCalenderDateEnd.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR)+1);
                }

                mEventCalenderDateStart.set(Calendar.HOUR_OF_DAY, 0);
                mEventCalenderDateStart.set(Calendar.MINUTE, 0);

                mEventCalenderDateEnd.set(Calendar.HOUR_OF_DAY, 23);
                mEventCalenderDateEnd.set(Calendar.MINUTE, 59);

                //add this only if one of them set
                if ((mFieldsSetMask & MASK_DATE_START_FIELD_SET) != 0X0 ||
                        (mFieldsSetMask & MASK_DATE_END_FIELD_SET) != 0X0) {
                    ParseQuery<ChatGroups> byDatesRange =
                            DbWrapper.getSearchByTimeRangeQuery(
                                    mEventCalenderDateStart.getTime(),
                                    mEventCalenderDateEnd.getTime());

                    queries.add(byDatesRange);
                }


                Intent intent = new Intent(SearchEventActivity.this, ShowSearchEventResultsActivity.class);
                ShowSearchEventResultsActivity.queries = queries;
                startActivityForResult(intent, REQUEST_SHOW_RESULTS);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, requestCode, intent);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case REQUEST_SHOW_RESULTS:
                    setResult(RESULT_OK);
                    finish();
            }
        }
    }
}
