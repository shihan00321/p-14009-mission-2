package com.back;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    private final Scanner scanner = new Scanner(System.in);
    private int lastId;

    private final Path directory = Paths.get("db/wiseSaying");
    private final Path lastIdTextFile = directory.resolve("lastId.txt");

    public void run() {
        System.out.println("== 명언 앱 ==");
        init();
        while (true) {
            System.out.print("명령) ");
            String cmd = scanner.nextLine().trim();
            switch (CommandManager.getCommand(cmd)) {
                case "종료" -> {
                    scanner.close();
                    return;
                }
                case "등록" -> registerWiseSaying();
                case "목록" -> showList();
                case "삭제" -> deleteWiseSaying(cmd);
                case "수정" -> updateWiseSaying(cmd);
                case "빌드" -> build();
                default -> System.out.println("등록되지 않은 명령어입니다.");
            }
        }
    }

    private void init() {
        try {
            Files.createDirectories(directory);
            if (Files.exists(lastIdTextFile)) {
                String content = Files.readString(lastIdTextFile);
                lastId = Integer.parseInt(content) + 1;
            } else {
                lastId = 1;
            }
        } catch (IOException e) {
            System.out.println("디렉토리 생성에 실패했습니다." + e.getMessage());
        }
    }

    private void showList() {
        System.out.println("번호 / 작가 / 명언");
        System.out.println("----------------------");
        List<WiseSaying> wiseSayingList = findWiseSayingList();
        wiseSayingList.forEach(System.out::println);
    }

    private void saveLastId() {
        try {
            Files.writeString(lastIdTextFile, String.valueOf(lastId));
        } catch (IOException e) {
            System.out.println("lastId.txt 저장 실패: " + e.getMessage());
        }
    }

    private void registerWiseSaying() {
        try {
            Path file = directory.resolve(lastId + ".json");

            System.out.print("명언 : ");
            String content = scanner.nextLine().trim();
            System.out.print("작가 : ");
            String author = scanner.nextLine().trim();

            WiseSaying wiseSaying = new WiseSaying(lastId, content, author);

            String json = JsonParser.wiseSayingToJson(wiseSaying);
            Files.writeString(file, json);

            saveLastId();
            System.out.println(lastId++ + "번 명언이 등록되었습니다.");
        } catch (IOException e) {
            System.out.println("파일 등록에 실패하였습니다. " + e.getMessage());
        }
    }

    private void updateWiseSaying(String cmd) {
        try {
            int updateId = Integer.parseInt(cmd.substring(6));
            Path file = directory.resolve(updateId + ".json");

            if (!Files.exists(file)) {
                System.out.printf("%d번 명언은 존재하지 않습니다.\n", updateId);
                return;
            }

            String json = Files.readString(file);
            WiseSaying oldWiseSaying = JsonParser.jsonToWiseSaying(json);

            System.out.printf("명언(기존) : %s\n", oldWiseSaying.getContent());
            System.out.print("명언 : ");
            String newContent = scanner.nextLine().trim();

            System.out.printf("작가(기존) : %s\n", oldWiseSaying.getAuthor());
            System.out.print("작가 : ");
            String newAuthor = scanner.nextLine().trim();

            WiseSaying wiseSaying = new WiseSaying(updateId, newContent, newAuthor);
            String newJson = JsonParser.wiseSayingToJson(wiseSaying);
            Files.writeString(file, newJson);

        } catch (IOException e) {
            System.out.println("올바른 형식이 아닙니다. (예: 수정?id=1)");
        }
    }

    private void deleteWiseSaying(String cmd) {
        try {
            int removeId = Integer.parseInt(cmd.substring(6));
            Path filePath = Paths.get("db/wiseSaying", removeId + ".json");
            if (Files.exists(filePath)) {
                Files.deleteIfExists(filePath);
                System.out.printf("%d번 명언이 삭제되었습니다.%n", removeId);
            } else {
                System.out.printf("%d번 명언은 존재하지 않습니다.%n", removeId);
            }
        } catch (IOException e) {
            System.out.println("올바른 형식이 아닙니다. (예: 삭제?id=1)");
        }
    }

    private List<WiseSaying> findWiseSayingList() {
        try (Stream<Path> pathStream = Files.list(directory)) {
            return pathStream
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.matches("\\d+.json");
                    })
                    .map(path -> {
                        try {
                            return JsonParser.jsonToWiseSaying(Files.readString(path));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted((w1, w2) -> w2.getId() - w1.getId())
                    .toList();
        } catch (Exception e) {
            System.out.println("파일 목록을 불러올 수 없습니다. " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private void build() {
        try {
            List<WiseSaying> wiseSayingList = findWiseSayingList();

            List<WiseSaying> sorted = wiseSayingList.stream()
                    .sorted(Comparator.comparingInt(WiseSaying::getId))
                    .toList();

            String json = sorted.stream()
                    .map(JsonParser::wiseSayingToJson)
                    .map(String::trim)
                    .collect(Collectors.joining(",\n", "[\n  ", "\n]"));
            Path file = directory.resolve("data.json");
            Files.writeString(file, json);

            System.out.println("data.json 파일의 내용이 갱신되었습니다.");
        } catch (IOException e) {
            System.out.println("data.json 생성 중 오류 발생 " + e.getMessage());
        }
    }
}