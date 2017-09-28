/****************************************************************************
 * Project Name		:	VAJRA
 *
 * File Name		:	CustomerDetailActivity
 *
 * Purpose			:	Represents Customer Detail activity, takes care of all
 * 						UI back end operations in this activity, such as event
 * 						handling data read from or display in views.
 *
 * DateOfCreation	:	15-November-2012
 *
 * Author			:	Balasubramanya Bharadwaj B S
 *
 ****************************************************************************/
package com.wepindia.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.wep.common.app.Database.Customer;
import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.WepBaseActivity;
import com.wep.common.app.views.WepButton;
import com.wepindia.pos.GenericClasses.DecimalDigitsInputFilter;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.utils.ActionBarUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerDetailActivity extends WepBaseActivity {

    // Context object
    Context myContext;

    private final int CHECK_INTEGER_VALUE = 0;
    private final int CHECK_DOUBLE_VALUE = 1;
    private final int CHECK_STRING_VALUE = 2;

    // DatabaseHandler object
    DatabaseHandler dbCustomer = new DatabaseHandler(CustomerDetailActivity.this);
    // MessageDialog object
    MessageDialog MsgBox;
    List<String> labelsItemName = null;
    // View handlers
    EditText txtName, txtPhone, txtAddress, txtSearchPhone, txtCreditAmount ,txGSTIN;
    WepButton btnAdd, btnEdit,btnClearCustomer,btnCloseCustomer;
    TableLayout tblCustomer;
    AutoCompleteTextView txtSearchName;
    String upon_rowClick_Phn = "";
    // Variables
    String Id, Name, Phone, Address, LastTransaction, TotalTransaction, CreditAmount, strUserName = "", strCustGSTIN ="";
    private Toolbar toolbar;

    private float mHeadingTextSize;
    private float mDataMiniDeviceTextsize;
    private float mSamsungTab3VDeviceTextsize;
    private float mSamsungT561DeviceTextsize;

    private int mItemNameWidth;
    private int mHSNWidth;
    private int mQuantityWidth;
    private int mRateWidth;
    private int mAmountWidth;

    private final static int mSamsungTab3VScreenResolutionWidth = 600;
    private final static int mSamsungT561ScreenResolutionWidth = 800;
    private final static int mDataMiniScreenResolutionWidth = 752;

    private TextView mItemNameTextView;
    private TextView mHSNTextView;
    private TextView mQuantityTextView;
    private TextView mRateTextView;
    private TextView mAmountTextView;
    private TextView mDeleteTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // old   activity_customerdetail
        setContentView(R.layout.activity_customerdetail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        myContext = this;
        MsgBox = new MessageDialog(myContext);

        strUserName = getIntent().getStringExtra("USER_NAME");

        //tvTitleUserName.setText(strUserName.toUpperCase());
        Date d = new Date();
        CharSequence s = DateFormat.format("dd-MM-yyyy", d.getTime());
        //tvTitleDate.setText("Date : " + s);
        com.wep.common.app.ActionBarUtils.setupToolbar(CustomerDetailActivity.this,toolbar,getSupportActionBar(),"Customer",strUserName," Date:"+s.toString());

        try {
            dbCustomer.CreateDatabase();
            dbCustomer.OpenDatabase();

            txtAddress = (EditText) findViewById(R.id.etCustomerAddress);
            txtName = (EditText) findViewById(R.id.etCustomerName);
            txtPhone = (EditText) findViewById(R.id.etCustomerPhone);
            txtCreditAmount = (EditText) findViewById(R.id.etCreditAmount);
            txtCreditAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});
            txtSearchName = (AutoCompleteTextView) findViewById(R.id.etSearchCustomerName);
            txtSearchPhone = (EditText) findViewById(R.id.etSearchCustomerPhone);
            txGSTIN = (EditText) findViewById(R.id.etCustomerGSTIN);

            btnAdd = (WepButton) findViewById(R.id.btnAddCustomer);
            btnEdit = (WepButton) findViewById(R.id.btnEditCustomer);
            btnClearCustomer = (WepButton) findViewById(R.id.btnClearCustomer);
            btnCloseCustomer = (WepButton) findViewById(R.id.btnCloseCustomer);

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddCustomer(v);
                }
            });
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditCustomer(v);
                }
            });
            btnClearCustomer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClearCustomer(v);
                }
            });
            btnCloseCustomer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloseCustomer(v);
                }
            });
            tblCustomer = (TableLayout) findViewById(R.id.tblCustomer);

            ResetCustomer();
            loadAutoCompleteData();

            txtSearchPhone.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try {
                        if (txtSearchPhone.getText().toString().length() == 10) {
                            Cursor crsrCust = dbCustomer.getCustomer(txtSearchPhone.getText().toString());
                            if (crsrCust.moveToFirst()) {
                                txtName.setText(crsrCust.getString(crsrCust.getColumnIndex("CustName")));
                                txtPhone.setText(crsrCust.getString(crsrCust.getColumnIndex("CustContactNumber")));
                                txtAddress.setText(crsrCust.getString(crsrCust.getColumnIndex("CustAddress")));
                                txtCreditAmount.setText(crsrCust.getString(crsrCust.getColumnIndex("CreditAmount")));
                                String gstin = crsrCust.getString(crsrCust.getColumnIndex("GSTIN"));
                                if (gstin==null)
                                    gstin = "";
                                txGSTIN.setText(gstin);

                                ClearCustomerTable();
                                DisplayCustomerSearch(txtSearchPhone.getText().toString());
                                txtSearchName.setText("");
                                //}
                            } else {
                                MsgBox.Show("", "Customer is not Found, Please Add Customer before Order");
                            }
                        } else {

                        }
                    } catch (Exception ex) {
                        MsgBox.Show("Error", ex.getMessage());
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            txtSearchName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Cursor crsrCust = dbCustomer.getCustomerList(txtSearchName.getText().toString());
                        if (crsrCust.moveToFirst()) {
                            txtName.setText(crsrCust.getString(crsrCust.getColumnIndex("CustName")));
                            txtPhone.setText(crsrCust.getString(crsrCust.getColumnIndex("CustContactNumber")));
                            txtAddress.setText(crsrCust.getString(crsrCust.getColumnIndex("CustAddress")));
                            txtCreditAmount.setText(crsrCust.getString(crsrCust.getColumnIndex("CreditAmount")));
                            String gstin = crsrCust.getString(crsrCust.getColumnIndex("GSTIN"));
                            if (gstin==null)
                                gstin = "";
                            txGSTIN.setText(gstin);
                            //txGSTIN.setText(crsrCust.getString(crsrCust.getColumnIndex("GSTIN")));
                            ClearCustomerTable();
                            DisplayCustomerSearchbyName(txtSearchName.getText().toString());
                            txtSearchPhone.setText("");
                            //}
                        } else {
                            MsgBox.Show("", "Customer is not Found, Please Add Customer before Order");
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            getScreenResolutionWidthType(checkScreenResolutionWidthType(this));

            DisplayCustomer();
        } catch (Exception exp) {
            Toast.makeText(myContext, "OnCreate: " + exp.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private static int checkScreenResolutionWidthType(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return height;
    }

    private void getScreenResolutionWidthType(int screenResolutionType) {

        switch (screenResolutionType) {

            case mSamsungTab3VScreenResolutionWidth:
                mDataMiniDeviceTextsize = 8;
                mItemNameWidth = 105;
                mHSNWidth = 50;
                mQuantityWidth = 55;
                mRateWidth = 60;
                mAmountWidth = 60;
                break;

            case mSamsungT561ScreenResolutionWidth:
                mHeadingTextSize = 16;
                mDataMiniDeviceTextsize = 9;
                mItemNameWidth = 140;
                mHSNWidth = 70;
                mQuantityWidth = 65;
                mRateWidth = 65;
                mAmountWidth = 75;
              /*  mItemNameTextView.setTextSize(mHeadingTextSize);
                mHSNTextView.setTextSize(mHeadingTextSize);
                mQuantityTextView.setTextSize(mHeadingTextSize);
                mRateTextView.setTextSize(mHeadingTextSize);
                mAmountTextView.setTextSize(mHeadingTextSize);
                mDeleteTextView.setTextSize(mHeadingTextSize);*/
                break;

            case mDataMiniScreenResolutionWidth:
                mHeadingTextSize = 16;
                mDataMiniDeviceTextsize = 11;
                mItemNameWidth = 140;
                mHSNWidth = 70;
                mQuantityWidth = 65;
                mRateWidth = 65;
                mAmountWidth = 85;
              /*  mItemNameTextView.setTextSize(mHeadingTextSize);
                mHSNTextView.setTextSize(mHeadingTextSize);
                mQuantityTextView.setTextSize(mHeadingTextSize);
                mRateTextView.setTextSize(mHeadingTextSize);
                mAmountTextView.setTextSize(mHeadingTextSize);
                mDeleteTextView.setTextSize(mHeadingTextSize);*/
                break;
        }
    }

    private void DisplayCustomerSearch(String PhoneNo) {
        Cursor crsrCustomer;
        crsrCustomer = dbCustomer.getCustomer(PhoneNo);

        TableRow rowCustomer = null;
        TextView tvSno, tvId, tvName, tvLastTransaction, tvTotalTransaction, tvPhone, tvAddress, tvCreditAmount;
        ImageButton btnImgDelete;
        int i = 1;
        if (crsrCustomer != null && crsrCustomer.getCount() > 0) {
            if (crsrCustomer.moveToFirst()) {
                do {
                    rowCustomer = new TableRow(myContext);
                    rowCustomer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    rowCustomer.setBackgroundResource(R.drawable.row_background);

                    tvSno = new TextView(myContext);
                    tvSno.setTextSize(18);
                    tvSno.setText(String.valueOf(i));
                    tvSno.setGravity(Gravity.LEFT);
                    rowCustomer.addView(tvSno);

                    tvId = new TextView(myContext);
                    tvId.setTextSize(18);
                    tvId.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustId")));
                    rowCustomer.addView(tvId);

                    tvName = new TextView(myContext);
                    tvName.setTextSize(18);
                    tvName.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustName")));
                    rowCustomer.addView(tvName);

                    tvLastTransaction = new TextView(myContext);
                    tvLastTransaction.setTextSize(18);
                    tvLastTransaction.setGravity(Gravity.LEFT);
                    tvLastTransaction.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("LastTransaction")));
                    rowCustomer.addView(tvLastTransaction);

                    tvTotalTransaction = new TextView(myContext);
                    tvTotalTransaction.setTextSize(18);
                    tvTotalTransaction.setGravity(Gravity.LEFT);
                    tvTotalTransaction.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("TotalTransaction")));
                    rowCustomer.addView(tvTotalTransaction);

                    tvPhone = new TextView(myContext);
                    tvPhone.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustContactNumber")));
                    rowCustomer.addView(tvPhone);

                    tvAddress = new TextView(myContext);
                    tvAddress.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustAddress")));
                    rowCustomer.addView(tvAddress);

                    tvCreditAmount = new TextView(myContext);
                    //tvCreditAmount.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CreditAmount")));
                    double amt = crsrCustomer.getDouble(crsrCustomer.getColumnIndex("CreditAmount"));
                    tvCreditAmount.setText(String.format("%.2f",amt));
                    tvCreditAmount.setGravity(Gravity.END);
                    tvCreditAmount.setPadding(0,0,10,0);
                    rowCustomer.addView(tvCreditAmount);

                    // Delete
                    int res = getResources().getIdentifier("delete", "drawable", this.getPackageName());
                    btnImgDelete = new ImageButton(myContext);
                    btnImgDelete.setImageResource(res);
                    btnImgDelete.setLayoutParams(new TableRow.LayoutParams(60, 40));
                    btnImgDelete.setOnClickListener(mListener);
                    rowCustomer.addView(btnImgDelete);

                    TextView tvGSTIN = new TextView(myContext);
                    String gstin = crsrCustomer.getString(crsrCustomer.getColumnIndex("GSTIN"));
                    if (gstin==null)
                        gstin = "";
                    tvGSTIN.setText(gstin);
                    //tvGSTIN.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("GSTIN")));
                    tvGSTIN.setGravity(1);
                    rowCustomer.addView(tvGSTIN);

                    rowCustomer.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (String.valueOf(v.getTag()) == "TAG") {
                                TableRow Row = (TableRow) v;

                                TextView rowId = (TextView) Row.getChildAt(1);
                                TextView rowName = (TextView) Row.getChildAt(2);
                                TextView rowLastTransaction = (TextView) Row.getChildAt(3);
                                TextView rowTotalTransaction = (TextView) Row.getChildAt(4);
                                TextView rowPhone = (TextView) Row.getChildAt(5);
                                TextView rowAddress = (TextView) Row.getChildAt(6);
                                TextView rowCreditAmount = (TextView) Row.getChildAt(7);
                                TextView gstin = (TextView)Row.getChildAt(9);

                                Id = rowId.getText().toString();
                                LastTransaction = rowLastTransaction.getText().toString();
                                TotalTransaction = rowTotalTransaction.getText().toString();

                                txtName.setText(rowName.getText());
                                txtPhone.setText(rowPhone.getText());
                                txtAddress.setText(rowAddress.getText());
                                txtCreditAmount.setText(rowCreditAmount.getText());
                                txGSTIN.setText(gstin.getText().toString());

                                btnAdd.setEnabled(false);
                                btnEdit.setEnabled(true);
                            }
                        }
                    });

                    rowCustomer.setTag("TAG");

                    tblCustomer.addView(rowCustomer,
                            new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                    i++;
                } while (crsrCustomer.moveToNext());
            } else {
                Log.d("DisplayCustomer", "No Customer found");
            }
        }
    }

    private void DisplayCustomerSearchbyName(String CustomerName) {
        Cursor crsrCustomer;
        crsrCustomer = dbCustomer.getCustomerList(CustomerName);

        TableRow rowCustomer = null;
        TextView tvSno, tvId, tvName, tvLastTransaction, tvTotalTransaction, tvPhone, tvAddress, tvCreditAmount;
        ImageButton btnImgDelete;
        int i = 1;
        if (crsrCustomer != null && crsrCustomer.getCount() > 0) {
            if (crsrCustomer.moveToFirst()) {
                do {
                    rowCustomer = new TableRow(myContext);
                    rowCustomer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                    rowCustomer.setBackgroundResource(R.drawable.row_background);

                    tvSno = new TextView(myContext);
                    tvSno.setTextSize(18);
                    tvSno.setText(String.valueOf(i));
                    tvSno.setGravity(1);
                    rowCustomer.addView(tvSno);

                    tvId = new TextView(myContext);
                    tvId.setTextSize(18);
                    tvId.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustId")));
                    rowCustomer.addView(tvId);

                    tvName = new TextView(myContext);
                    tvName.setTextSize(18);
                    tvName.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustName")));
                    rowCustomer.addView(tvName);

                    tvLastTransaction = new TextView(myContext);
                    tvLastTransaction.setTextSize(18);
                    tvLastTransaction.setGravity(1);
                    tvLastTransaction.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("LastTransaction")));
                    rowCustomer.addView(tvLastTransaction);

                    tvTotalTransaction = new TextView(myContext);
                    tvTotalTransaction.setTextSize(18);
                    tvTotalTransaction.setGravity(1);
                    tvTotalTransaction.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("TotalTransaction")));
                    rowCustomer.addView(tvTotalTransaction);

                    tvPhone = new TextView(myContext);
                    tvPhone.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustContactNumber")));
                    rowCustomer.addView(tvPhone);

                    tvAddress = new TextView(myContext);
                    tvAddress.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustAddress")));
                    rowCustomer.addView(tvAddress);

                    tvCreditAmount = new TextView(myContext);
                    double amt = crsrCustomer.getDouble(crsrCustomer.getColumnIndex("CreditAmount"));
                    tvCreditAmount.setText(String.format("%.2f",amt));
                    tvCreditAmount.setGravity(Gravity.LEFT);
                    tvCreditAmount.setPadding(0,0,10,0);
                    rowCustomer.addView(tvCreditAmount);

                    // Delete
                    int res = getResources().getIdentifier("delete", "drawable", this.getPackageName());
                    btnImgDelete = new ImageButton(myContext);
                    btnImgDelete.setImageResource(res);
                    btnImgDelete.setLayoutParams(new TableRow.LayoutParams(60, 40));
                    btnImgDelete.setOnClickListener(mListener);
                    rowCustomer.addView(btnImgDelete);

                    TextView tvGSTIN = new TextView(myContext);
                    String gstin = crsrCustomer.getString(crsrCustomer.getColumnIndex("GSTIN"));
                    if (gstin==null)
                        gstin = "";
                    tvGSTIN.setText(gstin);
                    //tvGSTIN.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("GSTIN")));
                    tvGSTIN.setGravity(1);
                    rowCustomer.addView(tvGSTIN);

                    rowCustomer.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (String.valueOf(v.getTag()) == "TAG") {
                                TableRow Row = (TableRow) v;

                                TextView rowId = (TextView) Row.getChildAt(1);
                                TextView rowName = (TextView) Row.getChildAt(2);
                                TextView rowLastTransaction = (TextView) Row.getChildAt(3);
                                TextView rowTotalTransaction = (TextView) Row.getChildAt(4);
                                TextView rowPhone = (TextView) Row.getChildAt(5);
                                TextView rowAddress = (TextView) Row.getChildAt(6);
                                TextView rowCreditAmount = (TextView) Row.getChildAt(7);
                                TextView gstin = (TextView) Row.getChildAt(9);

                                Id = rowId.getText().toString();
                                LastTransaction = rowLastTransaction.getText().toString();
                                TotalTransaction = rowTotalTransaction.getText().toString();

                                txtName.setText(rowName.getText());
                                txtPhone.setText(rowPhone.getText());
                                txtAddress.setText(rowAddress.getText());
                                txtCreditAmount.setText(rowCreditAmount.getText());
                                txGSTIN.setText(gstin.getText().toString());

                                btnAdd.setEnabled(false);
                                btnEdit.setEnabled(true);
                            }
                        }
                    });


                    rowCustomer.setTag("TAG");

                    tblCustomer.addView(rowCustomer,
                            new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                    i++;
                } while (crsrCustomer.moveToNext());
            } else {
                Log.d("DisplayCustomer", "No Customer found");
            }
        }
    }

    /*public String doubleconverter(double value) //Got here 6.743240136E7 or something..
    {
        DecimalFormat formatter;

        if (value - (int) value > 0.0)
            formatter = new DecimalFormat("0.00"); //Here you can also deal with rounding if you wish..
        else
            formatter = new DecimalFormat("0");

        return formatter.format(value);
    }*/

    @SuppressWarnings("deprecation")
    private void DisplayCustomer() {
        Cursor crsrCustomer;
        crsrCustomer = dbCustomer.getAllCustomer();

        TableRow rowCustomer = null;
        TextView tvSno, tvId, tvName, tvLastTransaction, tvTotalTransaction, tvPhone, tvAddress, tvCreditAmount;
        ImageButton btnImgDelete;
        int i = 1;
        if (crsrCustomer != null && crsrCustomer.getCount() > 0) {
            if (crsrCustomer.moveToFirst()) {
                do {
                    rowCustomer = new TableRow(myContext);
                    rowCustomer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                    rowCustomer.setBackgroundResource(R.drawable.row_background);

                    tvSno = new TextView(myContext);
                    tvSno.setTextSize(14);
                    tvSno.setWidth(100);
                    tvSno.setText(String.valueOf(i));
                    tvSno.setGravity(1);
                    rowCustomer.addView(tvSno);

                    tvId = new TextView(myContext);
                    tvId.setTextSize(18);
                    tvId.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustId")));
                    rowCustomer.addView(tvId);

                    tvName = new TextView(myContext);
                    tvName.setTextSize(18);
                    tvName.setWidth(120);
                    tvName.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustName")));
                    rowCustomer.addView(tvName);

                    tvLastTransaction = new TextView(myContext);
                    tvLastTransaction.setTextSize(18);
                    tvLastTransaction.setGravity(Gravity.LEFT);
                    tvLastTransaction.setPadding(15,0,0,0);
                    tvLastTransaction.setText(String.format("%.2f",crsrCustomer.getDouble(crsrCustomer.getColumnIndex("LastTransaction"))));
                    rowCustomer.addView(tvLastTransaction);

                    tvTotalTransaction = new TextView(myContext);
                    tvTotalTransaction.setTextSize(18);
                    tvTotalTransaction.setGravity(Gravity.LEFT);
                    tvTotalTransaction.setPadding(38,0,0,0);
                    tvTotalTransaction.setText(String.format("%.2f",crsrCustomer.getDouble(crsrCustomer.getColumnIndex("TotalTransaction"))));
                    rowCustomer.addView(tvTotalTransaction);

                    tvPhone = new TextView(myContext);
                    tvPhone.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustContactNumber")));
                    rowCustomer.addView(tvPhone);

                    tvAddress = new TextView(myContext);
                    tvAddress.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustAddress")));
                    rowCustomer.addView(tvAddress);

                    tvCreditAmount = new TextView(myContext);
                    double amt = crsrCustomer.getDouble(crsrCustomer.getColumnIndex("CreditAmount"));
                    tvCreditAmount.setText(String.format("%.2f",amt));
                    tvCreditAmount.setTextSize(18);
                    tvCreditAmount.setGravity(Gravity.LEFT);
                    tvCreditAmount.setPadding(15,0,0,0);
                    rowCustomer.addView(tvCreditAmount);

                    // Delete
                    int res = getResources().getIdentifier("delete", "drawable", this.getPackageName());
                    btnImgDelete = new ImageButton(myContext);
                    btnImgDelete.setImageResource(res);
                    btnImgDelete.setLayoutParams(new TableRow.LayoutParams(60, 40));
                    btnImgDelete.setOnClickListener(mListener);
                    rowCustomer.addView(btnImgDelete);

                    TextView tvGSTIN = new TextView(myContext);
                    tvGSTIN.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("GSTIN")));
                    tvGSTIN.setGravity(1);
                    rowCustomer.addView(tvGSTIN);

                    rowCustomer.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            if (String.valueOf(v.getTag()) == "TAG") {
                                TableRow Row = (TableRow) v;

                                TextView rowId = (TextView) Row.getChildAt(1);
                                TextView rowName = (TextView) Row.getChildAt(2);
                                TextView rowLastTransaction = (TextView) Row.getChildAt(3);
                                TextView rowTotalTransaction = (TextView) Row.getChildAt(4);
                                TextView rowPhone = (TextView) Row.getChildAt(5);
                                TextView rowAddress = (TextView) Row.getChildAt(6);
                                TextView rowCreditAmount = (TextView) Row.getChildAt(7);
                                TextView gstin = (TextView) Row.getChildAt(9);

                                Id = rowId.getText().toString();
                                LastTransaction = rowLastTransaction.getText().toString();
                                TotalTransaction = rowTotalTransaction.getText().toString();

                                txtName.setText(rowName.getText());
                                txtPhone.setText(rowPhone.getText());
                                upon_rowClick_Phn = rowPhone.getText().toString();
                                txtAddress.setText(rowAddress.getText());
                                txtCreditAmount.setText(rowCreditAmount.getText());
                                txGSTIN.setText(gstin.getText().toString());

                                btnAdd.setEnabled(false);
                                btnEdit.setEnabled(true);
                            }
                        }
                    });

                    rowCustomer.setTag("TAG");

                    tblCustomer.addView(rowCustomer,
                            new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                    i++;
                } while (crsrCustomer.moveToNext());
            } else {
                Log.d("DisplayCustomer", "No Customer found");
            }
        }
    }

    private View.OnClickListener mListener = new View.OnClickListener() {

        public void onClick(final View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(myContext)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to Delete this Customer.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            TableRow tr = (TableRow) v.getParent();
                            TextView CustId = (TextView) tr.getChildAt(1);
                            TextView CustName = (TextView) tr.getChildAt(2);

                            long lResult = dbCustomer.DeleteCustomer(Integer.valueOf(CustId.getText().toString()));
                            MsgBox.Show("", "Customer Deleted Successfully");
                            ((ArrayAdapter<String>)(txtSearchName.getAdapter())).remove(CustName.getText().toString());
                            ClearCustomerTable();
                            DisplayCustomer();

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    private boolean IsCustomerExists(String PhoneNumber) {
        boolean isCustomerExists = false;
        String strPhone = "", strName = "";
        TextView Name, Phone;

        for (int i = 1; i < tblCustomer.getChildCount(); i++) {

            TableRow Row = (TableRow) tblCustomer.getChildAt(i);

            if (Row.getChildAt(0) != null) {
                Phone = (TextView) Row.getChildAt(5);
                Name = (TextView) Row.getChildAt(2);
                strName = Name.getText().toString();
                strPhone = Phone.getText().toString();

                Log.v("CustomerActivity",
                        "Phone:" + strPhone.toUpperCase() + " New Phone:" + PhoneNumber.toUpperCase());

                if (strPhone.toUpperCase().equalsIgnoreCase(PhoneNumber.toUpperCase())) {
                    isCustomerExists = true;
                    break;
                }
            }
        }
        return isCustomerExists;
    }

    private void InsertCustomer(String strAddress, String strContactNumber, String strName, double fLastTransaction,
                                double fTotalTransaction, double fCreditAmount, String gstin) {
        long lRowId;

        Customer objCustomer = new Customer(strAddress, strName, strContactNumber, fLastTransaction, fTotalTransaction,
                fCreditAmount, gstin);

        lRowId = dbCustomer.addCustomer(objCustomer);

        Log.d("Customer", "Row Id: " + String.valueOf(lRowId));

    }

    private void ClearCustomerTable() {
        for (int i = 1; i < tblCustomer.getChildCount(); i++) {
            View Row = tblCustomer.getChildAt(i);
            if (Row instanceof TableRow) {
                ((TableRow) Row).removeAllViews();
            }
        }
    }

    private void ResetCustomer() {
        txtName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtCreditAmount.setText("0");
        txtSearchPhone.setText("");
        txtSearchName.setText("");
        txGSTIN.setText("");
        upon_rowClick_Phn="";
        btnAdd.setEnabled(true);
        btnEdit.setEnabled(false);

    }

    public void AddCustomer(View v) {
        Name = txtName.getText().toString();
        Phone = txtPhone.getText().toString();
        Address = txtAddress.getText().toString();
        CreditAmount = txtCreditAmount.getText().toString();
        String GSTIN  = txGSTIN.getText().toString().trim().toUpperCase();

        if (Name.equalsIgnoreCase("")) {
            MsgBox.Show("Warning", "Please enter customer name before adding customer");
        } else if (Phone.equalsIgnoreCase("")) {
            MsgBox.Show("Warning", "Please enter mobile no before adding customer");
        } else if(Phone.length() != 10) {
            MsgBox.Show("Warning", "Please enter correct mobile no before adding customer");
        } else {
            if (IsCustomerExists(Phone)) {
                MsgBox.Show("Warning", "Customer already exists");
            } else {

                if (GSTIN == null) {
                    GSTIN = "";
                }
                boolean mFlag = false;
                try {
                    if(GSTIN.trim().length() == 0)
                    {mFlag = true;}
                    else if (GSTIN.trim().length() > 0 && GSTIN.length() == 15) {
                        String[] part = GSTIN.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        if (CHECK_INTEGER_VALUE == checkDataypeValue(part[0], "Int")
                                && CHECK_STRING_VALUE == checkDataypeValue(part[1],"String")
                                && CHECK_INTEGER_VALUE == checkDataypeValue(part[2],"Int")
                                && CHECK_STRING_VALUE == checkDataypeValue(part[3],"String")
                                && CHECK_INTEGER_VALUE == checkDataypeValue(part[4],"Int")
                                && CHECK_STRING_VALUE == checkDataypeValue(part[5],"String")
                                && CHECK_INTEGER_VALUE == checkDataypeValue(part[6],"Int")) {

                               /* int length = gstin.length() -1;
                                if(Integer.parseInt(String.valueOf(gstin.charAt(length))) ==  checksumGSTIN(gstin.substring(0,length)))*/
                            mFlag = true;
                        } else {
                            mFlag = false;
                        }
                    } else {
                        mFlag = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mFlag = false;
                }
                if(mFlag)
                {

                    double dCreditAmount = txtCreditAmount.getText().toString().trim().equals("")?0.00:
                            Double.parseDouble(String.format("%.2f", Double.parseDouble(txtCreditAmount.getText().toString().trim())));

                    InsertCustomer(Address, Phone, Name, 0, 0, dCreditAmount,GSTIN);
                    Toast.makeText(myContext, "Customer Added Successfully", Toast.LENGTH_LONG).show();
                    ResetCustomer();
                    ClearCustomerTable();
                    DisplayCustomer();
                    ((ArrayAdapter<String>)(txtSearchName.getAdapter())).add(Name);
                }else
                {
                    MsgBox.Show("Invalid Information","Please enter valid GSTIN for customer");
                }
            }
        }
    }

    public static int checkDataypeValue(String value, String type) {
        int flag =0;
        try {
            switch(type) {
                case "Int":
                    Integer.parseInt(value);
                    flag = 0;
                    break;
                case "Double" : Double.parseDouble(value);
                    flag = 1;
                    break;
                default : flag =2;
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            flag = -1;
        }
        return flag;
    }


    public void EditCustomer(View v) {
        Name = txtName.getText().toString();
        Phone = txtPhone.getText().toString();
        Address = txtAddress.getText().toString();
        CreditAmount = txtCreditAmount.getText().toString();
        String GSTIN = txGSTIN.getText().toString().trim().toUpperCase();
        if(!Phone.equalsIgnoreCase(upon_rowClick_Phn))
        {
            Cursor cursor = dbCustomer.getCustomer(Phone);
            if(cursor!=null && cursor.moveToFirst())
            {
                String name = cursor.getString(cursor.getColumnIndex("CustName"));
                MsgBox.Show("Error", name+" already registered with Phn : "+Phone );
                return;
            }
        }
        if (GSTIN == null) {
            GSTIN = "";
        }
        boolean mFlag = false;
        try {
            if(GSTIN.trim().length() == 0)
            {mFlag = true;}
            else if (GSTIN.trim().length() > 0 && GSTIN.length() == 15) {
                String[] part = GSTIN.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                if (CHECK_INTEGER_VALUE == checkDataypeValue(part[0], "Int")
                        && CHECK_STRING_VALUE == checkDataypeValue(part[1],"String")
                        && CHECK_INTEGER_VALUE == checkDataypeValue(part[2],"Int")
                        && CHECK_STRING_VALUE == checkDataypeValue(part[3],"String")
                        && CHECK_INTEGER_VALUE == checkDataypeValue(part[4],"Int")
                        && CHECK_STRING_VALUE == checkDataypeValue(part[5],"String")
                        && CHECK_INTEGER_VALUE == checkDataypeValue(part[6],"Int")) {
                    mFlag = true;
                } else {
                    mFlag = false;
                }
            } else {
                mFlag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mFlag = false;
        }
        if (mFlag)
        {
            double dCreditAmount = txtCreditAmount.getText().toString().trim().equals("")?0.00:
                    Double.parseDouble(String.format("%.2f", Double.parseDouble(txtCreditAmount.getText().toString().trim())));

            Log.d("Customer Selection", "Id: " + Id + " Name: " + Name + " Phone:" + Phone + " Address:" + Address
                    + " Last Transn.:" + LastTransaction + " Total Transan.:" + TotalTransaction+" GSTIN : "+GSTIN);
            int iResult = dbCustomer.updateCustomer(Address, Phone, Name, Integer.parseInt(Id),
                    Double.parseDouble(LastTransaction), Double.parseDouble(TotalTransaction), dCreditAmount, GSTIN);
            Log.d("updateCustomer", "Updated Rows: " + String.valueOf(iResult));
            Toast.makeText(myContext, "Customer Updated Successfully", Toast.LENGTH_LONG).show();
            ResetCustomer();
            if (iResult > 0) {
                ClearCustomerTable();
                DisplayCustomer();
            } else {
                MsgBox.Show("Warning", "Update failed");
            }
        }else
        {
            MsgBox.Show("Invalid Information","Please enter valid GSTIN for customer");
        }


    }

    public void ClearCustomer(View v) {
        ResetCustomer();
        ClearCustomerTable();
        DisplayCustomer();
    }

    public void CloseCustomer(View v) {

        dbCustomer.CloseDatabase();
        this.finish();
    }

    private void loadAutoCompleteData() {
        // List - Get Item Name
        labelsItemName = dbCustomer.getAllCustomerName();
        // Creating adapter for spinner
        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,labelsItemName);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(myContext,android.R.layout.simple_expandable_list_item_1);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        // attaching data adapter to spinner

        txtSearchName.setAdapter(dataAdapter);
        dataAdapter.setNotifyOnChange(true);
        dataAdapter.addAll(labelsItemName);
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            AlertDialog.Builder AuthorizationDialog = new AlertDialog.Builder(myContext);
            AuthorizationDialog
                    .setTitle("Are you sure you want to exit ?")
                    .setIcon(R.drawable.ic_launcher)
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            /*Intent returnIntent =new Intent();
                            setResult(Activity.RESULT_OK,returnIntent);*/
                            dbCustomer.CloseDatabase();
                            finish();
                        }
                    })
                    .show();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onHomePressed() {
        ActionBarUtils.navigateHome(this);
    }
}
