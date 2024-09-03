package talkie.components;

import java.util.Scanner;

import talkie.exception.TalkieException;
import talkie.task.Task;
import talkie.task.TaskList;

/**
 * Handles user interactions and displays messages to the user.
 * <p>
 * The {@code Ui} class manages the display of various types of messages to the user,
 * such as welcome messages, task addition, deletion, and error handling.
 * </p>
 */
public class Ui {

    /**
     * Enum representing different types of messages used by the {@code Ui} class.
     */
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

        /**
         * Gets the message associated with the {@code MessageType}.
         *
         * @return The message string.
         */
        public String getMessage() {
            return this.message;
        }
    }

    // Scanner for input
    private Scanner input = new Scanner(System.in);

    /**
     * Reads the next line of input from the user.
     *
     * @return The user's input as a string.
     */
    public String readCommand() {
        return this.input.nextLine();
    }

    /**
     * Closes the {@code Scanner} used for user input.
     */
    public void closeInput() {
        this.input.close();
    }

    /**
     * Displays a welcome message to the user.
     */
    public String welcomeMessage() {
        return MessageType.WELCOME_MESSAGE.getMessage();
    }

    /**
     * Displays a goodbye message to the user.
     */
    public String byeMessage() {
       return MessageType.BYE_MESSAGE.getMessage();
    }

    /**
     * Displays a message indicating that the date/time format is incorrect.
     */
    public String wrongDateTimeFormatMessage() {
        String message = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "Please enter the time in the format of <yyyy-MM-dd HHmm>!\n"
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
        return message;
    }

    /**
     * Displays a message confirming that a task has been added to the task list.
     *
     * @param t The task that was added.
     * @param taskListSize The current size of the task list after addition.
     */
    public String addMessage(Task t, int taskListSize) {
        String taskWord = (taskListSize > 1) ? "tasks" : "task";
        String finalMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "Got it. I've added this task:\n"
                + "  " + t + "\n"
                + "Now you have " + taskListSize + " " + taskWord + " in the list.\n"
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
       return finalMessage;
    }

    /**
     * Displays a message confirming that a task has been deleted from the task list.
     *
     * @param t The task that was deleted.
     * @param taskListSize The current size of the task list after deletion.
     */
    public String deleteMessage(Task t, int taskListSize) {
        String taskWord = (taskListSize > 1) ? "tasks" : "task";
        String doneMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "Noted! I've removed this task:\n"
                + "  " + t + "\n"
                + "Now you have " + taskListSize + " " + taskWord + " in the list.\n"
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
        return doneMessage;
    }

    /**
     * Displays a list of all tasks in the task list.
     *
     * @param tasks The list of tasks to display.
     */
    public String listTasks(TaskList tasks) {
        String listMessage = "";
        for (int i = 1; i <= tasks.size(); i++) {
            Task currTask = tasks.getTask(i);
            String description = (i) + ". " + currTask + "\n";
            listMessage += description;
        }

        String finalMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "Here are the tasks in your list:\n"
                + listMessage
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
       return finalMessage;
    }

    /**
     * Displays a message confirming that a task has been marked as done.
     *
     * @param task The task that was marked as done.
     */
    public String markMessage(Task task) {
        String doneMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "Nice! I've marked this task as done:\n"
                + " " + task + "\n"
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
        return doneMessage;
    }

    /**
     * Displays a message confirming that a task has been marked as not done.
     *
     * @param task The task that was marked as not done.
     */
    public String unMarkMessage(Task task) {
        String undoneMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "OK, I've marked this task as not done yet:\n"
                + " " + task + "\n"
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
        return undoneMessage;
    }

    /**
     * Displays an error message when a {@link TalkieException} is encountered.
     *
     * @param e The exception to be displayed.
     */
    public void showTalkieException(TalkieException e) {
        System.out.println(String.format("%s\n", e));
    }

    /**
     * Searches for tasks containing the specified keyword and displays the matching tasks.
     * <p>
     * This method iterates through the provided {@code TaskList}, checks each task for the presence of the keyword,
     * and collects the tasks that match. It then displays the matching tasks or appropriate messages if no tasks
     * are found or if the task list is empty.
     * </p>
     *
     * @param tasks The {@code TaskList} containing all tasks to search through.
     * @param keyword The keyword to search for in the task descriptions.
     */
    public String findTasks(TaskList tasks, String keyword) {
        if (tasks.isEmpty()) {
            String emptyMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                    + "There are no tasks in your list! \n"
                    + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
            System.out.println(emptyMessage);
        }

        TaskList searchedList = new TaskList();
        for (int i = 1; i <= tasks.size(); i++) {
            Task currTask = tasks.getTask(i);
            if (currTask.containsWord(keyword)) {
                searchedList.addTask(currTask);
            }
        }

        if (searchedList.isEmpty()) {
            String noTaskMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                    + "There are no tasks found in your list! \n"
                    + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
            System.out.println(noTaskMessage);
        }

        StringBuilder searchedListMessage = new StringBuilder();
        for (int i = 1; i <= searchedList.size(); i++) {
            Task searchedTask = searchedList.getTask(i);
            String description = (i) + ". " + searchedTask + "\n";
            searchedListMessage.append(description);
        }

        String finalMessage = MessageType.HORIZONTAL_LINE.getMessage() + "\n"
                + "Here are the matching tasks in your list:\n"
                + searchedListMessage
                + MessageType.HORIZONTAL_LINE.getMessage() + "\n";
        return finalMessage;
    }

}
