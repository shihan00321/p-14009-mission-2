package com.back;

public class CommandManager {
    public static String getCommand(String cmd) {
        if (cmd.equals("목록")) return "목록";
        if (cmd.equals("등록")) return "등록";
        if (cmd.startsWith("삭제?id=")) return "삭제";
        if (cmd.startsWith("수정?id=")) return "수정";
        if (cmd.equals("종료")) return "종료";
        if (cmd.equals("빌드")) return "빌드";
        return "잘못된 명령어";
    }
}
