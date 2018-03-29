package com.todaycoder.dior;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mob.MobSDK;
import org.json.JSONObject;


import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class PhoneLoginActivity extends Activity implements View.OnClickListener{
    private EditText inputPhoneNUmber;
    private EditText inputVerifyCode;
    private Button getVerifyCodeBtn;
    private Button phoneLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        inputPhoneNUmber = (EditText) findViewById(R.id.input_phone_number);
        inputVerifyCode = (EditText) findViewById(R.id.input_verify_code);
        getVerifyCodeBtn = (Button) findViewById(R.id.get_verify_code_btn);
        phoneLoginBtn = (Button) findViewById(R.id.phone_login_btn);
        getVerifyCodeBtn.setOnClickListener(this);
        phoneLoginBtn.setOnClickListener(this);
        MobSDK.init(this);
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    @Override
    public void onClick(View view) {
        String phoneNUm = inputPhoneNUmber.getText().toString().trim();
        String verifyNum = inputVerifyCode.getText().toString().trim();
        switch (view.getId()) {
            case R.id.get_verify_code_btn:
                if(phoneNUm.length() != 11) {
                    Toast.makeText(getApplicationContext(), "手机号有误", Toast.LENGTH_SHORT).show();
                    return ;
                }
                SMSSDK.getVerificationCode("86", phoneNUm);
                break;
            case R.id.phone_login_btn:
                if(phoneNUm.length() != 11) {
                    Toast.makeText(getApplicationContext(), "手机号码不能为空", Toast.LENGTH_SHORT).show();
                    return ;
                }
                if(verifyNum.length() != 4) {
                    Toast.makeText(getApplicationContext(), "验证码不能为空", Toast.LENGTH_SHORT).show();
                    return ;
                }
                SMSSDK.submitVerificationCode("86", phoneNUm, verifyNum);
                break;
            default:
                break;
        }
    }


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.i("result", String.valueOf(result));
            Log.i("event", String.valueOf(event));
            if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                if(result == SMSSDK.RESULT_COMPLETE) {
                    startActivity(new Intent(PhoneLoginActivity.this, MainActivity.class));
                }else {
                    Toast.makeText(getApplicationContext(), "验证码错误", Toast.LENGTH_SHORT).show();
                }
            } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                if(result == SMSSDK.RESULT_COMPLETE) {
                    inputVerifyCode.requestFocus();
                    CountDownThread();
                    Toast.makeText(getApplicationContext(), "验证码已发送", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "验证码发送失败", Toast.LENGTH_SHORT).show();
                }

            } else if (result == SMSSDK.RESULT_ERROR) {
                try {
                    Throwable throwable = (Throwable) data;
                    throwable.printStackTrace();
                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");
                    int status = object.optInt("status");
                    if (status > 0 && !TextUtils.isEmpty(des)) {
                        Toast.makeText(PhoneLoginActivity.this, des, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };

    public void CountDownThread() {
        new CountDownTimer(60000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                getVerifyCodeBtn.setClickable(false);
                getVerifyCodeBtn.setText(millisUntilFinished /1000 + " s");
            }

            @Override
            public void onFinish() {
                getVerifyCodeBtn.setClickable(true);
                getVerifyCodeBtn.setText("重新发送");
            }
        }.start();
    }
}
