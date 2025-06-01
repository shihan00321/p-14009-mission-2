package com.back;

public class JsonParser {
    public static String wiseSayingToJson(WiseSaying wiseSaying) {
        String json = "{\n"
                + "   \"id\": " + wiseSaying.getId() + ",\n"
                + "   \"content\": \"" + wiseSaying.getContent() + "\",\n"
                + "   \"author\": \"" + wiseSaying.getAuthor() + "\"\n"
                + "}";
        return json;
    }

    public static WiseSaying jsonToWiseSaying(String json) {
        String[] parts = json.replaceAll("[{}\"]", "").split(",");
        int id = 0;
        String content = "", author = "";
        for (String part : parts) {
            String[] str = part.split(":", 2);
            String key = str[0].trim();
            String value = str[1].trim();

            switch (key) {
                case "id":
                    id = Integer.parseInt(value);
                    break;
                case "content":
                    content = value;
                    break;
                case "author":
                    author = value;
                    break;
            }
        }
        return new WiseSaying(id, content, author);
    }

}
