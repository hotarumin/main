package systemtests;

import static org.junit.Assert.assertTrue;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.DATE_DESC_AMY;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.DATE_DESC_BOB;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.INVALID_DATE_DESC;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.VALID_DATE_AMY;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static ssp.scheduleplanner.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static ssp.scheduleplanner.logic.parser.CliSyntax.PREFIX_TAG;
import static ssp.scheduleplanner.model.Model.PREDICATE_SHOW_ALL_TASKS;
import static ssp.scheduleplanner.testutil.TypicalIndexes.INDEX_FIRST_TASK;
import static ssp.scheduleplanner.testutil.TypicalIndexes.INDEX_SECOND_TASK;
import static ssp.scheduleplanner.testutil.TypicalTasks.AMY;
import static ssp.scheduleplanner.testutil.TypicalTasks.BOB;
import static ssp.scheduleplanner.testutil.TypicalTasks.KEYWORD_MATCHING_MEIER;

import org.junit.Test;

import ssp.scheduleplanner.commons.core.Messages;
import ssp.scheduleplanner.commons.core.index.Index;
import ssp.scheduleplanner.logic.commands.EditCommand;
import ssp.scheduleplanner.logic.commands.RedoCommand;
import ssp.scheduleplanner.logic.commands.UndoCommand;
import ssp.scheduleplanner.model.Model;
import ssp.scheduleplanner.model.tag.Tag;
import ssp.scheduleplanner.model.task.Date;
import ssp.scheduleplanner.model.task.Name;
import ssp.scheduleplanner.model.task.Priority;
import ssp.scheduleplanner.model.task.Task;
import ssp.scheduleplanner.model.task.Venue;
import ssp.scheduleplanner.testutil.TaskBuilder;

public class EditCommandSystemTest extends SchedulePlannerSystemTest {

    @Test
    public void edit() {
        Model model = getModel();

        /* ----------------- Performing edit operation while an unfiltered list is being shown ---------------------- */

        /* Case: edit all fields, command with leading spaces, trailing spaces and multiple spaces between each field
         * -> edited
         */
        Index index = INDEX_FIRST_TASK;
        String command = " " + EditCommand.COMMAND_WORD + "  " + index.getOneBased() + "  " + NAME_DESC_BOB + "  "
                + DATE_DESC_BOB + " " + EMAIL_DESC_BOB + "  " + ADDRESS_DESC_BOB + " " + TAG_DESC_HUSBAND + " ";
        Task editedTask = new TaskBuilder(BOB).withTags(VALID_TAG_HUSBAND).build();
        assertCommandSuccess(command, index, editedTask);

        /* Case: undo editing the last task in the list -> last task restored */
        command = UndoCommand.COMMAND_WORD;
        String expectedResultMessage = UndoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: redo editing the last task in the list -> last task edited again */
        command = RedoCommand.COMMAND_WORD;
        expectedResultMessage = RedoCommand.MESSAGE_SUCCESS;
        model.updateTask(
                getModel().getFilteredTaskList().get(INDEX_FIRST_TASK.getZeroBased()), editedTask);
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: edit a task with new values same as existing values -> edited */
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_BOB + DATE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        index = INDEX_SECOND_TASK;
        assertCommandSuccess(command, index, BOB);

        /* Case: edit a task with new values same as another task's values but with different name -> edited */
        assertTrue(getModel().getSchedulePlanner().getTaskList().contains(BOB));
        index = INDEX_SECOND_TASK;
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_AMY + DATE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        editedTask = new TaskBuilder(BOB).withName(VALID_NAME_AMY).build();
        assertCommandSuccess(command, index, editedTask);

        /* Case: edit a task with new values same as another task's values but with different date and email
         * -> edited
         */
        index = INDEX_SECOND_TASK;
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_BOB + DATE_DESC_AMY + EMAIL_DESC_AMY
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND + TAG_DESC_HUSBAND;
        editedTask = new TaskBuilder(BOB).withDate(VALID_DATE_AMY).withPriority(VALID_EMAIL_AMY).build();
        assertCommandSuccess(command, index, editedTask);

        /* Case: clear tags -> cleared */
        index = INDEX_FIRST_TASK;
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_TAG.getPrefix();
        Task taskToEdit = getModel().getFilteredTaskList().get(index.getZeroBased());
        editedTask = new TaskBuilder(taskToEdit).withTags().build();
        assertCommandSuccess(command, index, editedTask);

        /* ------------------ Performing edit operation while a filtered list is being shown ------------------------ */

        /* Case: filtered task list, edit index within bounds of address book but out of bounds of task list
         * -> rejected
         */
        showTasksWithName(KEYWORD_MATCHING_MEIER);
        int invalidIndex = getModel().getSchedulePlanner().getTaskList().size();
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + invalidIndex + NAME_DESC_BOB,
                Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);

