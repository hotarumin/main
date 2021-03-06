package ssp.scheduleplanner.logic;

import javafx.collections.ObservableList;
import ssp.scheduleplanner.logic.commands.CommandResult;
import ssp.scheduleplanner.logic.commands.exceptions.CommandException;
import ssp.scheduleplanner.logic.parser.exceptions.ParseException;
import ssp.scheduleplanner.model.category.Category;
import ssp.scheduleplanner.model.task.Task;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * @param commandText The command as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /** Returns an unmodifiable view of the filtered list of tasks */
    ObservableList<Task> getFilteredTaskList();

    /** Returns an unmodifiable view of the filtered list of archived tasks */
    ObservableList<Task> getFilteredArchivedTaskList();

    /** Returns an unmodifiable view of the list of categories */
    ObservableList<Category> getCategoryList();

    /** Returns the list of input entered by the user, encapsulated in a {@code ListElementPointer} object */
    ListElementPointer getHistorySnapshot();
}
