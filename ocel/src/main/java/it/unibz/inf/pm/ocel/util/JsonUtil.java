/*
 * ocel
 *
 * JsonUtil.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.pm.ocel.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import it.unibz.ocel.model.OcelEvent;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    public static Map<String, List<KeyValue>> map = new ConcurrentHashMap<>(28);

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
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
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
        StringBuilder readJson = new StringBuilder();
        JSONObject jsonObject = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null){
                readJson.append(tempString);
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
            jsonObject = JSONObject.parseObject(readJson.toString());
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

    /**
     * get all values by using key
     * @param key
     * @return
     * @throws IOException
     */
    public static List<Object> getValuesForKey(String key) throws IOException {
        List<KeyValue> list = getKeyValues(key);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(KeyValue::getValue).collect(Collectors.toList());
    }

    /**
     * get all values by using key and id
     * @return
     */
    public static Object getValueForKeyAndId(String key,String id) throws IOException {
        List<KeyValue> list = getKeyValues(key);

        if (list == null || list.isEmpty()) {
            return null;
        }

        for (KeyValue keyValue : list) {
            if (keyValue.getValue() != null) {
                return keyValue.getValue();
            }
        }
        return null;

    }

    public static List<KeyValue> getKeyValues(String key) throws IOException {
        if (key.isEmpty()) {
            return null;
        }

        if (map.isEmpty()) {
            readJsonData("ocel/logs/minimal.jsonocel");
        }
        return map.get(key);
    }

    /**
     * read json file and convert it to JSONObject
     * @throws IOException
     */
    public static void readJsonData(String filepath) throws IOException {
        File file = new File(filepath);
        String jsonString = FileUtils.readFileToString(file, "UTF-8");

        JSONObject jsonObject = JSONObject.parseObject(jsonString);

//        Set<String> keySet = jsonObject.keySet();
//        for (String s : keySet) {
//            String stringArray = jsonObject.getJSONArray(s).toJSONString();
//            List<KeyValue> keyValues = JSONArray.parseArray(stringArray, KeyValue.class);
//            map.put(s, keyValues);
//        }
    }

    public static Map readJsonToMap(String filepath) throws IOException {
        Map logMap = new HashMap();
        Map tmpMap = null;
        File file = new File(filepath);
        String jsonString = FileUtils.readFileToString(file, "UTF-8");
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Object globalLog = jsonObject.get("ocel:global-log");
        Object globalEvent = jsonObject.get("ocel:global-event");
        Object globalObject = jsonObject.get("ocel:global-object");

        Object events = jsonObject.get("ocel:events");
        Object objects = jsonObject.get("ocel:objects");

        Map globalEventMap = (Map) JSON.parse(globalEvent.toString());
        tmpMap = new HashMap();
        for (Object map : globalEventMap.entrySet()){
            tmpMap.put(((Map.Entry)map).getKey(),((Map.Entry)map).getValue());
        }
        logMap.put("ocel:global-event",tmpMap);

        // save the events element
        Map eventsMap = (Map) JSON.parse(events.toString());
        Map allEventsMap = new HashMap();
        tmpMap = new HashMap();
        for (Object map : eventsMap.entrySet()){
            String key = (String) ((Map.Entry)map).getKey();
            Object value = ((Map.Entry)map).getValue();
            Map eventElementMap = (Map) JSON.parse(value.toString());
            for (Object eventElmt : eventElementMap.entrySet()) {
                String keyEvent = (String) ((Map.Entry)eventElmt).getKey();
                Object valueEvent = ((Map.Entry)eventElmt).getValue();
                if("ocel:vmap".equals(keyEvent) ) {
                    Map vMap = (Map) JSON.parse(valueEvent.toString());
                    Map vTmpMap = new HashMap();
                    for (Object vmapElment : vMap.entrySet()) {
                        vTmpMap.put(((Map.Entry)vmapElment).getKey(),((Map.Entry)vmapElment).getValue());
                    }
                    tmpMap.put(keyEvent,vTmpMap);
                }else if("ocel:omap".equals(keyEvent) ) {
                    List<String> tmpList = new ArrayList<>((List) ((Map.Entry) eventElmt).getValue());
                    tmpMap.put(keyEvent,tmpList);
                } else {
                    tmpMap.put(((Map.Entry)eventElmt).getKey(),((Map.Entry)eventElmt).getValue());
                }
                allEventsMap.put(key,tmpMap);
            }
            logMap.put("ocel:events",allEventsMap);
        }

        // save the objects element
        Map objectsMap = (Map) JSON.parse(objects.toString());
        Map allObjectsMap = new HashMap();

        for (Object map : objectsMap.entrySet()){
            tmpMap = new HashMap();
            String key = (String) ((Map.Entry)map).getKey();
            Object value = ((Map.Entry)map).getValue();

            Map ojbectElementMap = (Map) JSON.parse(value.toString());
            for (Object objectElmt : ojbectElementMap.entrySet()) {
                String keyObject = (String) ((Map.Entry)objectElmt).getKey();
                Object valueObject = ((Map.Entry)objectElmt).getValue();
                if("ocel:ovmap".equals(keyObject) ) {
                    Map ovMap = (Map) JSON.parse(valueObject.toString());
                    Map ovTmpMap = new HashMap();
                    for (Object ovmapElment : ovMap.entrySet()) {
                        ovTmpMap.put(((Map.Entry)ovmapElment).getKey(),((Map.Entry)ovmapElment).getValue());
                    }
                    tmpMap.put(keyObject,ovTmpMap);
                    allObjectsMap.put(key,tmpMap);
                }else {
                    tmpMap.put(((Map.Entry)objectElmt).getKey(),((Map.Entry)objectElmt).getValue());
                    allObjectsMap.put(key,tmpMap);
                }
            }
            logMap.put("ocel:objects",allObjectsMap);
        }
        return logMap;
    }

    public static JSONObject readJsonfileToObject(String filePath) throws IOException {
        File file = new File(filePath);
        String jsonString = FileUtils.readFileToString(file, "UTF-8");
        return JSONObject.parseObject(jsonString);
    }

    public static void main(String[] args) throws IOException {
        readJsonToMap("ocel/logs/minimal.jsonocel");
    }

    @Data
    static class KeyValue{
        private String key;
        private String value;
    }

}