        /* --------------------- Performing edit operation while a task card is selected -------------------------- */

        /* Case: selects first card in the task list, edit a task -> edited, card selection remains unchanged but
         * browser url changes
         */
        showAllTasks();
        index = INDEX_FIRST_TASK;
        selectTask(index);
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + NAME_DESC_AMY + DATE_DESC_AMY + EMAIL_DESC_AMY
                + ADDRESS_DESC_AMY + TAG_DESC_FRIEND;
        // this can be misleading: card selection actually remains unchanged but the
        // browser's url is updated to reflect the new task's name
        assertCommandSuccess(command, index, AMY, index);

        /* --------------------------------- Performing invalid edit operation -------------------------------------- */

        /* Case: invalid index (0) -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " 0" + NAME_DESC_BOB,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: invalid index (-1) -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " -1" + NAME_DESC_BOB,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: invalid index (size + 1) -> rejected */
        invalidIndex = getModel().getFilteredTaskList().size() + 1;
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + invalidIndex + NAME_DESC_BOB,
                Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);

        /* Case: missing index -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + NAME_DESC_BOB,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: missing all fields -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_TASK.getOneBased(),
                EditCommand.MESSAGE_NOT_EDITED);

        /* Case: invalid name -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_TASK.getOneBased() + INVALID_NAME_DESC,
                Name.MESSAGE_NAME_CONSTRAINTS);

        /* Case: invalid date -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_TASK.getOneBased() + INVALID_DATE_DESC,
                Date.MESSAGE_DATE_CONSTRAINTS);

        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_TASK.getOneBased() + INVALID_EMAIL_DESC,
                Priority.MESSAGE_PRIORITY_CONSTRAINTS);

        /* Case: invalid address -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_TASK.getOneBased() + INVALID_ADDRESS_DESC,
                Venue.MESSAGE_ADDRESS_CONSTRAINTS);

        /* Case: invalid tag -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_TASK.getOneBased() + INVALID_TAG_DESC,
                Tag.MESSAGE_TAG_CONSTRAINTS);

    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Index, Task, Index)} except that
     * the browser url and selected card remain unchanged.
     * @param toEdit the index of the current model's filtered list
     * @see EditCommandSystemTest#assertCommandSuccess(String, Index, Task, Index)
     */
    private void assertCommandSuccess(String command, Index toEdit, Task editedTask) {
        assertCommandSuccess(command, toEdit, editedTask, null);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Model, String, Index)} and in addition,<br>
     * 1. Asserts that result display box displays the success message of executing {@code EditCommand}.<br>
     * 2. Asserts that the model related components are updated to reflect the task at index {@code toEdit} being
     * updated to values specified {@code editedTask}.<br>
     * @param toEdit the index of the current model's filtered list.
     * @see EditCommandSystemTest#assertCommandSuccess(String, Model, String, Index)
     */
    private void assertCommandSuccess(String command, Index toEdit, Task editedTask,
            Index expectedSelectedCardIndex) {
        Model expectedModel = getModel();
        expectedModel.updateTask(expectedModel.getFilteredTaskList().get(toEdit.getZeroBased()), editedTask);
        expectedModel.updateFilteredTaskList(PREDICATE_SHOW_ALL_TASKS);

        assertCommandSuccess(command, expectedModel,
                String.format(EditCommand.MESSAGE_EDIT_TASK_SUCCESS, editedTask), expectedSelectedCardIndex);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Model, String, Index)} except that the
     * browser url and selected card remain unchanged.
     * @see EditCommandSystemTest#assertCommandSuccess(String, Model, String, Index)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        assertCommandSuccess(command, expectedModel, expectedResultMessage, null);
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays an empty string.<br>
     * 2. Asserts that the result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the browser url and selected card update accordingly depending on the card at
     * {@code expectedSelectedCardIndex}.<br>
     * 4. Asserts that the status bar's sync status changes.<br>
     * 5. Asserts that the command box has the default style class.<br>
     * Verifications 1 and 2 are performed by
     * {@code SchedulePlannerSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see SchedulePlannerSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     * @see SchedulePlannerSystemTest#assertSelectedCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage,
            Index expectedSelectedCardIndex) {
        executeCommand(command);
        expectedModel.updateFilteredTaskList(PREDICATE_SHOW_ALL_TASKS);
        assertCommandBoxShowsDefaultStyle();
        if (expectedSelectedCardIndex != null) {
            assertSelectedCardChanged(expectedSelectedCardIndex);
        } else {
            assertSelectedCardUnchanged();
        }
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays {@code command}.<br>
     * 2. Asserts that result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the browser url, selected card and status bar remain unchanged.<br>
     * 4. Asserts that the command box has the error style.<br>
     * Verifications 1 and 2 are performed by
     * {@code SchedulePlannerSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see SchedulePlannerSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }
}
