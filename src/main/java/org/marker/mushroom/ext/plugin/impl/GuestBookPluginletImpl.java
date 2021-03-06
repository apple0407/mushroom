package org.marker.mushroom.ext.plugin.impl;

/**
 *
 *
 *
 * Created by marker on 17/5/19.
 */
import com.alibaba.fastjson.JSON;
import org.marker.app.domain.MessageResult;
import org.marker.mushroom.beans.GuestBook;
import org.marker.mushroom.core.SystemStatic;
import org.marker.mushroom.core.config.impl.DataBaseConfig;
import org.marker.mushroom.core.config.impl.SystemConfig;
import org.marker.mushroom.dao.ISupportDao;
import javax.servlet.http.HttpServletRequest;
import org.marker.mushroom.holder.SpringContextHolder;

import java.io.IOException;
import java.io.Writer;
import java.lang.Integer;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.marker.mushroom.ext.plugin.Pluginlet;
import org.marker.mushroom.alias.DAO;



/**
 * 留言插件
 *
 */
public class GuestBookPluginletImpl extends Pluginlet {
    public GuestBookPluginletImpl(){
        this._config = new HashMap<>();
        this._config.put("module","1316608071154b0fa1da3d34ee97037a");
        this._config.put("name", "留言插件");
        this._config.put("author","marker");
        this._config.put("type", "guestbook");// 模型标识
        this._config.put("description", "常规插件");


        // 路由配置
        this.routers = new HashMap<>();
        this.routers.put(" get:/list", "list");
        this.routers.put("post:/add" , "submit");
        this.routers.put("post:/delete" , "delete");

    }



    /**
     * 显示留言列表
     */
    public Object list(){
        HttpServletRequest request = getServletRequest();
        int currentPageNo = 1;
        try{
            currentPageNo = Integer.parseInt(request.getParameter("currentPageNo"));
        }catch(Exception e){ }
        ISupportDao dao = SpringContextHolder.getBean(DAO.COMMON);

        DataBaseConfig dbcfg = DataBaseConfig.getInstance();

        String sql = "select * from "+ dbcfg.getPrefix()  +"guestbook order by id desc";
        request.setAttribute("page",  dao.findByPage(currentPageNo, 5, sql));
        return "list.html";
    }




    /**
     * 显示留言列表
     */
    public void delete(){
        HttpServletRequest request = getServletRequest();
        HttpServletResponse response = getServletResponse();

        String rid = request.getParameter("rid");
        ISupportDao dao = SpringContextHolder.getBean(DAO.COMMON);
        dao.deleteByIds(GuestBook.class, rid);

        try {
            Writer out = response.getWriter();
            out.write(JSON.toJSONString(MessageResult.success()));
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * 提交
     */
    public void submit(){
        HttpServletRequest request = getServletRequest();
        HttpServletResponse response = getServletResponse();

        String code = request.getParameter("authcode");// 验证码
        String randauthcode = (String) request.getSession().getAttribute("randauthcode");
        String ip  = request.getRemoteHost();
        String nicknamea = request.getParameter("nickname");
        String content  = request.getParameter("content");
        String email    = request.getParameter("email");


        DataBaseConfig dbcfg = DataBaseConfig.getInstance();


        String msg = "thanks for you";
        if(randauthcode != null && randauthcode.toLowerCase().equals(code.toLowerCase())){
            String sql = "insert into "+dbcfg.getPrefix()+"guestbook(nickname,ip,content,time,email) values(?,?,?,sysdate(),?)";

            Object[] strs = new Object[]{nicknamea,ip,content,email};
            if(!commonDao.update(sql, strs)){
                msg = "亲，您留言数据无效，已经进行数据回滚";
            }
        }else{
            msg = "亲，验证码错了哈";
        }
        try {
            Writer out = response.getWriter();
            out.write("{\"status\":\"true\",\"message\":\""+msg+"\"}");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
