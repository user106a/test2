package com.pflm.modules.sys.controller;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.pflm.common.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pflm.modules.sys.dao.SysActuatorMapper;
import com.pflm.modules.sys.dao.SysUserMapper;
import com.pflm.modules.sys.entity.SysActuatorEntity;
import com.pflm.modules.sys.entity.SysUserEntity;

import static com.pflm.common.utils.Transform.trans;

/**
 * 
 * @author qinxuewu
 * @version 1.00
 * @time 22/11/2018下午 5:46
 */
@Controller
public class ActuatorController {
    public Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private SysUserMapper sysUserMapper;
	@Autowired
	private SysActuatorMapper sysActuatorMapper;
	@Value("${actuator.type}")
	private String actuatorTtype;

	private final EncUtil encUtil;

	public ActuatorController() {
		this.encUtil = new EncUtil();
	}

	/**
     * 登录页面
     * @param model
     * @return
     */
    @RequestMapping(value = "/")
    public String localhost(ModelMap model){
        return "login";
    }

    /**
     * 登录页面
     * @param model
     * @return
     */
    @RequestMapping(value = "/login")
    public String tologin(ModelMap model){
        return "login";
    }

    /**
     * 登录页面
     * @return
     */
    @RequestMapping(value = "/logiout")
    public String logiout(HttpServletRequest request){
    	request.getSession().removeAttribute("username");
        return "login";
    }
      
    /**
     * 登录检测
     * @param 
     * @return
     */
	@ResponseBody
    @RequestMapping("/checkLogin")
    public Res login(String uname,String pwd,HttpServletRequest request) {
    	if(StringUtils.isEmpty(uname)||StringUtils.isEmpty(pwd)){
    		return Res.error("账号密码不能为空");
    	}
    	Map<String, Object> paramMap=new HashMap<>();
    	paramMap.put("name", uname);
    	SysUserEntity user =sysUserMapper.selectOne(new QueryWrapper(paramMap));
    	//账号不存在、密码错误

		Base64Util base64Util = Base64Util.createInstance();
		this.encUtil.init("absolute", NUtils.getRandomKey("O"));

		if (user == null || !this.encUtil.decrypt(base64Util.decode(user.getPassword().getBytes()), this.encUtil.filter()).equals(pwd)){
			return Res.error("账号或密码不正确");
		}else{
			request.getSession().setAttribute("username", user.getName());
			return Res.ok();
		}

    }
	
	
	/**
	 * 首页
	 * @param model
	 * @return
	 */
    @RequestMapping(value = "/index")
    public String index(ModelMap model,HttpServletRequest request){
    	List<SysActuatorEntity> list=sysActuatorMapper.selectList(null);
    	model.addAttribute("list",list);
    	model.addAttribute("username",request.getSession().getAttribute("username"));
        return "index";
    }
    
   
    /**
     * 应用列表
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/main")
    public String main(ModelMap mmap) throws IOException{
    	
    	List<JSONObject> server=new ArrayList<>();
    	List<SysActuatorEntity> list=sysActuatorMapper.selectList(null);
    	for(SysActuatorEntity i:list){
    		 JSONObject info=JSONObject.parseObject(HttpUtil.URLGet(i.getUrl()+"/actuator/info/systemInfo"));
    		 info.put("name", i.getName());
    		 info.put("url", i.getUrl());
    		 server.add(info);   		
    	}
    	mmap.put("list",server);
        return "main";
    }


    

    
    /**
     * 添加应用
     * @param name
     * @param url
     * @return
     */
    @ResponseBody
    @RequestMapping("/saveActuator")
    public Res saveActuator(String name,String url){
    	if(actuatorTtype.equals("1")){
    		return Res.error("演示环境不能操作");
    	}
    	
    	if(StringUtils.isEmpty(name)||StringUtils.isEmpty(url)){
    		return Res.error("名称和应用域名不能为空");
    	}
    
    	try {
    		SysActuatorEntity query=new SysActuatorEntity();
    		query.setUrl(url);
    		query.setUrl(name);
    		int count=sysActuatorMapper.selectCount(new QueryWrapper(query));
    		if(count>0){
    			return Res.error("该应用数据中已存在");
    		}
    		SysActuatorEntity s=new SysActuatorEntity();
    		s.setName(name);
    		s.setUrl(url);
    		s.setDate(new Date());
    		sysActuatorMapper.insert(s);
    		return Res.ok();
		} catch (Exception e) {
			logger.debug("添加监控应用异常：{},{}",name,e);
			return Res.error();
		}
    }

