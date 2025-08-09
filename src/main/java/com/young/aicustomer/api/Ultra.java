package com.young.aicustomer.api;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Ultra {
//    public static String header = "76f2e2926708e525fbc8841a7ba1b0e1: MDA2YmNkYjBhOWM5YzNiOTc5MzUwNGMw"; // 注意此处替换自己的key和secret
    public static String header = "Authorization: Bearer PNkZnGTaIkiqSBxvQacF:WHZDRnZfkARHgSlgHlAz"; // 注意此处替换自己的key和secret
    public static Gson gson = new Gson();

    public static List<RoleContent> historyList = new ArrayList<>();

    public static void main(String[] args) {
        String userId = "10284711用户";
        while (true) {
            try {
                String url = "https://spark-api-open.xf-yun.com/v1/chat/completions";
                // 创建最外层的JSON对象
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user", userId);
                jsonObject.put("model", "Lite");
                // 创建messages数组
                JSONArray messagesArray = new JSONArray();
                // 创建单个消息的JSON对象
                System.out.print("我：");
                Scanner scanner = new Scanner(System.in);
                String tempQuestion = scanner.nextLine();
                // System.err.println(tempQuestion);
                // 历史信息获取，如果有则携带
                if (historyList.size() > 0) {
                    for (RoleContent tempRoleContent : historyList) {
                        messagesArray.add(JSON.toJSON(tempRoleContent));
                    }
                }
                // 拼接最新问题
                RoleContent roleContent = new RoleContent();
                roleContent.role = "user";
                roleContent.content = tempQuestion;
                messagesArray.add(JSON.toJSON(roleContent));
                historyList.add(roleContent);

                // 将messages数组添加到最外层的JSON对象中
                jsonObject.put("messages", messagesArray);
                // 设置stream属性为true
                jsonObject.put("stream", false);
                jsonObject.put("max_tokens", 8192);
                jsonObject.put("temperature", 0.1);
                // System.err.println(jsonObject);


                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", header);
                con.setDoOutput(true);

                OutputStream os = con.getOutputStream();
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = con.getResponseCode();
                // System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();


                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    response.append(inputLine);
                    // System.out.println(inputLine);
                    JsonParse jsonParse = gson.fromJson(inputLine, JsonParse.class);
                    List<Choices> choicesList = jsonParse.choices;
                    for (Choices tempChoices : choicesList) {
                        System.out.println("星火：" + tempChoices.message.content);
                        RoleContent tempRoleContent = new RoleContent();
                        tempRoleContent.setRole("assistant");
                        tempRoleContent.setContent(tempChoices.message.content);
                        historyList.add(roleContent);
                    }
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class JsonParse {
        List<Choices> choices;
    }

    static class Choices {
        Message message;
    }

    static class Message {
        String content;
    }

    static class RoleContent {
        String role;
        String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
