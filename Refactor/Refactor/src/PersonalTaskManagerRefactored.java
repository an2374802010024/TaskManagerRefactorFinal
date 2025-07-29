public class PersonalTaskManagerRefactored {
    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JSONArray loadTasks() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) return (JSONArray) obj;
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi đọc DB: " + e.getMessage());
        }
        return new JSONArray();
    }

    private void saveTasks(JSONArray tasks) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasks.toJSONString());
        } catch (IOException e) {
            System.err.println("Lỗi ghi DB: " + e.getMessage());
        }
    }

    private boolean isValidTitle(String title) {
        return title != null && !title.trim().isEmpty();
    }

    private LocalDate parseDueDate(String dueDateStr) {
        try {
            return LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isValidPriority(String priority) {
        return List.of("Thấp", "Trung bình", "Cao").contains(priority);
    }

    private boolean isDuplicateTask(JSONArray tasks, String title, String dueDate) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (task.get("title").toString().equalsIgnoreCase(title) &&
                task.get("due_date").toString().equals(dueDate)) {
                return true;
            }
        }
        return false;
    }

    private JSONObject createTask(String title, String description, String dueDateStr, String priority) {
        JSONObject task = new JSONObject();
        task.put("id", UUID.randomUUID().toString());
        task.put("title", title);
        task.put("description", description);
        task.put("due_date", dueDateStr);
        task.put("priority", priority);
        task.put("status", "Chưa hoàn thành");
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        task.put("created_at", now);
        task.put("last_updated_at", now);
        return task;
    }

    public JSONObject addTask(String title, String description, String dueDateStr, String priority) {
        if (!isValidTitle(title)) {
            System.out.println("Lỗi: Tiêu đề rỗng.");
            return null;
        }

        LocalDate dueDate = parseDueDate(dueDateStr);
        if (dueDate == null) {
            System.out.println("Lỗi: Ngày đến hạn không hợp lệ.");
            return null;
        }

        if (!isValidPriority(priority)) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ.");
            return null;
        }

        JSONArray tasks = loadTasks();
        if (isDuplicateTask(tasks, title, dueDateStr)) {
            System.out.printf("Lỗi: Nhiệm vụ '%s' trùng lặp ngày %s.%n", title, dueDateStr);
            return null;
        }

        JSONObject newTask = createTask(title, description, dueDateStr, priority);
        tasks.add(newTask);
        saveTasks(tasks);
        System.out.println("Đã thêm nhiệm vụ thành công.");
        return newTask;
    }
}
