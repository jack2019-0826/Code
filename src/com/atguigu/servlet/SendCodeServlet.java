package com.atguigu.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.CodeConfig;

import redis.clients.jedis.Jedis;

/**
 * 发送验证码的Servlet
 */
public class SendCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("resource")
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			
			//获取用户输入的手机号
			String phoneNo=request.getParameter("phone_no");
			//验空
			if(phoneNo==null) {
				return;
			}
			Jedis jedis = new Jedis(CodeConfig.HOST,CodeConfig.PORT);
			//拼接Redis中保存计数器的key
			String countKey=CodeConfig.PHONE_PREFIX+phoneNo+CodeConfig.COUNT_SUFFIX;
			//获取Redis中计数器count值
			String count=jedis.get(countKey);
			if(count==null) {
				//证明今天还没有发送过验证码，设置Redis中计数器的值为1
				jedis.setex(countKey, CodeConfig.SECONDS_PER_DAY, "1");
			}else if("3".equals(count)) {
				//证明今天已经发送过3次验证码，给浏览器响应一个字符串limit
				response.getWriter().write("limit");
				jedis.close();
				return;
			}else {
				//今日发送验证码的次数没有达到三次
				jedis.incr(countKey);
			}
			//拼接向Redis保存验证码的key
			String codeKey=CodeConfig.PHONE_PREFIX+phoneNo+CodeConfig.PHONE_SUFFIX;
			//获取验证码
			String code = getCode(CodeConfig.CODE_LEN);
			System.out.println(code);
			//将验证码保存到Redis中，并设置它的有效时间是120秒
			jedis.setex(codeKey, CodeConfig.CODE_TIMEOUT, code);
			//给浏览器响应一个字符串true
			response.getWriter().write("true");
			//关闭Jedis
			jedis.close();
		    
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	// 随机生成验证码的方法
	private String getCode(int len) {
		String code = "";
		for (int i = 0; i < len; i++) {
			int rand = new Random().nextInt(10);
			code += rand;
		}
		return code;
	}

}
