package com.young.aicustomer.model.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.young.aicustomer.dto.UserDTO;
import com.young.aicustomer.entity.QaHistory;
import com.young.aicustomer.model.CustomAIModel;
import com.young.aicustomer.service.IQaHistoryService;
import com.young.aicustomer.service.QaCacheService;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//import static com.young.aicustomer.api.Ultra.gson;

@Service("xunfei")
@RequiredArgsConstructor
public class XFModel implements CustomAIModel {

    private final OkHttpClient httpClient = new OkHttpClient();

    public static List<RoleContent> historyList = new ArrayList<>();

    private final IQaHistoryService qaHistoryService;

    private final QaCacheService qaCacheService;

    public final static Gson gson = new Gson();

    public static String removePrefix(String str, String prefix) {
        if (str != null && str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    @Override
    public void getAnswer(String question, UserDTO user, String sessionId, Consumer<String> onAnswer) {

//        String userId = "10284711用户";
        String userId = String.valueOf(user.getId());
        String url = "https://spark-api-open.xf-yun.com/v1/chat/completions";

        try {
            // 创建最外层的JSON对象
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user", userId);
            jsonObject.put("model", "Lite");

            // 创建messages数组
            JSONArray messagesArray = new JSONArray();
            // 创建单个消息的JSON对象
            System.out.print("我：" + question);

            if (historyList.isEmpty()) {    // 要么是新对话，要么就是再次进入历史对话
                // 向数据库查询是否有历史消息
                List<QaHistory> historyBySessionId = qaHistoryService.findHistoryBySessionId(sessionId);
                if (!historyBySessionId.isEmpty()) {
                    for (QaHistory history : historyBySessionId) {
                        RoleContent roleContent = new RoleContent();
                        roleContent.role = history.getRole();
                        roleContent.content = history.getContent();
                        historyList.add(roleContent);

                    }
                }
            }

            // 历史信息获取，如果有则携带
            if (!historyList.isEmpty()) {
                for (RoleContent tempRoleContent : historyList) {
                    messagesArray.add(JSON.toJSON(tempRoleContent));
                }
            }
            // 拼接最新问题
            RoleContent roleContent = new RoleContent();
            roleContent.role = "user";
            roleContent.content = question;
            messagesArray.add(JSON.toJSON(roleContent));
            historyList.add(roleContent);

            // 将messages数组添加到最外层的JSON对象中
            jsonObject.put("messages", messagesArray);
            // 设置stream属性为true
            jsonObject.put("stream", true);
            jsonObject.put("max_tokens", 8192);
            jsonObject.put("temperature", 0.1);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Authorization: Bearer PNkZnGTaIkiqSBxvQacF:WHZDRnZfkARHgSlgHlAz") // 替换为你真实的 Authorization 值
                    .post(RequestBody.create(
                            MediaType.parse("application/json"),
                            jsonObject.toString()))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    onAnswer.accept("[请求失败] " + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))

                    // 拼接最终回答
                    StringBuilder fullMsg = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.trim().isEmpty()) continue;
                            line = removePrefix(line, "data: ");
                            if (line.equals("[DONE]")) {
                                //  流式发送结束，存储历史（异步：MQ）
                                qaHistoryService.saveHistory(user.getId(), sessionId, question, "user");
                                qaHistoryService.saveHistory(user.getId(), sessionId, fullMsg.toString(), "assistant");    // 同步
                                // 符合缓存要求，缓存（异步：MQ）
                                if (question.length() < 100 && !dependsOnContext(question)) {
                                    qaCacheService.saveQa(user.getId(), question, fullMsg.toString());
                                }
                                break;
                            }
                            System.out.println(line);


                            JsonParse jsonParse = gson.fromJson(line, JsonParse.class);
                            List<Choices> choicesList =  jsonParse.choices;
                            for (Choices tempChoices : choicesList) {
                                Delta delta = tempChoices.delta;
                                System.out.println(delta);
                                if (delta != null) {
                                    String content = delta.content;

                                    fullMsg.append(content);

                                    onAnswer.accept(content);   // 将消息返回
                                    System.out.println("星火：" + content);
                                    RoleContent tempRoleContent = new RoleContent();
                                    tempRoleContent.setRole("assistant");
                                    tempRoleContent.setContent(content);
                                    historyList.add(roleContent);
                                }



                            }
//                        JsonObject json = jsonParse.parseString(line).getAsJsonObject();
//                        JsonArray choices = json.getAsJsonArray("choices");
//                            if (choices != null) {
//                                for (JsonElement choice : choices) {
//                                    JsonObject msg = choice.getAsJsonObject().getAsJsonObject("message");
//                                    if (msg != null) {
//                                        String content = msg.get("content").getAsString();
//                                        onAnswer.accept(content);
//                                    }
//                                }
//                            }
                        }
                    }
                }
            });



        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static class JsonParse {
        List<Choices> choices;
    }

    public static class Choices {
        Delta delta;
    }

    public static class Delta {
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

    // 简单规则：有跟上下文相关关键字时，不存在缓存里
    public boolean dependsOnContext(String question) {
        return question.matches(".*(之前|继续|刚才).*");
    }
}
