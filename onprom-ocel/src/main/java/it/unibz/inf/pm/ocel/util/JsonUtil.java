package it.unibz.inf.pm.ocel.util;

import com.alibaba.fastjson.*;
import com.alibaba.fastjson.serializer.SerializerFeature;
import it.unibz.inf.pm.ocel.entity.OcelEvent;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    // save the Nodes into json file
    public static void saveJson(Object jsonDiagram,String filePath){
        String writeString = JSON.toJSONString(jsonDiagram, SerializerFeature.PrettyFormat);

        LOGGER.info(writeString);
        BufferedWriter writer = null;
        File file = new File(filePath);
        if (!file.exists()){
            try {
                file.createNewFile();
            }catch (IOException e){
                LOGGER.error(e.getMessage());
            }
        }
        //before writing, set the file empty
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false),"UTF-8"));
            writer.write("");
            writer.write(writeString);
        }catch (IOException e){
            LOGGER.error(e.getMessage());
        }finally {
            try{
                if (writer != null){
                    writer.close();
                }
            }catch (IOException e){
                LOGGER.error(e.getMessage());
            }
        }
    }

    // read the JSON file, return the string of JSONObject
    public static String readJsonFile(String filePath){
        BufferedReader reader = null;
        String readJson = "";
        JSONObject jsonObject = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null){
                readJson += tempString;
            }
        }catch (IOException e){
            LOGGER.error(e.getMessage());
        }finally {
            if (reader != null){
                try {
                    reader.close();
                }catch (IOException e){
                    LOGGER.error(e.getMessage());
                }
            }
        }

        // gain the jsonObject
        try {
            jsonObject = JSONObject.parseObject(readJson);
           // System.out.println(JSON.toJSONString(jsonObject));
        }catch (JSONException e){
            LOGGER.error(e.getMessage());
        }
        return JSON.toJSONString(jsonObject);
    }

    public static OcelEvent getEvent(String jsonstring, Class cls) {
        OcelEvent object = null;
        try {
            object = (OcelEvent) JSON.parseObject(jsonstring, cls);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return object;
    }

    public static List getEventList(String jsonstring, Class cls) {
        List list = new ArrayList();
        try {
            list = JSON.parseArray(jsonstring, cls);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return list;
    }

    public static List<OcelEvent> getEventListMap(String jsonstring) {
        List<OcelEvent> list = new ArrayList<>();
        try {
            list = JSON.parseObject(jsonstring,
                    new TypeReference<OcelEvent>() {
            }.getType());
        } catch (Exception e) {
           e.printStackTrace();
        }
        return list;
    }

    public static Map<String, List<KeyValue>> map = new ConcurrentHashMap<>(28);


    /**
     * get all values by using key
     * @param key
     * @return
     * @throws IOException
     */
    public static List<Object> getValuesForKey(String key) throws IOException {
        List<KeyValue> list = getKeyValues(key);
        if (CollectionUtils.isEmpty(list)) {return null;}
        List<Object> valueStrings = list.stream().map(s -> s.getValue()).collect(Collectors.toList());
        return valueStrings;
    }

    /**
     * get all values by using key and id
     * @return
     */
    public static Object getValueForKeyAndId(String key,String id) throws IOException {
        List<KeyValue> list = getKeyValues(key);

        if (CollectionUtils.isEmpty(list)) {return null;}

        for (KeyValue keyValue : list) {
            if (keyValue.getValue() != null) {
                return keyValue.getValue();
            }
        }
        return null;

    }

    public static List<KeyValue> getKeyValues(String key) throws IOException {
        if (StringUtils.isEmpty(key)){return null;}

        if (CollectionUtils.isEmpty(map)) {
            readJsonData();
        }
        List<KeyValue> list = map.get(key);
        return list;
    }

    /**
     * read json file and convert it to JSONObject
     * @throws IOException
     */
    public static void readJsonData() throws IOException {
        File file = new File("ocel/logs/minimal.jsonocel");
        String jsonString = FileUtils.readFileToString(file);

        JSONObject jsonObject = JSONObject.parseObject(jsonString);

        Set<String> keySet = jsonObject.keySet();
        for (String s : keySet) {
            String stringArray = jsonObject.getJSONArray(s).toJSONString();
            List<KeyValue> keyValues = JSONArray.parseArray(stringArray, KeyValue.class);
            map.put(s, keyValues);
        }
    }

    public static JSONObject readJsonfileToObject(String filePath) throws IOException {
        File file = new File(filePath);
        String jsonString = FileUtils.readFileToString(file);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        return jsonObject;
    }

    @Data
    static class KeyValue{
        private String key;
        private String value;
    }

}
