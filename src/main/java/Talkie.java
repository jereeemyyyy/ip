import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Talkie {

    public enum MessageType {
        HORIZONTAL_LINE("-------------------------------------------------------------------"),
        WELCOME_MESSAGE(HORIZONTAL_LINE.message + "\n"
                + "Hello! I'm Talkie, your friendly ChatBot.\n"
                + "What can I do for you?\n"
                + HORIZONTAL_LINE.message + "\n"),
        BYE_MESSAGE(HORIZONTAL_LINE.message + "\n"
                + "Bye. Hope to see you again soon!\n"
                + HORIZONTAL_LINE.message + "\n");

        private final String message;

        MessageType(String message) {
            this.message = message;
        }

        public String getMessage() {
            return this.message;
        }
    }

    private static String directory = "./data";
    private static String fileName = "Talkie.txt";
    private static String dataPath = String.valueOf(Paths.get(Talkie.directory, Talkie.fileName));

    protected static List<Task> taskList = new ArrayList<>();
    protected static Scanner scanner = new Scanner(System.in);


    private static void loadData() {

        File database = new File(Talkie.dataPath);
        try {

            Scanner fileReader = new Scanner(database);
            while (fileReader.hasNextLine()) {
                String entry = fileReader.nextLine();
                readEntry(entry);
            }
        } catch (FileNotFoundException e) {
            Talkie.createDatabase();

        } catch (TalkieException e) {
            System.out.println("OH NO! Error when reading data entry!");
        }
    }

    private static void readEntry(String entry) throws TalkieNoTaskFoundException {
        String[] fields = entry.split(" \\| ");
        Task taskToBeAdded;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        switch(fields[0]) {
            case "T":
                taskToBeAdded = new ToDo(fields[2]);
                break;

            case "E":
                taskToBeAdded = new Event(fields[2],
                        LocalDateTime.parse(fields[3], formatter),
                        LocalDateTime.parse(fields[4], formatter));
                break;

            case "D":
                taskToBeAdded = new Deadline(fields[2],
                        LocalDateTime.parse(fields[3], formatter));
                break;

            default:
                throw new TalkieNoTaskFoundException();
        }

        if (Integer.parseInt(fields[1]) == 1) {
            taskToBeAdded.markAsDone();
        }

        Talkie.taskList.add(taskToBeAdded);
    }

    private static void createDatabase() {
        File db = new File(Talkie.dataPath);
        File dir = new File(Talkie.directory);

        dir.mkdir();

        try {
            db.createNewFile();
        } catch (IOException e) {
            System.out.println("Oops! Something went wrong when creating the database!");
        }
    }

    private static void saveData() throws IOException {
        FileWriter writer = new FileWriter(Talkie.dataPath, false);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);

        for (Task task : Talkie.taskList) {
            bufferedWriter.write(task.stringifyTask());
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
        writer.close();
    }



    // Creates Deadline Task
    public static void createDeadline(String input) throws TalkieMissingArgumentException {
        String[] parts = input.split(" ", 2); // Split into type and the rest of the input

        try {
            if (parts.length == 2) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

                String details = parts[1]; // rest of the input (eg. from, to details)
                String[] deadlineParts = details.split("/by ");
                String description = deadlineParts[0].trim();
                String by = deadlineParts[1].trim();

                LocalDateTime time = LocalDateTime.parse(by, formatter);

                Task newDeadline = new Deadline(description, time);
                taskList.add(newDeadline);
                String message = Talkie.addMessage(newDeadline);
                System.out.println(message);
            } else {
                throw new TalkieMissingArgumentException(parts[0],
                        "The 'description' and 'by' of deadline cannot be empty.");
            }
        } catch (DateTimeParseException e) {
            System.out.println(MessageType.HORIZONTAL_LINE.message + "\n"
                    + "Please enter the time in the format of <yyyy-MM-dd HHmm>!\n"
                    + MessageType.HORIZONTAL_LINE.message + "\n");
        }
    }

    // Creates Event Task
    public static void createEvent(String input) throws TalkieMissingArgumentException {
        String[] parts = input.split(" ", 2); // Split into type and the rest of the input

        try {
            if (parts.length == 2) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
                String details = parts[1]; // rest of the input (eg. from, to details)
                String[] eventParts = details.split("/from | /to ");

                String description = eventParts[0].trim();
                String from = eventParts[1].trim();
                String to = eventParts[2].trim();

                LocalDateTime startTime = LocalDateTime.parse(from, formatter);
                LocalDateTime endTime = LocalDateTime.parse(to, formatter);

                if (startTime.isAfter(endTime)) {
                    System.out.println("The end time must be after the start time!");
                    return;
                }

                Task newEvent = new Event(description, startTime, endTime);
                taskList.add(newEvent);
                String message = Talkie.addMessage(newEvent);
                System.out.println(message);
            } else {
                throw new TalkieMissingArgumentException(parts[0],
                        "The 'description', 'from' and 'to' of event cannot be empty.");
            }
        } catch (DateTimeParseException e) {
            System.out.println(MessageType.HORIZONTAL_LINE + "\n"
                    + "Please enter the time in the format of <yyyy-MM-dd HHmm>!\n"
                    + MessageType.HORIZONTAL_LINE.message + "\n");
        }

    }

    // Creates ToDo Task
    public static void createToDo(String input) throws TalkieMissingArgumentException{
        String[] parts = input.split(" ", 2); // Split into type and the rest of the input

        if (parts.length == 2) {
            String details = parts[1]; // rest of the input (eg. from, to details)
            Task newToDo = new ToDo(details.trim());
            taskList.add(newToDo);
            String message = Talkie.addMessage(newToDo);
            System.out.println(message);
        } else {
            throw new TalkieMissingArgumentException(parts[0], "The 'description' of todo cannot be empty.");
        }
    }

    // The message displayed whenever a task is created
    public static String addMessage(Task t) {
        String taskWord = (taskList.size() > 1) ? "tasks" : "task";
        return MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "Got it. I've added this task:\n"
                + "  " + t + "\n"
                + "Now you have " + taskList.size() + " " + taskWord + " in the list.\n"
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
    }

    // Deletes a task
    public static void deleteTask(String input)
            throws TalkieMissingArgumentException, TalkieNoTaskFoundException, TalkieInvalidArgumentException{
        String[] temp = input.split(" ");

        // Check if user included an argument
        if (temp.length == 1) {
            throw new TalkieMissingArgumentException(temp[0], "The 'delete' command requires an integer as argument");

        // Check if user included the correct int argument
        } else if (Talkie.isInteger(temp[1])) {
            int index = Integer.parseInt(input.split(" ")[1]) - 1;

            // Check if the task is in the list
            if (index <= taskList.size() - 1) {
                Task task = taskList.remove(index);
                String taskWord = (taskList.size() > 1) ? "tasks" : "task";
                String doneMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                        + "Noted! I've removed this task:\n"
                        + "  " + task + "\n"
                        + "Now you have " + taskList.size() + " " + taskWord + " in the list.\n"
                        + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
                System.out.println(doneMessage);
            } else {
                throw new TalkieNoTaskFoundException();
            }
        } else {
            throw new TalkieInvalidArgumentException(temp[0], "The 'delete' command requires an integer as argument");
        }
    }

    // Display the list of tasks
    public static void listTasks() {
        String listMessage = "";
        for (int i=0; i<taskList.size(); i++) {
            Task currTask = taskList.get(i);
            String description = (i + 1) + ". " + currTask + "\n";
            listMessage += description;
        }

        String finalListMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "Here are the tasks in your list:\n"
                +  listMessage
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
        System.out.println(finalListMessage);
    }

    // Marks a task
    public static void markTask(String input)
            throws TalkieInvalidArgumentException, TalkieMissingArgumentException, TalkieNoTaskFoundException {
        String[] temp = input.split(" ");

        // Check if the user included an argument
        if (temp.length == 1) {
            throw new TalkieMissingArgumentException(temp[0], "The 'mark' command requires an integer as argument");

        // Check if user included the correct int argument
        } else if (Talkie.isInteger(temp[1])) {
            int index = Integer.parseInt(input.split(" ")[1]) - 1;

            // Check if the task is in the list
            if (index <= taskList.size() - 1) {
                Task task = taskList.get(index);
                task.markAsDone();
                String doneMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                        + "Nice! I've marked this task as done:\n"
                        + " " + task + "\n"
                        + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
                System.out.println(doneMessage);
            } else {
                throw new TalkieNoTaskFoundException();
            }

        } else {
            throw new TalkieInvalidArgumentException(temp[0], "The 'mark' command requires an integer as argument");
        }
    }

    // Unmarks a Task
    public static void unmarkTask(String input)
            throws TalkieInvalidArgumentException, TalkieMissingArgumentException, TalkieNoTaskFoundException {
        String[] temp = input.split(" ");

        // Check if the user included an argument
        if (temp.length == 1) {
            throw new TalkieMissingArgumentException(temp[0], "The 'unmark' command requires an integer as argument");

        // Check if the user included the correct int argument
        } else if (Talkie.isInteger(temp[1])) {
            int index = Integer.parseInt(input.split(" ")[1]) - 1;

            // Check if the task index is valid in the task list
            if (index <= taskList.size() - 1) {
                Task task = taskList.get(index);
                task.markAsNotDone();
                String undoneMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                        + "OK, I've marked this task as not done yet:\n"
                        + " " + task + "\n"
                        + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
                System.out.println(undoneMessage);
            } else {
                throw new TalkieNoTaskFoundException();
            }

        } else {
            throw new TalkieInvalidArgumentException(temp[0], "The 'unmark' command requires an integer as argument");
        }
    }

    // Check if the input string is a number (Helper method for unmark and mark)
    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Exits the program
    public static void exit() {
        try {
            Talkie.saveData();
            System.out.println(MessageType.BYE_MESSAGE.getMessage());
        } catch (IOException e) {
            System.out.println("Oops! Something went wrong when saving the data!");
        }
    }

    // Runs the main program
    public static void runTalkie() {
        System.out.println(MessageType.WELCOME_MESSAGE.getMessage());

        boolean isFinished = false;
        while (!isFinished) {
            String input = scanner.nextLine();

            try {
                if (input.equalsIgnoreCase("bye")) {
                    Talkie.exit();
                    isFinished = true;

                } else if (input.equalsIgnoreCase("list")) {
                    Talkie.listTasks();

                } else if (input.startsWith("delete")) {
                    Talkie.deleteTask(input);

                } else if (input.startsWith("mark")) {
                    Talkie.markTask(input);

                } else if (input.startsWith("unmark")) {
                    Talkie.unmarkTask(input);

                } else if (input.startsWith("todo")) {
                    Talkie.createToDo(input);

                } else if (input.startsWith("deadline")) {
                    Talkie.createDeadline(input);

                } else if (input.startsWith("event")) {
                    Talkie.createEvent(input);

                } else {
                    throw new TalkieUnknownCommandException(input);
                }
            } catch (TalkieException e) {
                System.out.println(e);
            }

        }
    }

    public static void main(String[] args) {
        // Load data
        Talkie.loadData();
        // Start of Talkie
        Talkie.runTalkie();
    }
}
