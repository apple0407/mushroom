package org.marker.mushroom.dao.impl;

import org.marker.mushroom.alias.DAO;
import org.marker.mushroom.beans.Article;
import org.marker.mushroom.dao.DaoEngine;
import org.marker.mushroom.dao.DoctorDao;
import org.marker.mushroom.dao.IArticleDao;
import org.springframework.stereotype.Repository;


@Repository(DAO.DOCTOR)
public class DoctorDaoImpl extends DaoEngine implements DoctorDao {


    @Override
    public boolean updateStatus(Integer id, int status) {
        String prefix = getPreFix();//获取数据库表前缀
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(prefix).append("doctor")
                .append(" set status=?,updateTime=sysdate() where id=?");
        int a = jdbcTemplate.update(sql.toString(), status , id );
        return a>0? true : false;

    }
}
