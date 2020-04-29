package com.itellyou.api.controller.oauth;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.itellyou.api.handler.response.Result;
import com.itellyou.model.ali.SmsLogModel;
import com.itellyou.model.user.*;
import com.itellyou.service.ali.AlipayService;
import com.itellyou.service.ali.SmsLogService;
import com.itellyou.service.github.GithubService;
import com.itellyou.service.user.*;
import com.itellyou.util.DateUtils;
import com.itellyou.util.IPUtils;
import com.itellyou.util.StringUtils;
import com.itellyou.util.annotation.MultiRequestBody;
import com.itellyou.util.validation.Mobile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/oauth/github")
public class GithubOAuthController {

    private final UserThirdAccountService accountService;
    private final UserThirdLogService logService;
    private final UserLoginService loginService;
    private final AlipayService alipayService;
    private final GithubService githubService;
    private final SmsLogService smsLogService;
    private final UserSearchService userSearchService;
    private final UserRegisterService userRegisterService;
    private final static String GITHUB_USER_SESSION_KEY = "github_user";

    public GithubOAuthController(UserThirdAccountService accountService, UserThirdLogService logService, UserLoginService loginService, AlipayService alipayService, GithubService githubService, SmsLogService smsLogService, UserSearchService userSearchService, UserRegisterService userRegisterService) {
        this.accountService = accountService;
        this.logService = logService;
        this.loginService = loginService;
        this.alipayService = alipayService;
        this.githubService = githubService;
        this.smsLogService = smsLogService;
        this.userSearchService = userSearchService;
        this.userRegisterService = userRegisterService;
    }

    @GetMapping("")
    public Result github(HttpServletRequest request,HttpServletResponse response,UserInfoModel userModel, @RequestParam String action){
        try {
            String referer = request.getHeader("Referer");
            UserThirdAccountAction accountAction = UserThirdAccountAction.valueOf(action.toUpperCase());
            if(userModel == null && accountAction.equals(UserThirdAccountAction.BIND)){
                return new Result(401,"未登录");
            }
            Long userId = userModel == null ? 0l : userModel.getId();
            String url = accountService.oauthGithubURL(userId,accountAction,referer,IPUtils.toLong(IPUtils.getClientIp(request)));
            if(accountAction.equals(UserThirdAccountAction.LOGIN)){
                response.sendRedirect(url);
            }
            return new Result(url);
        }catch (Exception e){
            return new Result(0,e.getLocalizedMessage());
        }
    }

    @GetMapping("/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response, HttpSession session, @RequestParam(name = "code") String authCode, @RequestParam String state) throws IOException {
        try {
            UserThirdLogModel logModel = logService.find(state);
            if(logModel == null || logModel.isVerify()) {
                throw new Exception("错误的 state");
            }
            int result = logService.updateVerify(true,logModel.getId());
            if(result != 1) throw new Exception("验证异常");
            Long ip = IPUtils.toLong(IPUtils.getClientIp(request));
            if(logModel.getAction().equals(UserThirdAccountAction.BIND)){
                result = accountService.bindGithub(logModel.getCreatedUserId(),authCode,ip);
                if(result != 1) throw new Exception("绑定失败");
            }else if (logModel.getAction().equals(UserThirdAccountAction.LOGIN)){
                String accessToken = githubService.getOAuthToken(authCode);
                if(StringUtils.isEmpty(accessToken)){
                    throw new Exception("验证失败");
                }
                JSONObject jsonObject = githubService.getUserInfo(accessToken);
                if(jsonObject == null || StringUtils.isEmpty(jsonObject.getString("id"))) throw new Exception("验证失败");
                String githubUserId = jsonObject.getString("id");
                URL redirectUrl = new URL(logModel.getRedirectUri());
                UserThirdAccountModel accountModel = accountService.searchByTypeAndKey(UserThirdAccountType.GITHUB,githubUserId);
                if(accountModel == null){
                    // 注册
                    Map<String , String > userMap = new HashMap<>();
                    userMap.put("key",githubUserId);
                    userMap.put("name",jsonObject.getString("login"));
                    userMap.put("avatar",jsonObject.getString("avatar_url"));
                    userMap.put("home",jsonObject.getString("html_url"));
                    userMap.put("star",jsonObject.getString("followers"));
                    session.setAttribute(GITHUB_USER_SESSION_KEY,userMap);
                    StringBuilder urlBuilder = new StringBuilder(redirectUrl.getProtocol()).append("://").append(redirectUrl.getAuthority()).append("/login/oauth?type=github");
                    logModel.setRedirectUri(urlBuilder.toString());
                }else {
                    String token = loginService.createToken(accountModel.getUserId(),ip);
                    loginService.sendToken(response,token);

                    StringBuilder urlBuilder = new StringBuilder(redirectUrl.getProtocol()).append("://").append(redirectUrl.getAuthority()).append("/dashboard");
                    logModel.setRedirectUri(urlBuilder.toString());
                }
            }
            String uri = logModel.getRedirectUri();
            URL url = new URL(uri);

            if(!new HashSet<String>(){{ add("www.itellyou.com");add("localhost");}}.contains(url.getHost()))
                uri = "https://www.itellyou.com";
            response.sendRedirect(uri);
        }catch (Exception e){
            e.printStackTrace();
            response.sendRedirect("https://www.itellyou.com/500");
        }
    }

