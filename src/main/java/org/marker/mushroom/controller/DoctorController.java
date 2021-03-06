package org.marker.mushroom.controller;

import org.marker.app.common.SessionAttr;
import org.marker.mushroom.beans.Doctor;
import org.marker.mushroom.beans.Page;
import org.marker.mushroom.beans.ResultMessage;
import org.marker.mushroom.core.proxy.SingletonProxyFrontURLRewrite;
import org.marker.mushroom.dao.DoctorDao;
import org.marker.mushroom.dao.IArticleDao;
import org.marker.mushroom.dao.IChannelDao;
import org.marker.mushroom.service.impl.ArticleService;
import org.marker.mushroom.service.impl.CategoryService;
import org.marker.mushroom.service.impl.ChannelService;
import org.marker.mushroom.service.impl.DoctorService;
import org.marker.mushroom.support.SupportController;
import org.marker.mushroom.utils.HttpUtils;
import org.marker.urlrewrite.URLRewriteEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 医生管理
 *
 * @author marker
 * */
@Controller
@RequestMapping("/admin/doctor")
public class DoctorController extends SupportController {

    public static final int DEPT_PID = 15;

	// 文章Dao
	@Autowired
    DoctorDao doctorDao;

	@Autowired
    DoctorService doctorService;

	@Autowired
	ChannelService channelService;

	@Autowired CategoryService categoryService;

	@Autowired
	IChannelDao channelDao;

	public DoctorController() {
		this.viewPath = "/admin/doctor/";
	}

	// 添加医生
	@RequestMapping("/add")
	public ModelAndView add(HttpSession session){
		ModelAndView view = new ModelAndView(this.viewPath+"add");
        view.addObject("channels", channelDao.findAll());

		int userGroupId = (int)session.getAttribute(SessionAttr.USER_GROUP_ID);
//		view.addObject("channels", channelService.getUserGroupChannel(userGroupId));
		view.addObject("categorys", categoryService.getUserGroupCategory(userGroupId));


		return view;
	}
	
	// 编辑医生
	@RequestMapping("/edit")
	public ModelAndView edit(@RequestParam("id") int id, HttpSession session){
		ModelAndView view = new ModelAndView(this.viewPath+"edit");
		view.addObject("doctor", commonDao.findById(Doctor.class, id));
        int userGroupId = (int)session.getAttribute(SessionAttr.USER_GROUP_ID);
//        view.addObject("channels", channelService.getUserGroupChannel(userGroupId));
        view.addObject("categorys", categoryService.getUserGroupCategory(userGroupId));

        return view;
	}
	
	
	/**
	 * 持久化文章操作
	 * @param article
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/save")
	public Object save(Doctor article, @RequestParam("cid") int cid){
		article.setTime(new Date());
		article.setCid(cid);// 这里是因为不能注入bean里
		
		String msg = "";
		if(article.getStatus() == 1){
			msg = "发布";
		}else{
			msg = "保存草稿";
		}
		if(commonDao.save(article)){ 
			return new ResultMessage(true,  msg + "成功!"); 
		}else{
			return new ResultMessage(false, msg + "失败!"); 
		}
	}
	

	// 保存
	@ResponseBody
	@RequestMapping("/update")
	public Object update(@ModelAttribute("entity") Doctor doctor){

		if(doctorDao.update(doctor)){
			return new ResultMessage(true, "更新成功!");
		}else{
			return new ResultMessage(false,"更新失败!"); 
		}
	}
	
	
	
	//删除文章
	@ResponseBody
	@RequestMapping("/delete")
	public Object delete(@RequestParam("rid") String rid){
		boolean status = commonDao.deleteByIds(Doctor.class, rid);
		if(status){
			return new ResultMessage(true,"删除成功!");
		}else{
			return new ResultMessage(false,"删除失败!"); 
		}
	}



    // 审核文章
    @ResponseBody
    @RequestMapping("/audit")
    public Object audit(@RequestParam("id") Integer id){
        boolean status = doctorDao.updateStatus(id, 1);
        if(status){
            return new ResultMessage(true,"审核成功!");
        }else{
            return new ResultMessage(false,"审核删除!");
        }
    }
	
	// 发布文章
	@RequestMapping("/list")
	public ModelAndView listview(HttpSession session){
		ModelAndView view = new ModelAndView(this.viewPath+"list");
        int userGroupId = (int)session.getAttribute(SessionAttr.USER_GROUP_ID);
        view.addObject("categorys", categoryService.getUserGroupCategory(userGroupId));
		return view;
	}
	
	
	/**
	 * 文章列表接口(REST)
	 * @param currentPageNo
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody 
	public Object list(HttpServletRequest request, ModelMap model,
			@RequestParam("currentPageNo") int currentPageNo,
			@RequestParam("cid") int cid,
			@RequestParam("status") String status,
			@RequestParam("keyword") String keyword,
			@RequestParam("pageSize") int pageSize,
					   HttpSession session
		 
			){
		Map<String,Object> params = new HashMap<String,Object>(4);
		params.put("cid", cid);
		params.put("status", status);
		params.put("keyword", keyword);
		int userGroupId = (int)session.getAttribute(SessionAttr.USER_GROUP_ID);
		params.put("userGroupId", userGroupId);



		Page page = doctorService.find(currentPageNo, pageSize, params);
		
		URLRewriteEngine urlRewrite = SingletonProxyFrontURLRewrite.getInstance();
		
		String url = HttpUtils.getRequestURL(request);
		// 遍历URL重写
		Iterator<Map<String, Object>> it = page.getData().iterator();
		while(it.hasNext()){
			Map<String,Object> data = it.next();
			data.put("url", url + urlRewrite.encoder(data.get("url").toString())); 
		}
		return page;
	}



}
