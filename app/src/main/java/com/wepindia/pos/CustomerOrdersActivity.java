/****************************************************************************
 * Project Name		:	VAJRA
 *
 * File Name		:	CustomerOrdersActivity
 *
 * Purpose			:	Represents customer order activity, takes care of all
 * 						UI back end operations in this activity, such as event
 * 						handling data read from or display in views.
 *
 * DateOfCreation	:	06-January-2013
 *
 * Author			:	Balasubramanya Bharadwaj B S
 *
 ****************************************************************************/
package com.wepindia.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.wep.common.app.Database.Customer;
import com.wep.common.app.Database.DatabaseHandler;
import com.wep.common.app.Database.RiderSettlement;
import com.wep.common.app.WepBaseActivity;
import com.wepindia.pos.GenericClasses.MessageDialog;
import com.wepindia.pos.utils.ActionBarUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerOrdersActivity extends WepBaseActivity{

	// Context object
	Context myContext;

	// DatabaseHandler object
	DatabaseHandler dbCustomerOrder = new DatabaseHandler(CustomerOrdersActivity.this);
	// MessageDialog object
	MessageDialog MsgBox;

	// View handlers
	TextView lblHeadingOrderDetails;
	EditText txtCustPhone, txtCustName, txtCustAddress, txtCustId, txtBillAmount,txtTime;
	Button btnAdd, btnOrder, btnTender, btnDelivery;
	ListView lstvwOrderCustName;
	ScrollView scrlOrderDetail;
	TableLayout tblOrderDetails, tblRider;

	// Variables
	//private static final String FILE_SHARED_PREFERENCE = "WeP_FnB";
	Cursor crsrCustomer = null, crsrPendingOrderItems = null;
	SimpleAdapter adapCustomerOrders;
	List<Map<String,String>> lstCustomerDetail;
	String BILLING_MODE = "";
	String strUserId = "", strUserName = "", strCustId = "", strPaymentStatus = "", strBillAmt = "";
	int iAccessLevel = 0;
	int iServiceTaxType = 0, iRiderCode = 0;
    double dDeliveryCharge = 0, dPettyCash = 0, dServiceTaxPercent = 0;

    EditText txtRiderName, txtPaidStatus, txtAmountDue;
    Dialog RiderDialog;
    String strRiderCode = "";
    LinearLayout lnrboxx6,lnrboxx7;

    Cursor crsrSettings;
	private Toolbar toolbar;
	private String title;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerorders);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

        /*getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.application_title_bar);
        TextView tvTitleText = (TextView)findViewById(R.id.tvTitleBarCaption);
        TextView tvTitleUserName = (TextView) findViewById(R.id.tvTitleBarUserName);
        TextView tvTitleDate = (TextView) findViewById(R.id.tvTitleBarDate);
		ActionBarUtils.goBack(this, findViewById(R.id.imgTitleBackIcon));
		ActionBarUtils.goHome(this, findViewById(R.id.imgTitleHomeIcon));
		ActionBarUtils.takeScreenshot(this, findViewById(R.id.imgTitleScreenshotIcon), findViewById(R.id.lnrCustomerOrder));*/
        //tvTitleText.setText("Customer Orders");

        myContext = this;

        try {
        	MsgBox = new MessageDialog(myContext);

        	InitializeViews();
            ResetCustomerOrder();

        	BILLING_MODE = getIntent().getStringExtra("BILLING_MODE");
        	strUserId = getIntent().getStringExtra("USER_ID");
        	strUserName = getIntent().getStringExtra("USER_NAME");
        	iAccessLevel = getIntent().getIntExtra("ACCESS_LEVEL", 1);

            //tvTitleUserName.setText(strUserName.toUpperCase());
            Date d = new Date();
            CharSequence s = DateFormat.format("dd-MM-yyyy", d.getTime());
            //tvTitleDate.setText("Date : " + s);
        	Log.d("CustomerOrderActivity", "Billing Mode:" + BILLING_MODE);

        	if(BILLING_MODE.equalsIgnoreCase("3")){
        		title = "PickUp";
        		btnDelivery.setVisibility(View.GONE);
				btnTender.setText("Pay Bill");
        	} else if(BILLING_MODE.equalsIgnoreCase("4")){
				title  = "Delivery";
                btnTender.setText("COD Settlement");
				btnDelivery.setVisibility(View.GONE);
                //btnDelivery.setText("Assign Driver");
        	}
			com.wep.common.app.ActionBarUtils.setupToolbar(this,toolbar,getSupportActionBar(),title,strUserName," Date:"+s.toString());

			dbCustomerOrder.CreateDatabase();
			dbCustomerOrder.OpenDatabase();

            crsrSettings = dbCustomerOrder.getBillSetting();
			if(crsrSettings.moveToFirst()){
				iServiceTaxType = crsrSettings.getInt(crsrSettings.getColumnIndex("ServiceTaxType"));
				dServiceTaxPercent = crsrSettings.getDouble(crsrSettings.getColumnIndex("ServiceTaxPercent"));
			}

			LoadOrderToList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	private void InitializeViews(){
		lblHeadingOrderDetails = (TextView)findViewById(R.id.tvHeadingPendingOrderDetails);

		txtCustPhone = (EditText)findViewById(R.id.etOrderCustomerPhone);
		txtCustPhone.setOnKeyListener(Phone_Search_KeyPress);
		txtCustName = (EditText)findViewById(R.id.etOrderCustomerName);
		txtCustAddress = (EditText)findViewById(R.id.etOrderCustomerAddress);
		txtCustId = (EditText)findViewById(R.id.etOrderCustomerId);
		txtTime = (EditText)findViewById(R.id.etOrderCustomerTime);
		txtBillAmount = (EditText) findViewById(R.id.etOrderBillAmount);
		btnAdd = (Button)findViewById(R.id.btn_OrderCustomerAddCustomer);
		btnOrder = (Button)findViewById(R.id.btn_OrderCustomerOrder);
		btnTender = (Button)findViewById(R.id.btn_OrderCustomerTender);
		btnDelivery = (Button)findViewById(R.id.btn_OrderCustomerDelivery);

		lstvwOrderCustName = (ListView)findViewById(R.id.lstCustomerPendingOrders);
		lstvwOrderCustName.setOnItemClickListener(CustomerOrderListClick);

		tblOrderDetails = (TableLayout)findViewById(R.id.tblPendingOrderDetails);

        // Rider Selection
		scrlOrderDetail = (ScrollView)findViewById(R.id.scrlPendingOrderDetailsTable);
        tblRider = (TableLayout) findViewById(R.id.tblCustRider);
        txtRiderName = (EditText)findViewById(R.id.etCustDeliveryRiderName);
        txtPaidStatus = (EditText) findViewById(R.id.etCustPaidStatus);
        txtAmountDue = (EditText) findViewById(R.id.etCustAmountDue);
        lnrboxx6 = (LinearLayout) findViewById(R.id.boxx6);
        lnrboxx7 = (LinearLayout) findViewById(R.id.boxx7);
	}

	OnKeyListener Phone_Search_KeyPress = new OnKeyListener() {

		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub

			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((EditText) v).getWindowToken(), 0);

				if(((EditText)v).getText().toString().equalsIgnoreCase("")){
					MsgBox.Show("Warning", "Please enter phone number to search");
				} else {

					crsrCustomer = dbCustomerOrder.getCustomer(txtCustPhone.getText().toString());
					if(crsrCustomer.moveToFirst()){
						txtCustName.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustName")));
						txtCustAddress.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustAddress")));
						txtCustId.setText(crsrCustomer.getString(crsrCustomer.getColumnIndex("CustId")));

						btnAdd.setEnabled(false);
						btnOrder.setEnabled(true);

					} else {
						txtCustName.requestFocus();
						Log.d("", "Customer not found for the phone number " + txtCustPhone.getText().toString());
					}
					return true;
				}
			}

			return false;
		}
	};

	OnItemClickListener CustomerOrderListClick = new OnItemClickListener(){

		public void onItemClick(AdapterView<?> adapter, View v, int position,
				long id) {
			// TODO Auto-generated method stub
			ResetCustomerOrder();

			Map<String, String> CustData = new HashMap<String, String>(5);
			CustData = lstCustomerDetail.get(position);

			LoadPendingOrderItems(Integer.parseInt(CustData.get("Id")));

			txtCustName.setText(CustData.get("Name"));
			txtCustPhone.setText(CustData.get("Phone").substring(7));
			txtCustAddress.setText(CustData.get("Address"));
			txtCustId.setText(CustData.get("Id"));
			Log.d("Customer Orders Click", "Customer Id:" + CustData.get("Id"));
			txtTime.setText(CustData.get("Time"));
			Log.d("Customer Orders Click", "Customer Time:" + CustData.get("Time"));
            btnOrder.setEnabled(true);

            if(BILLING_MODE.equalsIgnoreCase("4"))
            {
                lnrboxx6.setVisibility(View.VISIBLE);
            } else {
                lnrboxx6.setVisibility(View.INVISIBLE);
            }

		}

	};

	private void LoadOrderToList(){

		lstCustomerDetail = new ArrayList<Map<String,String>>();

		Cursor crsrCustomerOrders = dbCustomerOrder.getCustomerPendingOrders(BILLING_MODE);

		if(crsrCustomerOrders.moveToFirst()){
			do{
				Map<String, String> CustData = new HashMap<String, String>(5);

				CustData.put("Id", crsrCustomerOrders.getString
						(crsrCustomerOrders.getColumnIndex("CustId")));
				CustData.put("Name", crsrCustomerOrders.getString
						(crsrCustomerOrders.getColumnIndex("CustName")));
				CustData.put("Phone", "Phone: " + crsrCustomerOrders.getString
						(crsrCustomerOrders.getColumnIndex("CustContactNumber")));
				CustData.put("Address", crsrCustomerOrders.getString
						(crsrCustomerOrders.getColumnIndex("CustAddress")));
				CustData.put("Time", crsrCustomerOrders.getString
						(crsrCustomerOrders.getColumnIndex("Time")));

				if(!lstCustomerDetail.contains(CustData)){

					lstCustomerDetail.add(CustData);
				}

			}while(crsrCustomerOrders.moveToNext());

		} else {
			Log.d("LoadOrderToList", "No Pending Orders");
		}

		adapCustomerOrders = new SimpleAdapter(myContext,lstCustomerDetail,
				android.R.layout.simple_list_item_2,
				new String[]{"Name","Phone"},
				new int[] {android.R.id.text1,android.R.id.text2});

		lstvwOrderCustName.setAdapter(adapCustomerOrders);
	}

	@SuppressWarnings("deprecation")
	private void LoadPendingOrderItems(int CustId){
		TextView tvSno, tvItemName, tvQty;
		TableRow rowItem = null;
		crsrPendingOrderItems = dbCustomerOrder.getKOTItems(CustId,BILLING_MODE);
        int i = 1;
		if(crsrPendingOrderItems.moveToFirst()){
			do{
				rowItem = new TableRow(myContext);
				rowItem.setLayoutParams(new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				rowItem.setBackgroundResource(R.drawable.row_background);

                // S.No
                tvSno = new TextView(myContext);
                tvSno.setGravity(1);
                tvSno.setText(String.valueOf(i));

				// Item Name
				tvItemName = new TextView(myContext);
				tvItemName.setText(crsrPendingOrderItems.getString(crsrPendingOrderItems.getColumnIndex("ItemName")));

				// Item Name
				tvQty = new TextView(myContext);
                tvQty.setGravity(1);
				tvQty.setText(crsrPendingOrderItems.getString(crsrPendingOrderItems.getColumnIndex("Quantity")));

				// Add text views to row
                rowItem.addView(tvSno);
				rowItem.addView(tvItemName);
				rowItem.addView(tvQty);

				// Add row to table
				tblOrderDetails.addView(rowItem,
						new LayoutParams(LayoutParams.FILL_PARENT,
								LayoutParams.WRAP_CONTENT));
                i++;
			}while(crsrPendingOrderItems.moveToNext());

            CalculateTotalAmount();
		} else {
			Log.d("LoadPendingOrderItems", "No pending order items");
		}
	}

	public void Clear (View view)
	{
        ResetCustomerOrder();
        lnrboxx6.setVisibility(View.INVISIBLE);
        lnrboxx7.setVisibility(View.INVISIBLE);
    }

	private void CalculateTotalAmount(){

        double dBillTotal = 0, dSubTotal = 0, dTaxTotal = 0, dOtherChrgs = 0, dServiceTaxAmt = 0, dTaxAmt = 0, dSerTaxAmt = 0;
        float dTaxPercent = 0, dSerTaxPercent = 0;

		if(crsrPendingOrderItems.moveToFirst()){
            // Item wsie Calculation -----------------------------
			do{
				dTaxTotal += crsrPendingOrderItems.getDouble
						(crsrPendingOrderItems.getColumnIndex("TaxAmount"));
				dServiceTaxAmt += crsrPendingOrderItems.getDouble
						(crsrPendingOrderItems.getColumnIndex("ServiceTaxAmount"));

                dSubTotal += crsrPendingOrderItems.getDouble(crsrPendingOrderItems.getColumnIndex("Amount"));

			}while(crsrPendingOrderItems.moveToNext());
            // --------------------------------------

            // Other Charges ------------------
            Cursor crssOtherChrg = dbCustomerOrder.getKOTModifierByModes("HomeDelivery");
            if (crssOtherChrg.moveToFirst()) {
                do {
                    dOtherChrgs += crssOtherChrg.getDouble(crssOtherChrg.getColumnIndex("ModifierAmount"));
                } while (crssOtherChrg.moveToNext());
            }
            // -----------------------------------

            // Bill wise Tax Calculation ---------------
            Cursor crsrtax = dbCustomerOrder.getTaxConfig(1);
            if (crsrtax.moveToFirst()) {
                dTaxPercent = crsrtax.getFloat(crsrtax.getColumnIndex("TotalPercentage"));
                dTaxAmt += dSubTotal * (dTaxPercent / 100);
            }
            Cursor crsrtax1 = dbCustomerOrder.getTaxConfig(2);
            if (crsrtax1.moveToFirst()) {
                dSerTaxPercent = crsrtax1.getFloat(crsrtax1.getColumnIndex("TotalPercentage"));
                dSerTaxAmt += dSubTotal * (dSerTaxPercent / 100);
            }
            // -----------------------


            if(crsrSettings.getString(crsrSettings.getColumnIndex("Tax")).equalsIgnoreCase("1")) {
                if (crsrSettings.getString(crsrSettings.getColumnIndex("TaxType")).equalsIgnoreCase("1")) {
                    dBillTotal = dSubTotal + dTaxTotal + dServiceTaxAmt + dOtherChrgs;
                } else {
                    dBillTotal = dSubTotal + dTaxAmt + dSerTaxAmt + dOtherChrgs;
                }
            }
            else
            {
                if (crsrSettings.getString(crsrSettings.getColumnIndex("TaxType")).equalsIgnoreCase("1")) {
                    dBillTotal = dSubTotal + dOtherChrgs;
                } else {
                    dBillTotal = dSubTotal + dOtherChrgs;
                }
            }

		}
        txtBillAmount.setText(String.format("%.2f", dBillTotal));
	}

	private void ResetCustomerOrder(){

		txtCustPhone.setText("");
		txtCustName.setText("");
		txtCustAddress.setText("");
		txtCustId.setText("");
        txtBillAmount.setText("");
		btnAdd.setEnabled(true);
		btnOrder.setEnabled(false);
		btnTender.setEnabled(true);
		btnDelivery.setEnabled(true);

		for(int iPosition = tblOrderDetails.getChildCount() -1; iPosition >= 1; iPosition--){
			TableRow rowOrderItem = (TableRow)tblOrderDetails.getChildAt(iPosition);
			// Remove all views present in row
			rowOrderItem.removeAllViews();
			// Remove the row
			tblOrderDetails.removeView(rowOrderItem);
		}
        ResetDelivery();
	}

	private void LaunchBillScreen(){
		//SharedPreferences spUser = getSharedPreferences(FILE_SHARED_PREFERENCE, 0);
		Intent intentBillScreen = new Intent(myContext,BillingScreenActivity.class);
		intentBillScreen.putExtra("BILLING_MODE", BILLING_MODE);
		intentBillScreen.putExtra("USER_ID", strUserId);//spUser.getString("USER_ID", "GHOST"));
		intentBillScreen.putExtra("USER_NAME", strUserName);//spUser.getString("USER_NAME", "GHOST"));
        intentBillScreen.putExtra("CUST_ID", Integer.parseInt(txtCustId.getText().toString()));
        intentBillScreen.putExtra("Payment_Status", strPaymentStatus);
		intentBillScreen.putExtra("MAKE_ORDER", "NO");
		//startActivityForResult(intentBillScreen,1);
		setResult(RESULT_OK, intentBillScreen);

        //this.finish();
	}

	private void SaveRiderdelivery(String CustId, String BillNimber){
		long lResult = 0;

		RiderSettlement objRiderSettlement = new RiderSettlement();

		// BillNumber
		objRiderSettlement.setBillNumber(Integer.valueOf(BillNimber));
		Log.d("SaveRiderDelivery", "Bill Number:" + dbCustomerOrder.getNewBillNumbers());

		// TotalItems
		objRiderSettlement.setTotalItems(crsrPendingOrderItems.getCount());
		Log.d("SaveRiderDelivery", "Total Items:" + crsrPendingOrderItems.getCount());

		// BillAmount
		objRiderSettlement.setBillAmount(Float.parseFloat
				(String.format("%.2f", Double.parseDouble(txtBillAmount.getText().toString()))));
		Log.d("SaveRiderDelivery", "Bill Amount:" + String.format("%.2f", Double.parseDouble(txtBillAmount.getText().toString())));

		// EmployeeId
		objRiderSettlement.setEmployeeId(iRiderCode);
		Log.d("SaveRiderDelivery", "Rider Code:" + iRiderCode);

		// Delivery Cash
		objRiderSettlement.setDeliveryCharge(Float
				.parseFloat(String.format("%.2f", dDeliveryCharge)));
		Log.d("SaveRiderDelivery", "Delivery Charge:" + String.format("%.2f", dDeliveryCharge));

		// Petty Cash
		objRiderSettlement.setPettyCash(Float.parseFloat
				(String.format("%.2f", dPettyCash)));
		Log.d("SaveRiderDelivery", "Petty Cash:" + String.format("%.2f", dPettyCash));

		// SettledAmount
		objRiderSettlement.setSettledAmount(0);
		Log.d("SaveRiderDelivery", "Settled Amount:0");

        // Cust Id
        objRiderSettlement.setCustId(Integer.valueOf(CustId));

		lResult = dbCustomerOrder.addRiderSettlement(objRiderSettlement);

		Log.d("RiderSettlement", "Rider delivery bill inserted at:" + lResult);
	}

	public void Add(View v){
		if(txtCustPhone.getText().toString().equalsIgnoreCase("") ||
				txtCustName.getText().toString().equalsIgnoreCase("")){
			MsgBox.Show("Warning", "Please enter phone number and name before adding customer");
		} else {
			long lResult = 0;
			Customer objCustomer;
			objCustomer = new Customer();

			// Customer Name
			objCustomer.setCustName(txtCustName.getText().toString());

			// Customer Phone
			objCustomer.setCustContactNumber(txtCustPhone.getText().toString());

			// Customer Address
			objCustomer.setCustAddress(txtCustAddress.getText().toString());

			// Insert customer details to database
			lResult = dbCustomerOrder.addCustomer(objCustomer);

			if(lResult > 0){
				txtCustId.setText(String.valueOf(lResult));
				btnAdd.setEnabled(false);
				btnOrder.setEnabled(true);
			}

			Log.d("CustomerOrder", "New Customer added at position:" + lResult);
		}
	}

	public void Order(View v){
		//LaunchBillScreen();

        Intent intentBillScreen = new Intent(myContext,BillingScreenActivity.class);
        intentBillScreen.putExtra("BILLING_MODE", BILLING_MODE);
        intentBillScreen.putExtra("USER_ID", strUserId);//spUser.getString("USER_ID", "GHOST"));
        intentBillScreen.putExtra("USER_NAME", strUserName);//spUser.getString("USER_NAME", "GHOST"));
        intentBillScreen.putExtra("CUST_ID", Integer.parseInt(txtCustId.getText().toString()));
        intentBillScreen.putExtra("MAKE_ORDER", "YES");
        intentBillScreen.putExtra("PAYMENT_STATUS",strPaymentStatus);
        //startActivityForResult(intentBillScreen,1);
        setResult(RESULT_OK, intentBillScreen);
        this.finish();
	}

	public void Tender(View v){
		if(BILLING_MODE.equalsIgnoreCase("3")){
			if(tblOrderDetails.getChildCount() <= 1){
				MsgBox.Show("Warning", "No pick up order is selected to tender");
			} else {
				//LaunchBillScreen();
                Intent intentBillScreen = new Intent(myContext,BillingScreenActivity.class);
                intentBillScreen.putExtra("BILLING_MODE", BILLING_MODE);
                intentBillScreen.putExtra("USER_ID", strUserId);//spUser.getString("USER_ID", "GHOST"));
                intentBillScreen.putExtra("USER_NAME", strUserName);//spUser.getString("USER_NAME", "GHOST"));
                intentBillScreen.putExtra("CUST_ID", Integer.parseInt(txtCustId.getText().toString()));
                intentBillScreen.putExtra("MAKE_ORDER", "NO");
                //startActivityForResult(intentBillScreen,1);
                setResult(RESULT_OK, intentBillScreen);
                this.finish();
			}

		} else {
			Cursor PendingDelivery = dbCustomerOrder.getRiderPendingDelivery();
			if(PendingDelivery.moveToFirst()){
				startActivity(new Intent(myContext,RiderSettlementActivity.class));
			} else {
				MsgBox.Show("Warning", "No pending delivery order is present to tender");
			}
		}
	}

	public void Delivery(View v){
		if(tblOrderDetails.getChildCount() <= 1){
			MsgBox.Show("Warning", "Select one order to deliver");
		} else {
            Intent intentTender = new Intent(myContext, DeliveryActivity.class);
            intentTender.putExtra("CUST_ID", txtCustId.getText().toString());
			intentTender.putExtra("BILLAMT", txtBillAmount.getText().toString());
            Cursor crsrBillDetail = dbCustomerOrder.getBillDetailByCustomerWithTime(Integer.valueOf(txtCustId.getText().toString()), 2, Float.parseFloat(txtBillAmount.getText().toString()));
            if(crsrBillDetail.moveToFirst()) {
                intentTender.putExtra("DueAmount", "0");
            }
            else
            {
                intentTender.putExtra("DueAmount", txtBillAmount.getText().toString());
            }
            startActivityForResult(intentTender, 2);
			//startActivityForResult(new Intent(myContext,DeliveryActivity.class),2);
            ResetCustomerOrder();
            LoadOrderToList();
		}
	}

	public void Close(View v){
		// Close database connection
		dbCustomerOrder.CloseDatabase();
		// finish activity
		this.finish();
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
        case 1:	// Bill Screen Activity Result
        	ResetCustomerOrder();
			LoadOrderToList();
        	break;

        case 2:	// Delivery Activity Result
        	if(resultCode == RESULT_OK){
        		iRiderCode = data.getIntExtra("RIDER_CODE", 0);
        		dDeliveryCharge = data.getDoubleExtra("DELIVERY_CHARGE", 0);
        		dPettyCash = data.getDoubleExtra("PETTY_CASH", 0);
                strCustId = data.getStringExtra("CUST_ID");
                strPaymentStatus = data.getStringExtra("PAYMENT_STATUS");
                strBillAmt = data.getStringExtra("BILLAMT");

        		Log.d("Delivery Result", "Rider Code:" + iRiderCode +
        				" Delivery Cahrge:" + dDeliveryCharge + " Petty Cash:" + dPettyCash);

				Cursor crsrBillDetail = dbCustomerOrder.getBillDetailByCustomerWithTime(Integer.valueOf(strCustId), 2, Float.parseFloat(strBillAmt));
                if(crsrBillDetail.moveToFirst()) {
                    txtBillAmount.setText(crsrBillDetail.getString(crsrBillDetail.getColumnIndex("BillAmount")));
                    // Insert details to RiderSettlement table
                    SaveRiderdelivery(strCustId, crsrBillDetail.getString(crsrBillDetail.getColumnIndex("InvoiceNo")));

                    // Launch bill screen to save bill to database
                    Toast.makeText(myContext, "This Customer already Paid the amount", Toast.LENGTH_LONG).show();
                    Cursor crsrRiderSettlement = dbCustomerOrder.getRiderSettlementByCustId(Integer.valueOf(strCustId));
                    if(crsrRiderSettlement.moveToFirst()) {
                        int iResult = dbCustomerOrder.deleteKOTItems(Integer.valueOf(strCustId), String.valueOf(4));
                        Log.d("Delivery:", "Items deleted from pending KOT:" + iResult);
                    }
                    txtCustId.setText(strCustId.toString());
                    //LaunchBillScreen();
                }
                else
                {
                    // Calculate bill amount
                    CalculateTotalAmount();

                    // Insert details to RiderSettlement table
                    SaveRiderdelivery(strCustId, String.valueOf(dbCustomerOrder.getNewBillNumbers()));
                    txtCustId.setText(strCustId);

                    //LaunchBillScreen();
                }

                //ResetCustomerOrder();
                //LoadOrderToList();
                //LaunchBillScreen();
                Intent intentBillScreen = new Intent(myContext,BillingScreenActivity.class);
                intentBillScreen.putExtra("BILLING_MODE", BILLING_MODE);
                intentBillScreen.putExtra("USER_ID", strUserId);//spUser.getString("USER_ID", "GHOST"));
                intentBillScreen.putExtra("USER_NAME", strUserName);//spUser.getString("USER_NAME", "GHOST"));
                intentBillScreen.putExtra("CUST_ID", Integer.parseInt(txtCustId.getText().toString()));
                intentBillScreen.putExtra("Payment_Status", strPaymentStatus);
                intentBillScreen.putExtra("MAKE_ORDER", "NO");
                //startActivityForResult(intentBillScreen,1);
                setResult(RESULT_OK, intentBillScreen);

                this.finish();
        	}
        	break;
        }
	}

	public void CancelOrder(View view)
	{
       // boolean r = txtCustId.getText().toString().equals("");
        if((txtCustId!= null && !txtCustId.getText().toString().equals("")))
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(myContext)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to Delete this Order")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            int iResult = dbCustomerOrder.deleteKOTItems(Integer.valueOf(txtCustId.getText().toString()), String.valueOf(BILLING_MODE));
                            Log.d("Delivery:", "Items deleted from pending KOT:" + iResult);
                            MsgBox.Show("", "Customer Order Deleted Successfully");

                            ResetCustomerOrder();
                            LoadOrderToList();

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
        else
        {
            MsgBox = new MessageDialog(myContext);
            MsgBox.Show("Insufficient Information", "Please Select the Order to Cancel");
        }
	}

	public void AssignDriver(View view)
	{
        // custom dialog
        RiderDialog = new Dialog(myContext);
        RiderDialog.setContentView(R.layout.activity_riderselection_layout);
        RiderDialog.setTitle("Rider Selection");

        // set the custom dialog components - text, image and button
        tblRider = (TableLayout) RiderDialog.findViewById(R.id.tblCustRider);
        TableRow rowRider = null;
        TextView tvSno, Code, Name, Status;
        Cursor crsrRider = dbCustomerOrder.getAllDeliveryRiders();
        int i = 1;
        if(crsrRider.moveToFirst()){
            do{
                rowRider = new TableRow(myContext);
                rowRider.setLayoutParams(new LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

                //S.No
                tvSno = new TextView(myContext);
                tvSno.setTextSize(18);
                tvSno.setGravity(1);
                tvSno.setHeight(40);
                tvSno.setText(String.valueOf(i));

                // Rider code
                Code = new TextView(myContext);
                Code.setTextSize(18);
                Code.setText(crsrRider.getString(crsrRider.getColumnIndex("UserId")));

                // Rider Name
                Name = new TextView(myContext);
                Name.setTextSize(18);
                Name.setText(crsrRider.getString(crsrRider.getColumnIndex("Name")));

                // Rider status
                Status = new TextView(myContext);
                Status.setText("Free");

                // add views to row
                rowRider.addView(tvSno);
                rowRider.addView(Code);
                rowRider.addView(Name);
                rowRider.addView(Status);

                rowRider.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if(((TableRow)v).getChildAt(0) != null){

                            TextView RiderCode = (TextView) ((TableRow)v).getChildAt(1);
                            TextView RiderName = (TextView) ((TableRow)v).getChildAt(2);
                            strRiderCode = RiderCode.getText().toString();
                            txtRiderName.setText(RiderName.getText());
                            String time = txtTime.getText().toString();
                            int paid  = dbCustomerOrder.getBillDetailByCustomerWithTime1(Integer.valueOf(txtCustId.getText().toString()),
                                    2,Float.parseFloat(txtBillAmount.getText().toString()),time);
                            if(paid ==1) {
                                txtPaidStatus.setText("Paid");
                                txtAmountDue.setText("0");
                            }
                            else
                            {
                                txtPaidStatus.setText("Cash on Delivery");
                                txtAmountDue.setText(txtBillAmount.getText().toString());
                            }
                            lnrboxx7.setVisibility(View.VISIBLE);

                            RiderDialog.dismiss();
                            Log.d("Delivery", "Selected Rider - Code:" + strRiderCode +
                                    " Name:" + txtRiderName.getText().toString());
                        }
                    }
                });

                // Add row to table
                tblRider.addView(rowRider,
                        new LayoutParams(LayoutParams.FILL_PARENT,
                                LayoutParams.WRAP_CONTENT));

                i++;
            }while(crsrRider.moveToNext());
        }
        RiderDialog.show();
	}

    public void OK(View v){

        double dDeliveryCharge = 0, dPettyCash = 0;

        if(txtRiderName.getText().toString().equalsIgnoreCase("")){
            MsgBox.Show("Warning", "Please Select Rider for Delivery");
            return;
        } else {

			try
			{
				PayBill();
			} catch (Exception e)
			{
				e.printStackTrace();
				MsgBox.Show("",e.getMessage());
			}


        }
    }

    private void ResetDelivery()
    {
        txtRiderName.setText("");
        txtAmountDue.setText("");
        txtPaidStatus.setText("");
    }

    public void PrintBill(View view)
    {
        double dDeliveryCharge = 0, dPettyCash = 0;

        if(txtRiderName.getText().toString().equalsIgnoreCase("")){
            MsgBox.Show("Warning", "Please Select Rider for Delivery");
            return;
        } else {

            PayBill();
        }
    }

    protected void PayBill()
    {
        iRiderCode = Integer.valueOf(strRiderCode);
        strCustId = txtCustId.getText().toString();
        strPaymentStatus = txtPaidStatus.getText().toString();
        strBillAmt = txtBillAmount.getText().toString();

        Log.d("Delivery Result", "Rider Code:" + iRiderCode +
                " Delivery Cahrge:" + dDeliveryCharge + " Petty Cash:" + dPettyCash);

        Cursor crsrBillDetail = dbCustomerOrder.getBillDetailByCustomerWithTime(Integer.valueOf(strCustId), 2, Float.parseFloat(strBillAmt));
        if(crsrBillDetail.moveToFirst()) {
            txtBillAmount.setText(crsrBillDetail.getString(crsrBillDetail.getColumnIndex("BillAmount")));
            // Insert details to RiderSettlement table
            SaveRiderdelivery(strCustId, crsrBillDetail.getString(crsrBillDetail.getColumnIndex("InvoiceNo")));

            // Launch bill screen to save bill to database
            Toast.makeText(myContext, "This Customer already Paid the amount", Toast.LENGTH_LONG).show();
            Cursor crsrRiderSettlement = dbCustomerOrder.getRiderSettlementByCustId(Integer.valueOf(strCustId));
            if(crsrRiderSettlement.moveToFirst()) {
                int iResult = dbCustomerOrder.deleteKOTItems(Integer.valueOf(strCustId), String.valueOf(4));
                Log.d("Delivery:", "Items deleted from pending KOT:" + iResult);
            }
            txtCustId.setText(strCustId.toString());
            //LaunchBillScreen();
        }
        else
        {
            // Calculate bill amount
            CalculateTotalAmount();

            // Insert details to RiderSettlement table
            SaveRiderdelivery(strCustId, String.valueOf(dbCustomerOrder.getNewBillNumbers()));
            txtCustId.setText(strCustId);

            //LaunchBillScreen();
        }

        //ResetCustomerOrder();
        //LoadOrderToList();
        //LaunchBillScreen();
        Intent intentBillScreen = new Intent(myContext,BillingScreenActivity.class);
        intentBillScreen.putExtra("BILLING_MODE", BILLING_MODE);
        intentBillScreen.putExtra("USER_ID", strUserId);//spUser.getString("USER_ID", "GHOST"));
        intentBillScreen.putExtra("USER_NAME", strUserName);//spUser.getString("USER_NAME", "GHOST"));
        intentBillScreen.putExtra("CUST_ID", Integer.parseInt(txtCustId.getText().toString()));
        intentBillScreen.putExtra("Payment_Status", strPaymentStatus);
        intentBillScreen.putExtra("MAKE_ORDER", "NO");
        //startActivityForResult(intentBillScreen,1);
        setResult(RESULT_OK, intentBillScreen);

        this.finish();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			AlertDialog.Builder AuthorizationDialog = new AlertDialog.Builder(myContext);
			LayoutInflater UserAuthorization = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View vwAuthorization = UserAuthorization.inflate(R.layout.user_authorization, null);
			final EditText txtUserId = (EditText) vwAuthorization.findViewById(R.id.etAuthorizationUserId);
			final EditText txtPassword = (EditText) vwAuthorization.findViewById(R.id.etAuthorizationUserPassword);
			final TextView tvAuthorizationUserId= (TextView) vwAuthorization.findViewById(R.id.tvAuthorizationUserId);
			final TextView tvAuthorizationUserPassword= (TextView) vwAuthorization.findViewById(R.id.tvAuthorizationUserPassword);
			tvAuthorizationUserId.setVisibility(View.GONE);
			tvAuthorizationUserPassword.setVisibility(View.GONE);
			txtUserId.setVisibility(View.GONE);
			txtPassword.setVisibility(View.GONE);
			AuthorizationDialog
					.setTitle("Are you sure you want to exit ?")
					.setView(vwAuthorization)
					.setNegativeButton("No", null)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							/*Intent returnIntent =new Intent();
							setResult(Activity.RESULT_OK,returnIntent);*/
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