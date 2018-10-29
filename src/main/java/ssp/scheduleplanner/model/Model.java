package ssp.scheduleplanner.model;

import java.util.function.Predicate;

import javafx.collections.ObservableList;
import ssp.scheduleplanner.model.task.Task;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluate to true */
    Predicate<Task> PREDICATE_SHOW_ALL_TASKS = unused -> true;

    /** {@code Predicate} that always evaluate to true*/
    Predicate<Task> PREDICATE_SHOW_ALL_ARCHIVED_TASKS = unused -> true;

    /** Clears existing backing model and replaces with the provided new data. */
    void resetData(ReadOnlySchedulePlanner newData);

    /** Returns the SchedulePlanner */
    ReadOnlySchedulePlanner getSchedulePlanner();

    /**
     * Returns true if a task with the same identity as {@code task} exists in the Schedule Planner.
     */
    boolean hasTask(Task task);

    /**
     * Returns true if a archived task with the same identity as {@code archivedTask} exists
     * in the Schedule Planner.
     */
    boolean hasArchivedTask(Task archivedTask);

    /**
     * Deletes the given task.
     * The task must exist in the Schedule Planner.
     */
    void deleteTask(Task target);

    /**
     * Adds the given task.
     * {@code task} must not already exist in the Schedule Planner.
     */
    void addTask(Task task);

    /**
     * Archive the given task.
     */
    void archiveTask(Task task);

    /**
     * Replaces the given task {@code target} with {@code editedTask}.
     * {@code target} must exist in the Schedule Planner.
     * The task identity of {@code editedTask} must not be the same as another existing task in the Schedule Planner.
     */
    void updateTask(Task target, Task editedTask);

    /** Returns an unmodifiable view of the filtered task list */
    ObservableList<Task> getFilteredTaskList();

    /** Returns an unmodifiable view of the filtered archived task list */
    ObservableList<Task> getFilteredArchivedTaskList();

    /**
     * Updates the filter of the filtered task list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredTaskList(Predicate<Task> predicate);

    /**
     * updates the filter of filtered archived task list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredArchivedTaskList(Predicate<Task> predicate);

    /**
     * Returns true if the model has previous Schedule Planner states to restore.
     */
    boolean canUndoSchedulePlanner();

    /**
     * Returns true if the model has undone Schedule Planner states to restore.
     */
    boolean canRedoSchedulePlanner();

    /**
     * Restores the model's Schedule Planner to its previous state.
     */
    void undoSchedulePlanner();

    /**
     * Restores the model's Schedule Planner to its previously undone state.
     */
    void redoSchedulePlanner();

    /**
     * Saves the current Schedule Planner state for undo/redo.
     */
    void commitSchedulePlanner();

}
