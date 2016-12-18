package cn.edu.gdmec.s07150804.myweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.GestureDetector;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class GetWeatherInfoTask extends AsyncTask<String,Void,List<Map<String,Object>>> {
   private Activity context;
    private ProgressDialog progressDialog;
    private String errorMsg="网络错误！！";
    private ListView weather_info;
    private static String BASE_URL="http://v.juhe.cn/weather/index/format=2&cityname=''";
    private static String key="&key=f4e43074042d6c4889cb3cbda16bb209";
public GetWeatherInfoTask(Activity context){
    this.context=context;
    progressDialog=new ProgressDialog(context);
    progressDialog.setMessage("正在获取天气，请稍后。。。");
    progressDialog.setCancelable(false);

}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(List<Map<String, Object>> result) {
        super.onPostExecute(result);
        progressDialog.dismiss();
        if (result.size()>0){
            weather_info=(ListView)context.findViewById( R.id.weather_info);
            SimpleAdapter simpleAdapter=new SimpleAdapter(context,result,R.layout.weather_item,
                    new String[]{"temperature","weather","date","week","weather_icon"},
                    new int[]{R.id.temperature,R.id.weather,R.id.date,R.id.week,R.id.weather_icon});
            weather_info.setAdapter(simpleAdapter);
        }else{
            Toast.makeText(context,errorMsg,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected List<Map<String, Object>> doInBackground(String... params) {
        List<Map<String ,Object>> list=new ArrayList<Map<String, Object>>();
        try {
            HttpClient httpClient=new DefaultHttpClient();
            String url=BASE_URL+ URLEncoder.encode(params[0],"UTF-8")+key;
            HttpGet httpGet=new HttpGet(url);
            HttpResponse response=httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode()==200){
                String jsonString= EntityUtils.toString(response.getEntity(),"UTF-8");
                JSONObject jsonObdata=new JSONObject(jsonString);
                if (jsonObdata.getInt("resultcode")==200){
                    JSONObject result=jsonObdata.getJSONObject("result");
                    JSONArray weatherList=result.getJSONArray("future");
                    for (int i=0;i<7;i++){
                        Map<String ,Object> item=new HashMap<String ,Object>();
                        JSONObject weathObject=weatherList.getJSONObject(i);
                        item.put("temperature",weathObject.getString("temperature"));
                        item.put("weather",weathObject.getString("weather"));
                        item.put("date",weathObject.getString("date"));
                        item.put("week",weathObject.getString("week"));
                        item.put("wind",weathObject.getString("wind"));
                        JSONObject wid=weathObject.getJSONObject("weather_id");
                        int weather_icon=wid.getInt("fa");
                        item.put("weather_icon",WeathIcon.weather_icons[weather_icon]);
                        list.add(item);
                    }
                }else {
                    errorMsg="本应用暂时不支持你所请求的城市！！";
                }

            }else {
                errorMsg="网络错误！！";
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
