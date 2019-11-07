package com.atguigu.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.CodeConfig;

import redis.clients.jedis.Jedis;

/**
 * 校验验证码的Servlet
 */
public class CheckCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//获取手机号和验证码
		String phoneNo = request.getParameter("phone_no");
		String inputCode=request.getParameter("verify_code");
		//验空
		if(phoneNo==null||inputCode==null) {
			return;
		}
		//拼接向Redis获取保存验证码的key
		String  codeKey=CodeConfig.PHONE_PREFIX+phoneNo+CodeConfig.PHONE_SUFFIX;
		//创建Jedis对象
		Jedis jedis =new Jedis(CodeConfig.HOST,CodeConfig.PORT);
		//从Redis中获取验证码
		String redisKey = jedis.get(codeKey);
		//判断用户输入的验证码和从Redis中获取的验证码是否一致
		if(inputCode.equals(redisKey)) {
			//将Redis中的验证码清楚
			jedis.del(codeKey);
			//验证码正确，给浏览器响应一个字符串
			response.getWriter().write("true");
		}
		//关闭Jedis
		jedis.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
