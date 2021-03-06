package com.foodbox.foodcabby.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.foodbox.foodcabby.models.Order;
import com.foodbox.foodcabby.models.OrderModel;
import com.foodbox.foodcabby.R;
import com.foodbox.foodcabby.adapter.OrdersAdapter;
import com.foodbox.foodcabby.build.api.ApiClient;
import com.foodbox.foodcabby.build.api.ApiInterface;
import com.foodbox.foodcabby.helper.ConnectionHelper;
import com.foodbox.foodcabby.helper.CustomDialog;
import com.foodbox.foodcabby.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.foodbox.foodcabby.helper.GlobalData.onGoingOrderList;
import static com.foodbox.foodcabby.helper.GlobalData.pastOrderList;

public class OrdersActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.orders_rv)
    RecyclerView ordersRv;
    @BindView(R.id.error_layout)
    LinearLayout errorLayout;
    Activity activity = OrdersActivity.this;
    Context context;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    List<OrderModel> modelList = new ArrayList<>();
    Intent orderIntent;
    ConnectionHelper connectionHelper;
    Runnable orderStatusRunnable;
    CustomDialog customDialog;
    private OrdersAdapter adapter;
    private List<OrderModel> modelListReference = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        context = OrdersActivity.this;
        customDialog = new CustomDialog(context);
        ButterKnife.bind(this);
        connectionHelper = new ConnectionHelper(context);
        setSupportActionBar(toolbar);
        onGoingOrderList = new ArrayList<>();
        pastOrderList = new ArrayList<>();
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setPadding(0, 0, 0, 0);//for tab otherwise give space in tab
        toolbar.setContentInsetsAbsolute(0, 0);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        ordersRv.setLayoutManager(manager);
        modelListReference.clear();
        adapter = new OrdersAdapter(this, activity, modelListReference);
        ordersRv.setAdapter(adapter);
        ordersRv.setHasFixedSize(false);

    }


    private void getPastOrders() {
        Call<List<Order>> call = apiInterface.getPastOders();
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                Log.e("onResponse","onResponse");
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    Log.e("onResponse","isSuccessful");
                    pastOrderList.clear();
                    pastOrderList = response.body();
                    OrderModel model = new OrderModel();
                    model.setHeader(getString(R.string.past_orders));
                    model.setOrders(pastOrderList);
                    modelList.add(model);
                    modelListReference.clear();
                    modelListReference.addAll(modelList);
                    adapter.notifyDataSetChanged();
                    if (onGoingOrderList.size() == 0 && pastOrderList.size() == 0) {
                        errorLayout.setVisibility(View.VISIBLE);
                    } else
                        errorLayout.setVisibility(View.GONE);
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                customDialog.dismiss();
                Toast.makeText(OrdersActivity.this, "Some thing went wrong"  + call.toString(), Toast.LENGTH_SHORT).show();
                Log.e("OrdersActivityonFail1",t.getMessage());
            }
        });

    }

    private void getOngoingOrders() {
        Call<List<Order>> call = apiInterface.getOngoingOrders();
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                if (response.isSuccessful()) {
                    onGoingOrderList.clear();
                    modelListReference.clear();
                    onGoingOrderList = response.body();
                    modelList.clear();
                    OrderModel model = new OrderModel();
                    model.setHeader("Current Orders");
                    model.setOrders(onGoingOrderList);
                    modelList.add(model);
                    modelListReference.addAll(modelList);
                    getPastOrders();
                } else {
                    getPastOrders();
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                Toast.makeText(OrdersActivity.this, "Something went wrong!" , Toast.LENGTH_SHORT).show();
                getPastOrders();
                customDialog.dismiss();
                Log.e("OrdersActivityonFail2",t.getMessage());
                Log.e("OrdersActivityonFail3",call.toString());
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        modelList.clear();
        //Get Ongoing Order list
        if (connectionHelper.isConnectingToInternet()) {
            customDialog.show();
            getOngoingOrders();
        } else {
            Utils.displayMessage(activity, context, getString(R.string.oops_connect_your_internet));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_nothing, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
