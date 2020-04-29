package com.itellyou.api.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.itellyou.api.handler.response.Result;
import com.itellyou.model.ali.DmLogModel;
import com.itellyou.model.ali.SmsLogModel;
import com.itellyou.model.sys.SysPath;
import com.itellyou.model.sys.SysPathModel;
import com.itellyou.model.user.UserInfoModel;
import com.itellyou.service.ali.DmLogService;
import com.itellyou.service.ali.SmsLogService;
import com.itellyou.service.sys.SysPathService;
import com.itellyou.service.user.UserInfoService;
import com.itellyou.service.user.UserSearchService;
import com.itellyou.util.DateUtils;
import com.itellyou.util.IPUtils;
import com.itellyou.util.annotation.MultiRequestBody;
import com.itellyou.util.serialize.filter.Labels;
import com.itellyou.util.validation.Mobile;
import com.itellyou.util.validation.Password;
import com.itellyou.util.validation.Path;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/user/update")
public class UserUpdateController {

    private final UserInfoService userInfoService;
    private final UserSearchService userSearchService;
    private final SmsLogService smsLogService;
    private final DmLogService dmLogService;
    private final SysPathService pathService;

    public UserUpdateController(UserInfoService userInfoService,UserSearchService userSearchService,SmsLogService smsLogService,DmLogService dmLogService,SysPathService pathService){
        this.userInfoService = userInfoService;
        this.userSearchService = userSearchService;
        this.smsLogService = smsLogService;
        this.dmLogService = dmLogService;
        this.pathService = pathService;
    }

    @PutMapping("/mobile")
    public Result mobile(HttpServletRequest request, UserInfoModel userModel, @MultiRequestBody @NotBlank @Mobile String mobile, @MultiRequestBody @NotBlank String code){
        if(userModel == null) return new Result(403,"未登陆");
        List<SmsLogModel> listLog = smsLogService.searchByTemplateAndMobile("replace",mobile);
        SmsLogModel checkLog = null;
        for(SmsLogModel smsLogModel : listLog){
            Map<String,String> dataMap = JSONObject.parseObject(smsLogModel.getData(),new TypeReference<Map<String,String>>(){});
            if(StringUtils.isNotEmpty(dataMap.get("code")) && dataMap.get("code").equals(code)){
                checkLog = smsLogModel;
                break;
            }
        }

        if(checkLog == null){
            return new Result(1001,"验证码错误或已过期");
        }

        int resultRows = smsLogService.updateStatus(3,checkLog.getId());
        if(resultRows == 0){
            return new Result(0,"更新验证码状态失败");
        }

        UserInfoModel mobileUser = userSearchService.findByMobile(mobile);
        if(mobileUser != null || userModel.getMobile() == mobile){
            return new Result(1002,"手机号已被占用",mobile);
        }

        if(userModel.isDisabled()){
            return new Result(1003,"账户已锁定");
        }

        String ip = IPUtils.getClientIp(request);
        UserInfoModel updateModel = new UserInfoModel(
                userModel.getId(),null,null,null,null,null,null,mobile,true,null,userModel.isEmailStatus(),
                null,null,null,null,null,userModel.isDisabled(),userModel.getId(),
                DateUtils.getTimestamp(),IPUtils.toLong(ip)
        );

        int result = userInfoService.updateByUserId(updateModel);
        if(result == 1){
            return new Result(userSearchService.findById(userModel.getId()),new Labels.LabelModel(UserInfoModel.class,"base","account"));
        }
        return new Result(0,"更新失败");
    }

    @PutMapping("/email")
    public Result email(HttpServletRequest request, UserInfoModel userModel, @MultiRequestBody @NotBlank @Email String email, @MultiRequestBody @NotBlank String code){
        if(userModel == null) return new Result(403,"未登陆");
        List<DmLogModel> listLog = dmLogService.searchByTemplateAndEmail("replace",email);
        DmLogModel checkLog = null;
        for(DmLogModel logModel : listLog){
            Map<String,String> dataMap = JSONObject.parseObject(logModel.getData(),new TypeReference<Map<String,String>>(){});
            if(StringUtils.isNotEmpty(dataMap.get("code")) && dataMap.get("code").equals(code)){
                checkLog = logModel;
                break;
            }
        }

        if(checkLog == null){
            return new Result(1001,"验证码错误或已过期");
        }

        int resultRows = dmLogService.updateStatus(3,checkLog.getId());
        if(resultRows == 0){
            return new Result(0,"更新验证码状态失败");
        }

        UserInfoModel mobileUser = userSearchService.findByMobile(email);
        if(mobileUser != null || userModel.getEmail() == email){
            return new Result(1002,"邮箱已被占用",email);
        }

        if(userModel.isDisabled()){
            return new Result(1003,"账户已锁定");
        }

        String ip = IPUtils.getClientIp(request);
        UserInfoModel updateModel = new UserInfoModel(
                userModel.getId(),null,null,null,null,null,null,
                null,userModel.isMobileStatus(),email,true,
                null,null,null,null,null,userModel.isDisabled(),userModel.getId(),
                DateUtils.getTimestamp(),IPUtils.toLong(ip)
        );

        int result = userInfoService.updateByUserId(updateModel);
        if(result == 1){
            return new Result(userSearchService.findById(userModel.getId()),new Labels.LabelModel(UserInfoModel.class,"base","account"));
        }
        return new Result(0,"更新失败");
    }

    @PutMapping("/password")
    public Result password(HttpServletRequest request, UserInfoModel userModel, @MultiRequestBody @NotBlank @Password String password, @MultiRequestBody @NotBlank String confirm){
        if(userModel == null) return new Result(403,"未登陆");

        if(!password.equals(confirm))
            return new Result(1001,"两次密码输入不一致");

        if(userModel.isDisabled()){
            return new Result(1002,"账户已锁定");
        }

        String ip = IPUtils.getClientIp(request);
        UserInfoModel updateModel = new UserInfoModel(
                userModel.getId(),null,null,password,null,null,
                null,null,userModel.isMobileStatus(),null,userModel.isEmailStatus(),
                null,null,null,null,null,userModel.isDisabled(),userModel.getId(),
                DateUtils.getTimestamp(),IPUtils.toLong(ip)
        );

        int result = userInfoService.updateByUserId(updateModel);
        if(result == 1){
            return new Result(userSearchService.findById(userModel.getId()),new Labels.LabelModel(UserInfoModel.class,"base","account")).extend("is_set_pwd",true);
        }
        return new Result(0,"更新失败");
    }

    @PutMapping("/path")
    public Result path(UserInfoModel userModel, @MultiRequestBody @NotBlank @Path String path){
        if(userModel == null) return new Result(403,"未登陆");
        path = path.toLowerCase();
        SysPathModel pathModel = pathService.findByTypeAndId(SysPath.USER,userModel.getId());
        boolean isSame = false;
        if(pathModel != null && pathModel.getPath().equals(path)) {
            isSame = true;}
        SysPathModel model = new SysPathModel(path,SysPath.USER,userModel.getId());
        int result = isSame ? 1 : (pathModel == null ? pathService.insert(model) : pathService.updateByTypeAndId(model));
        if(result == 1) return new Result(userSearchService.findById(userModel.getId()),new Labels.LabelModel(UserInfoModel.class,"base","account")).extend("path",path);
        return new Result(0,"更新失败");
    }
}