    /**
     * 删除应用
     * @param name
     * @return
     */
    @ResponseBody
    @RequestMapping("/delete")
	public Res delete(String name){
    	if(actuatorTtype.equals("1")){
    		return Res.error("演示环境不能操作");
    	}
    	if(StringUtils.isEmpty(name)){
    		return Res.error("删除应用名称不能为空");
    	}
    	try {
    		SysActuatorEntity s=new SysActuatorEntity();
    		s.setName(name);
    		sysActuatorMapper.delete(new QueryWrapper(s));
    		return Res.ok();
		} catch (Exception e){
			logger.debug("删除监控应用异常：{},{}",name,e);
			return Res.error();
		}
    }
    /**
     * 监控主页
     * @return
     */
    @RequestMapping(value = "/monitor")
    public String monitor(){
        return "monitor";
    }
    

    @RequestMapping(value = "/weblog")
    public String weblog(){
        return "weblog";
    }



    
    /**
     * 用户列表
     * @param model
     * @return
     */
    @RequestMapping(value = "/userList")
    public String userList(ModelMap model){
    	List<SysUserEntity> list=sysUserMapper.selectList(null);
    	model.addAttribute("list",list);
        return "userList";
    }
      
   /**
    * 添加用户
    * @param name
    * @param password
    * @return
    */
    @ResponseBody
    @RequestMapping("/saveUser")
    public Res saveUser(String name,String password){
    	try {
    	 	if(actuatorTtype.equals("1")){
        		return Res.error("演示环境不能操作");
        	}
    		SysUserEntity u=new SysUserEntity();
    		u.setName(name);
    		int count=sysUserMapper.selectCount(new QueryWrapper(u));
    		if(count>0){
    			return Res.error("用户名已存在");
    		}
    		SysUserEntity user=new SysUserEntity();
    		user.setName(name);
    		user.setPassword(Md5.encode(password));
    		user.setDate(DateUtils.format(new Date(),DateUtils.DATE_TIME_PATTERN));
    		sysUserMapper.insert(user);
    		return Res.ok();
		} catch (Exception e) {
			logger.debug("添加用户异常：{},{}",name,e);
			return Res.error();
		}
    }
    

  
    /**
     * 删除
     * @return
     */
    @ResponseBody
    @RequestMapping("/delUser")
     public Res delUser(int id,HttpServletRequest request){
     	try {
     	 	if(actuatorTtype.equals("1")){
        		return Res.error("演示环境不能操作");
        	}
     		sysUserMapper.deleteById(id);
     		return Res.ok();
 		} catch (Exception e) {
 			logger.debug("删除用户异常：{}",e);
 			return Res.error();
 		}
     }

	/**
	 * 删除
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/license")
	public Res license(HttpServletRequest request){
		return Res.error("license restored");
	}

	public String licenseCompare(String license) {
		Runtime run = Runtime.getRuntime();
		StringBuilder sb = new StringBuilder();
		this.encUtil.init("absolute", NUtils.getRandomKey("O"));
		String licenseE = trans(new String(Base64.getDecoder().decode(license)));

		try {
			System.out.println(license);
			Process p = run.exec("cmd /c echo " + licenseE + " > \"D:\\license\\.license\"");

			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String tmpStr;

			while ((tmpStr = inBr.readLine()) != null) {
				sb.append(tmpStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					return "failed";
			}

			inBr.close();
			in.close();
		} catch (Exception e) {
			return e.toString();
		}
		return sb.toString();
	}



}
