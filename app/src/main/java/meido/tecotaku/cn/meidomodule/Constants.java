package meido.tecotaku.cn.meidomodule;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/10/21 0021.
 */
public class Constants {
    public static HashMap <String, String> ts = new HashMap<>();
    public static HashMap <String, BaseAction> action = new HashMap<>();
    static {
        ts.put("你是", "我是Sino制造出来的女朋友（划掉）人工智能 Meido酱，所说的话都由脸滚键盘随机产生 0-0 不代表任何人的立场 /笑 ");
        String my = "Sino是世界上最伟大的技术宅，如果有人敢说坏话的话就往你电脑里送病毒哦 /笑";
        ts.put("主人", my);
        ts.put("sino", my);
        ts.put("Sino", my);
        ts.put("么", "没有，下一个");
        ts.put("吗", "没有，滚");
        ts.put("戴凌磊", "傻逼");
        new BaseAction(){
            @Override
            String doAction(String a) {
                return "";
            }
        };
    }
}
