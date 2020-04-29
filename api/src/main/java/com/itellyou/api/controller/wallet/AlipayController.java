package com.itellyou.api.controller.wallet;

import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayAcquireQueryResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.itellyou.api.handler.response.Result;
import com.itellyou.model.user.UserInfoModel;
import com.itellyou.model.user.UserPaymentModel;
import com.itellyou.model.user.UserPaymentStatus;
import com.itellyou.service.ali.AlipayService;
import com.itellyou.service.user.UserPaymentService;
import com.itellyou.util.IPUtils;
import com.itellyou.util.annotation.MultiRequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;


@Validated
@RestController
@RequestMapping("/pay")
public class AlipayController {

    private final UserPaymentService paymentService;
    private final AlipayService alipayService;

    public AlipayController(UserPaymentService paymentService, AlipayService alipayService) {
        this.paymentService = paymentService;
        this.alipayService = alipayService;
    }

    @PostMapping("/alipay")
    public Result alipayPrecreate(HttpServletRequest request, @MultiRequestBody double amount, UserInfoModel userModel){
        if(userModel == null) return new Result(401,"未登陆");
        try {
            AlipayTradePrecreateResponse response = paymentService.preCreateAlipay("支付宝充值",amount,userModel.getId(), IPUtils.toLong(IPUtils.getClientIp(request)));
            return new Result().extend("id",response.getOutTradeNo()).extend("qr",response.getQrCode());
        }catch (Exception e){
            return new Result(500,e.getLocalizedMessage());
        }
    }

    @GetMapping("/alipay")
    public Result alipayQuery(HttpServletRequest request, @RequestParam String id, UserInfoModel userModel){
        if(userModel == null) return new Result(401,"未登陆");
        try {
            UserPaymentStatus status = paymentService.queryAlipay(id,userModel.getId(), IPUtils.toLong(IPUtils.getClientIp(request)));
            return new Result().extend("status",status);
        }catch (Exception e){
            return new Result(500,e.getLocalizedMessage());
        }
    }

    @GetMapping("/alipay/callback")
    public void callback(HttpServletRequest request,HttpServletResponse response, @RequestParam Map<String,String> params){
        String result = "failure";

        try {
            if(params != null){
                boolean signVerified = alipayService.rsaCheckV1(params);
                if(signVerified){
                    String orderId = params.get("out_trade_no");
                    UserPaymentModel paymentModel = paymentService.getDetail(orderId);
                    if(paymentModel != null && paymentModel.getStatus().equals(UserPaymentStatus.DEFAULT)){
                        paymentService.queryAlipay(orderId,paymentModel.getCreatedUserId(),IPUtils.toLong(IPUtils.getClientIp(request)));
                    }
                    result = "success";
                }
            }

            ServletOutputStream out = response.getOutputStream();
            OutputStreamWriter ow = new OutputStreamWriter(out, "UTF-8");
            ow.write(result);
            ow.flush();
            ow.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("/log")
    public Result log(UserInfoModel userModel, @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit){
        if(userModel == null) return new Result(401,"未登陆");
        Map<String,String > order = new HashMap<>();
        order.put("created_time","desc");
        return new Result(paymentService.page(null,null,null,userModel.getId(),null,null,null,order,offset,limit));
    }
}
