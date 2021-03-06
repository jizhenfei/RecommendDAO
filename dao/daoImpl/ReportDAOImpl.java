package cn.edu.bjtu.weibo.daoimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import cn.edu.bjtu.weibo.dao.ReportDAO;
import pl.quaternion.SentinelBasedJedisPoolWrapper;
import redis.clients.jedis.Jedis;
@Repository("reportDAO")
public class ReportDAOImpl implements ReportDAO {

	int XxCommentmeMessageNumber = 0;
	final Set<String> sentinels = new HashSet<String>();
	final Config config = new Config();
	config.testOnReturn = true;
	config.testOnBorrow = true;
        sentinels.add("192.168.146.129:26379");
	SentinelBasedJedisPoolWrapper pool = new SentinelBasedJedisPoolWrapper(config, 90000, null, 0, "mymaster", sentinels);

	Jedis client = pool.getResource();

	@Override
	public boolean ReportUser(String UserId, String reportedfromuserId) {
		if (client.exists("User:" + UserId + ":id") && client.exists("User:" + reportedfromuserId + ":id")) {
			client.lpush("Report:User:" + UserId + ":reporterid-list", reportedfromuserId);
			return true;
		}
		return false;
	}

	@Override
	public boolean ReportWeibo(String WeiboId, String reportedfromuserId) {
		if (client.exists("Weibo:" + WeiboId + ":WeiboId") && client.exists("User:" + reportedfromuserId + ":id")) {
			client.lpush("Report:Weibo:" + WeiboId + ":reporterid-list", reportedfromuserId);
			return true;
		}
		return false;
	}

	@Override
	public int ReportUserNumber(String ReportUserId) {
		String key = "Report:User:" + ReportUserId + ":reporterid-list";
		List<String> list = client.lrange(key, 0, -1);
		return list.size();
	}

	@Override
	public int ReportWeiboNumber(String ReportWeiboId) {
		String key = "Report:Weibo:" + ReportWeiboId + ":reporterid-list";
		List<String> list = client.lrange(key, 0, -1);
		return list.size();
	}

	@Override
	public List<String> ReportedUserfromuserList(String UserId, int pageIndex, int pagePerNumber) {
		String key = "Report:User:" + UserId + ":reporterid-list";
		List<String> list = client.lrange(key, 0, -1);
		List<String> subList = list.subList((pageIndex - 1) * pagePerNumber, pageIndex * pagePerNumber);
		return subList;
	}

	@Override
	public List<String> ReportedWeibofromuserList(String WeiboId, int pageIndex, int pagePerNumber) {
		String key = "Report:Weibo:" + WeiboId + ":reporterid-list";
		List<String> list = client.lrange(key, 0, -1);
		List<String> subList = list.subList((pageIndex - 1) * pagePerNumber, pageIndex * pagePerNumber);
		return subList;
	}

	@Override
	public List<String> ReportUserList(int pageIndex, int pagePerNumber) {
		List<String> list = new ArrayList<String>();
		Set keys = client.keys("Report:User:*:reporterid-list");
		Iterator t1 = keys.iterator();
		while (t1.hasNext()) {
			String obj1 = (String) t1.next();
			list.add(obj1);
		}
		List<String> subList = list.subList((pageIndex - 1) * pagePerNumber, pageIndex * pagePerNumber);
		return subList;
	}

	@Override
	public List<String> ReportWeiboList(int pageIndex, int pagePerNumber) {
		List<String> list = new ArrayList<String>();
		Set keys = client.keys("Report:Weibo:*:reporterid-list");
		Iterator t1 = keys.iterator();
		while (t1.hasNext()) {
			String obj1 = (String) t1.next();
			list.add(obj1);
		}
		List<String> subList = list.subList((pageIndex - 1) * pagePerNumber, pageIndex * pagePerNumber);
		return subList;
	}

	@Override
	public boolean ReportComment(String userId, String commentId) {
		String key = "Report:Comment:"+commentId+":reporter-list";
		if (client.exists("User:" + userId + ":id") && client.exists("Comment:" + commentId + ":Commentid")) {
			client.lpush(key, userId);
			return true;
		}
		return false;
	}
	



}