    @PostMapping("/login")
    public Result login(HttpServletRequest request, HttpServletResponse response, HttpSession session, @MultiRequestBody @NotBlank @Mobile String mobile, @MultiRequestBody @NotBlank String code){

        List<SmsLogModel> listLog = smsLogService.searchByTemplateAndMobile("verify",mobile);
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
        Object sessionData = session.getAttribute(GITHUB_USER_SESSION_KEY);
        if(sessionData == null) return new Result(500,"认证错误，请返回重试");
        Map<String , String> userMap = (Map<String , String>)sessionData;

        String clientIp = IPUtils.getClientIp(request);
        UserInfoModel userInfoModel = userSearchService.findByMobile(mobile);
        Long userId = userInfoModel == null ? null : userInfoModel.getId();
        String nickName = userInfoModel == null ? null : userInfoModel.getName();
        String name = userMap.get("name").trim();
        if(userId == null){
            nickName = name;
            if(StringUtils.isEmpty(nickName)){
                nickName = "u_" + StringUtils.randomString(10);
            }
            UserInfoModel nameModel = userSearchService.findByName(nickName);
            if(nameModel != null){
                nickName = name + "_" + StringUtils.randomString(10);
            }
            userId = userRegisterService.mobile(nickName,null,mobile,clientIp);
        }
        UserThirdAccountModel accountModel = new UserThirdAccountModel();
        accountModel.setUserId(userId);
        accountModel.setCreatedTime(DateUtils.getTimestamp());
        accountModel.setCreatedIp(IPUtils.toLong(clientIp));
        accountModel.setKey(userMap.get("key"));
        accountModel.setType(UserThirdAccountType.GITHUB);
        accountModel.setName(StringUtils.isEmpty(name) ? nickName : name);
        accountModel.setAvatar(userMap.get("avatar"));
        accountModel.setHome(userMap.get("home"));
        accountModel.setStar(Long.valueOf(userMap.getOrDefault("star","0")));
        try {
            accountService.insert(accountModel);
        }catch (Exception e){
            return new Result(500,"绑定失败，请重试");
        }
        if(userInfoModel != null && userInfoModel.isDisabled()){
            return new Result(1003,"账户已锁定");
        }
        String token = loginService.createToken(userId,IPUtils.toLong(clientIp));
        if(StringUtils.isEmpty(token)){
            return new Result(1004,"登录出错啦");
        }
        loginService.sendToken(response,token);
        session.removeAttribute(GITHUB_USER_SESSION_KEY);
        return new Result(userInfoModel,"base");
    }
}
